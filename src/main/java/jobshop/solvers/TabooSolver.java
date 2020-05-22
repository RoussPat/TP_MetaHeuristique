package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabooSolver implements Solver {


    int MaxIter;
    int DureeTabou;

    public TabooSolver (int MaxIter,int dureeTabou){
        this.MaxIter=MaxIter;
        this.DureeTabou = dureeTabou;
    }

    /**
     * A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     * <p>
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     * <p>
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     */
    static class Block {
        /**
         * machine on which the block is identified
         */
        final int machine;
        /**
         * index of the first task of the block
         */
        final int firstTask;
        /**
         * index of the last task of the block
         */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }

        public String toString() {
            return ("machine: " + this.machine + " fisrtTask : " + this.firstTask + " lastTask : " + this.lastTask);
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     * <p>
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     * <p>
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /**
         * Apply this swap on the given resource order, transforming it into a new solution.
         */
        public void applyOn(ResourceOrder order) {
            Task temp = order.tasksByMachine[this.machine][this.t1];
            order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = temp;
        }
    }

    /** Returns a list of all blocks of the critical path. */
    List<TabooSolver.Block> blocksOfCriticalPath(ResourceOrder order) {
        Schedule sced = order.toSchedule();
        List<TabooSolver.Block> ret = new ArrayList<>();
        List<Task> cp  = sced.criticalPath();
        TabooSolver.Block b ;
        int lastM=-1,firstofblock=-1,lastofblock=-1,k,t;
        for (t=0;t<cp.size();t++) {
            if(lastM==-1){
                lastM = order.instance.machine(cp.get(t));
                for(k=0;k<order.instance.numTasks;k++){
                    if(order.tasksByMachine[lastM][k].equals(cp.get(t))){
                        firstofblock = k;
                    }
                }
            }
            else{
                if(order.instance.machine(cp.get(t)) != lastM){
                    for(k=0;k<order.instance.numJobs;k++){
                        if(order.tasksByMachine[lastM][k].equals(cp.get(t-1))){
                            lastofblock = k;
                        }
                    }
                    if(lastofblock > firstofblock ){
                        b = new TabooSolver.Block(lastM,firstofblock,lastofblock);
                        ret.add(b);
                    }
                    lastM = order.instance.machine(cp.get(t));
                    for(k=0;k<order.instance.numTasks;k++){
                        if(order.tasksByMachine[lastM][k].equals(cp.get(t))){
                            firstofblock = k;
                        }
                    }
                }
            }
        }
        for(k=0;k<order.instance.numJobs;k++){
            if(order.tasksByMachine[lastM][k].equals(cp.get(t-1))){
                lastofblock = k;
            }
        }
        if(lastofblock > firstofblock  && firstofblock != -1){
            b = new TabooSolver.Block(lastM,firstofblock,lastofblock);
            ret.add(b);
        }
        return(ret);
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<TabooSolver.Swap> neighbors(TabooSolver.Block block) {
        List<TabooSolver.Swap> ret= new ArrayList<TabooSolver.Swap>();
        if(block.firstTask +1 == block.lastTask ){
            ret.add(new TabooSolver.Swap(block.machine,block.firstTask,block.lastTask));
        }
        else{
            ret.add(new TabooSolver.Swap(block.machine,block.firstTask,block.firstTask+1));
            ret.add(new TabooSolver.Swap(block.machine,block.lastTask-1,block.lastTask));
        }
        return ret;
    }


    @Override
    public Result solve(Instance instance, long deadline) throws IOException {
        Result Sinit = (new GreedySolver(2)).solve(instance, deadline);
        if (deadline - System.currentTimeMillis() < 1) {
            return (Sinit);
        }
        boolean haschanged;
        int i,j,k=0;
        Schedule Shedtemp=null;
        Result CurBest = Sinit;
        Schedule tempsced = null;
        ResourceOrder LocalBest = null;
        ResourceOrder ROtemp = null;
        int[][] MatrixTaboo = new int [instance.numTasks*instance.numJobs][instance.numTasks*instance.numJobs];
        for(i=0;i<instance.numTasks*instance.numJobs;i++){
            for(j=0;j<instance.numTasks*instance.numJobs;j++){
                MatrixTaboo[i][j]=-1;
            }
        }
        //MatrixtoPrint(MatrixTaboo,instance.numTasks*instance.numJobs);
        ArrayList<TabooSolver.Swap> swaps = new ArrayList<TabooSolver.Swap>();
        ResourceOrder Bestneighbors = null;
        TabooSolver.Swap BestneighborsSwap = null;
        ResourceOrder RO = new ResourceOrder(Sinit.schedule);
        List<TabooSolver.Block> blockList = blocksOfCriticalPath(RO);
        List<ResourceOrder> neighbors = new ArrayList<ResourceOrder>();
        for (TabooSolver.Block b : blockList) {
            for (TabooSolver.Swap s : neighbors(b)) {
                ROtemp = RO.copy();
                s.applyOn(ROtemp);
                neighbors.add(ROtemp);
                swaps.add(s);
            }
        }
        while(!neighbors.isEmpty() && k<MaxIter){
            if (deadline - System.currentTimeMillis() < 1) {
                return (CurBest);
            }
            //System.out.println("neighbors size : " + neighbors.size());
            if(neighbors.size() == 1){
                if((MatrixTaboo[swaps.get(0).t2][swaps.get(0).t1] < k)){
                    Bestneighbors = neighbors.get(0);
                    haschanged = true;
                }
                else{
                    haschanged = true;
                }
            }
            else {
                i=0;
                haschanged = true;
                Bestneighbors = neighbors.get(0);
                BestneighborsSwap = swaps.get(i);
                for (ResourceOrder cur : neighbors) {
                    tempsced = cur.toSchedule();
                    if(tempsced != null) {
                        if ((tempsced.makespan() < Bestneighbors.toSchedule().makespan()) && (MatrixTaboo[swaps.get(i).t1][swaps.get(i).t2] < k)) {

                            Bestneighbors = cur;
                            BestneighborsSwap = swaps.get(i);
                            haschanged = true;
                        }
                    }else{
                        //donothing
                    }
                    i++;
                }
            }
            neighbors.clear();


            if (haschanged) {
                MatrixTaboo[BestneighborsSwap.t1][BestneighborsSwap.t2] = k + this.DureeTabou;
                swaps.clear();
                Shedtemp = Bestneighbors.toSchedule();
                //System.out.println(Shedtemp.makespan());
                if (Shedtemp.makespan() < CurBest.schedule.makespan()) {
                    CurBest = new Result(instance, Shedtemp, Result.ExitCause.Timeout);
                }
                blockList = blocksOfCriticalPath(Bestneighbors);
                for (TabooSolver.Block b : blockList) {
                    for (TabooSolver.Swap s : neighbors(b)) {
                        ROtemp = Bestneighbors.copy();
                        s.applyOn(ROtemp);
                        neighbors.add(ROtemp);
                        swaps.add(s);
                    }
                }
            }

            //System.out.println("k = " + k);
            //MatrixtoPrint(MatrixTaboo,instance.numTasks*instance.numJobs);
            k++;
        }
        return new Result(instance, CurBest.schedule, Result.ExitCause.Blocked);
    }

    void MatrixtoPrint(int[][] Matrix,int size){
        int i,j;
        //System.out.println("PrintMatrix");
        for(i=0;i<size;i++){
            for(j=0;j<size;j++){
                if(Matrix[i][j] != -1){
                    System.out.println(Matrix[i][j] + " @ i="+i+" j="+j+ "   ");
                }
            }

        }
    }
}
