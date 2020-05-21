package jobshop.solvers;


import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.io.IOException;
import java.util.ArrayList;

public class GreedySolver implements Solver{

    int prioroty;
    ResourceOrder RO;
    ArrayList<Task> Todo;
    ArrayList<Task> Doable;
    ArrayList<Task> Selected;
    ArrayList<Task> Done;
    int[] JobAvailability;
    int[] MachineAvailability;
    int[] tasksPerMachine;
    int[] timeleft;

    public  GreedySolver(int prio){
        prioroty=prio;
        RO = null;
        Todo= new ArrayList();
        Doable= new ArrayList();
        Selected = new ArrayList();
        Done= new ArrayList();
        JobAvailability = null;
        MachineAvailability = null;
        tasksPerMachine=null;
        int[] timeleft = null;
    }
    Result SolveSPT(Instance instance,long deadline) throws IOException{
        int next,smalesttime,t,k;
        boolean trouvé;
        while(! Doable.isEmpty()){
            if(deadline - System.currentTimeMillis() < 1) {
                return (new Result(instance, null, Result.ExitCause.Timeout));
            }
            next = 0;
            smalesttime = instance.duration(Doable.get(0));

            for (t = 1; t < Doable.size(); t++) {
                if (instance.duration(Doable.get(t)) < smalesttime) {
                    next = t;
                    smalesttime = instance.duration(Doable.get(t));
                }
            }
            RO.tasksByMachine[instance.machine(Doable.get(next))][tasksPerMachine[instance.machine(Doable.get(next))]]=Doable.get(next);
            tasksPerMachine[instance.machine(Doable.get(next))]++;
            k=0;
            trouvé = false;
            while(k<Todo.size() && !trouvé){
                if(Todo.get(k).job == Doable.get(next).job){
                    if(Todo.get(k).task-1 == Doable.get(next).task){
                        Doable.add(Todo.remove(k));
                        trouvé = true;
                    }
                }
                k++;
            }
            Done.add(Doable.remove(next));

        }
        if(!Todo.isEmpty() || !Doable.isEmpty()){
            //System.out.println("Soluce in greedySolver" + RO.toSchedule());
            return(new Result(instance,null,Result.ExitCause.Blocked));
        }
        else{
            //System.out.println("Soluce in greedySolver" + RO.toSchedule());
            return(new Result(instance,RO.toSchedule(),Result.ExitCause.ProvedOptimal));
        }


    }
    Result SolveLRPT(Instance instance,long deadline) throws IOException{
        int next,t,k;
        boolean trouvé;
        while(! Doable.isEmpty()){
            if(deadline - System.currentTimeMillis() < 1) {
                return (new Result(instance, null, Result.ExitCause.Timeout));
            }
            next = 0;
            for (t = 1; t < Doable.size(); t++) {
                if (timeleft[(Doable.get(t)).job] > timeleft[(Doable.get(next)).job]) {
                    next = t;
                }
            }
            timeleft[(Doable.get(next)).job] -=  instance.duration(Doable.get(next));
            RO.tasksByMachine[instance.machine(Doable.get(next))][tasksPerMachine[instance.machine(Doable.get(next))]]=Doable.get(next);
            tasksPerMachine[instance.machine(Doable.get(next))]++;
            k=0;
            trouvé = false;
            while(k<Todo.size() && !trouvé){
                if(Todo.get(k).job == Doable.get(next).job){
                    if(Todo.get(k).task-1 == Doable.get(next).task){
                        Doable.add(Todo.remove(k));
                        trouvé = true;
                    }
                }
                k++;
            }
            Done.add(Doable.remove(next));

        }
        if(!Todo.isEmpty() || !Doable.isEmpty()){
            //System.out.println("Soluce in greedySolver" + RO.toSchedule());
            return(new Result(instance,null,Result.ExitCause.Blocked));
        }
        else{
            //System.out.println("Soluce in greedySolver" + RO.toSchedule());
            return(new Result(instance,RO.toSchedule(),Result.ExitCause.ProvedOptimal));
        }

    }
    Result SolveEST_SPT(Instance instance,long deadline) throws IOException{
        //int [][] sched= new int[instance.numJobs][instance.numTasks]; //directly make the scedule
        int j,t,EST_value,next,smalesttime;
        boolean trouvé;
        /*for(j=0;j<instance.numJobs;j++){ //init the scedule at -1
            for(t=0;t<instance.numTasks;t++){
                sched[j][t]=-1;
            }
        }*/

        while(! Doable.isEmpty()){
            if(deadline - System.currentTimeMillis() < 1){
                return(new Result(instance,null,Result.ExitCause.Timeout));
            }
            //EST SELETION ----------------------------------------------
            EST_value = Integer.MAX_VALUE;
            Selected.clear();
            for (t=0;t<Doable.size();t++) {
                if(Integer.max(MachineAvailability[instance.machine(Doable.get(t))],JobAvailability[Doable.get(t).job]) < EST_value  ){
                    EST_value = Integer.max(MachineAvailability[instance.machine(Doable.get(t))],JobAvailability[Doable.get(t).job]);
                }
            }
            for (t=0;t<Doable.size();t++) {
                if(Integer.max(MachineAvailability[instance.machine(Doable.get(t))],JobAvailability[Doable.get(t).job]) == EST_value  ){
                    Selected.add(Doable.get(t));
                }
            }
            // SPT SELECTION -------------------------------------------
            next = 0;
            smalesttime = instance.duration(Selected.get(0));
            for (t = 1; t < Selected.size(); t++) {
                if (instance.duration(Selected.get(t)) < smalesttime) {
                    next = t;
                    smalesttime = instance.duration(Selected.get(t));
                }
            }
            // EST VAR UPDATE ------------------------------------------
            if(MachineAvailability[instance.machine(Selected.get(next))]>JobAvailability[Selected.get(next).job]){
                //sched[Doable.get(next).job][Selected.get(next).task] = MachineAvailability[instance.machine(Selected.get(next))];
                MachineAvailability[instance.machine(Selected.get(next))]+=instance.duration(Selected.get(next));
                JobAvailability[Selected.get(next).job] = MachineAvailability[instance.machine(Selected.get(next))];
            }
            else{
                //sched[Doable.get(next).job][Selected.get(next).task] = JobAvailability[Selected.get(next).job];
                JobAvailability[Selected.get(next).job] +=instance.duration(Selected.get(next));
                MachineAvailability[instance.machine(Selected.get(next))] = JobAvailability[Selected.get(next).job];
            }
            //LISTS UPDATE AFTER SELECTION
            next = Doable.indexOf(Selected.get(next));
            RO.tasksByMachine[instance.machine(Doable.get(next))][tasksPerMachine[instance.machine(Doable.get(next))]]=Doable.get(next);
            tasksPerMachine[instance.machine(Doable.get(next))]++;
            t=0;
            trouvé = false;
            while(t<Todo.size() && !trouvé){
                if(Todo.get(t).job == Doable.get(next).job){
                    if(Todo.get(t).task-1 == Doable.get(next).task){
                        Doable.add(Todo.remove(t));
                        trouvé = true;
                    }
                }
                t++;
            }
            Done.add(Doable.remove(next));
        }
        if(!Todo.isEmpty() || !Doable.isEmpty()){
            //System.out.println("Soluce in greedySolver" + RO.toSchedule());
            return(new Result(instance,null,Result.ExitCause.Blocked));
        }
        else{
            //System.out.println("Soluce in greedySolver" + RO.toSchedule());
            //return(new Result(instance,new Schedule(instance,sched),Result.ExitCause.ProvedOptimal));
            return(new Result(instance,RO.toSchedule(),Result.ExitCause.ProvedOptimal));
        }
    }
    Result SolveEST_LRPT(Instance instance,long deadline) throws IOException{
        //int [][] sched= new int[instance.numJobs][instance.numTasks]; //directly make the scedule
        int j,t,EST_value,next;
        boolean trouvé;
        /*for(j=0;j<instance.numJobs;j++){ //init the scedule at -1
            for(t=0;t<instance.numTasks;t++){
                sched[j][t]=-1;
            }
        }*/

        while(! Doable.isEmpty()){
            if(deadline - System.currentTimeMillis() < 1){
                return(new Result(instance,null,Result.ExitCause.Timeout));
            }
            //EST SELETION ----------------------------------------------
            EST_value = Integer.MAX_VALUE;
            Selected.clear();
            for (t=0;t<Doable.size();t++) {
                if(Integer.max(MachineAvailability[instance.machine(Doable.get(t))],JobAvailability[Doable.get(t).job]) < EST_value  ){
                    EST_value = Integer.max(MachineAvailability[instance.machine(Doable.get(t))],JobAvailability[Doable.get(t).job]);
                }
            }
            for (t=0;t<Doable.size();t++) {
                if(Integer.max(MachineAvailability[instance.machine(Doable.get(t))],JobAvailability[Doable.get(t).job]) == EST_value  ){
                    Selected.add(Doable.get(t));
                }
            }
            // LRPT SELECTION--------------------------------------------
            next = 0;
            for (t = 1; t < Selected.size(); t++) {
                if (timeleft[(Selected.get(t)).job] > timeleft[(Selected.get(next)).job]) {
                    next = t;
                }
            }
            timeleft[(Selected.get(next)).job] -=  instance.duration(Selected.get(next));
            // EST VAR UPDATE ------------------------------------------
            if(MachineAvailability[instance.machine(Selected.get(next))]>JobAvailability[Selected.get(next).job]){
                //sched[Doable.get(next).job][Selected.get(next).task] = MachineAvailability[instance.machine(Selected.get(next))];
                MachineAvailability[instance.machine(Selected.get(next))]+=instance.duration(Selected.get(next));
                JobAvailability[Selected.get(next).job] = MachineAvailability[instance.machine(Selected.get(next))];
            }
            else{
                //sched[Doable.get(next).job][Selected.get(next).task] = JobAvailability[Selected.get(next).job];
                JobAvailability[Selected.get(next).job] +=instance.duration(Selected.get(next));
                MachineAvailability[instance.machine(Selected.get(next))] = JobAvailability[Selected.get(next).job];
            }
            //LISTS UPDATE AFTER SELECTION
            next = Doable.indexOf(Selected.get(next));
            RO.tasksByMachine[instance.machine(Doable.get(next))][tasksPerMachine[instance.machine(Doable.get(next))]]=Doable.get(next);
            tasksPerMachine[instance.machine(Doable.get(next))]++;
            t=0;
            trouvé = false;
            while(t<Todo.size() && !trouvé){
                if(Todo.get(t).job == Doable.get(next).job){
                    if(Todo.get(t).task-1 == Doable.get(next).task){
                        Doable.add(Todo.remove(t));
                        trouvé = true;
                    }
                }
                t++;
            }
            Done.add(Doable.remove(next));


        }
        if(!Todo.isEmpty() || !Doable.isEmpty()){
            //System.out.println("Soluce in greedySolver" + RO.toSchedule());
            return(new Result(instance,null,Result.ExitCause.Blocked));
        }
        else{
            //System.out.println("Soluce in greedySolver" + RO.toSchedule());
            //return(new Result(instance,new Schedule(instance,sched),Result.ExitCause.ProvedOptimal));
            return(new Result(instance,RO.toSchedule(),Result.ExitCause.ProvedOptimal));
        }
    }

    @Override
    public Result solve(Instance instance, long deadline) throws IOException {
        Result res;
        int j, k, t;
        tasksPerMachine = new int[instance.numMachines];
        for (k = 0; k < instance.numMachines; k++) { //init the tab tasksPerMachine to 0
            tasksPerMachine[k] = 0;
        }
        for (j = 0; j < instance.numJobs; j++) { //add all task to the (T)odo list and add the fisrt task of each jobs to the Doable List
            for (t = 1; t < instance.numTasks; t++) {
                Todo.add(new Task(j, t));
            }
            Doable.add(new Task(j, 0));
        }
        switch (prioroty) {
            case 0: //SPT
                RO = new ResourceOrder(instance);
                res = SolveSPT(instance, deadline);
                break;
            case 1: //LRPT
                timeleft = new int[instance.numJobs]; //this variable holds the total time left on a Job for LRPT method
                for (j = 0; j < instance.numMachines; j++) {
                    timeleft[j] = 0;
                    for (t = 0; t < instance.numTasks; t++) {
                        timeleft[j] += instance.duration(j, t);
                    }
                }
                RO = new ResourceOrder(instance);
                res = SolveLRPT(instance, deadline);
                break;
            case 2: //EST_SPT
                RO = new ResourceOrder(instance);
                JobAvailability = new int[instance.numJobs];
                MachineAvailability = new int[instance.numMachines];
                for (j = 0; j < instance.numJobs; j++) {
                    JobAvailability[j] = 0;
                }
                for (k = 0; k < instance.numMachines; k++) {
                    MachineAvailability[k] = 0;
                }
                res = SolveEST_SPT(instance, deadline);
                break;
            case 3: // EST_LRPT
                RO = new ResourceOrder(instance);
                timeleft = new int[instance.numJobs]; //this variable holds the total time left on a Job for LRPT method
                for (j = 0; j < instance.numMachines; j++) {
                    timeleft[j] = 0;
                    for (t = 0; t < instance.numTasks; t++) {
                        timeleft[j] += instance.duration(j, t);
                    }
                }
                JobAvailability = new int[instance.numJobs];
                MachineAvailability = new int[instance.numMachines];
                for (j = 0; j < instance.numJobs; j++) {
                    JobAvailability[j] = 0;
                }
                for (k = 0; k < instance.numMachines; k++) {
                    MachineAvailability[k] = 0;
                }
                res = SolveEST_LRPT(instance, deadline);
                break;
            default:
                throw new IOException("[GreedySolver]  bad priority rule used");
        }
        return res;
    }

}