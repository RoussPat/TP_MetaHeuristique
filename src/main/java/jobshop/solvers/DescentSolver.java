package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.io.IOException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

public class DescentSolver implements Solver {

    int priorotyType;
    public DescentSolver(int prio){
        super();
        priorotyType = prio;
    }

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
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
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
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

    @Override
    public Result solve(Instance instance, long deadline) throws IOException {

        Result currentbest = (new GreedySolver(priorotyType)).solve(instance, deadline);
        if (deadline - System.currentTimeMillis() < 1) {
            return (currentbest);
        }
        int i;
        Schedule tempsced =null;
        ResourceOrder RO = new ResourceOrder(currentbest.schedule);
        ResourceOrder ROtemp = null;
        List<Block> blockList = blocksOfCriticalPath(RO);
        List<ResourceOrder> neighbors = new ArrayList<ResourceOrder>();
        for (Block b : blockList) {
            for (Swap s : neighbors(b)) {
                ROtemp = RO.copy();
                s.applyOn(ROtemp);
                neighbors.add(ROtemp);
            }
        }
        //System.out.println("criticalPath" + RO.toSchedule().criticalPath().toString());
        //System.out.println("RO : "+ RO.toString());
        //System.out.println(currentbest.schedule.toString());
        //System.out.println("neighbors Size : " + neighbors.size() + " Block size : "+blockList.size());

        while (!neighbors.isEmpty()) {
            //System.out.println("neighbors size : " + neighbors.size());
            i = neighbors.size()-1;
            while( i >= 0 && !neighbors.isEmpty()) {
                //System.out.println("debut de boucle i=" + i);
                if (deadline - System.currentTimeMillis() < 1) {
                    return (currentbest);
                }
                //System.out.println(neighbors.get(i).toSchedule());
                //System.out.print("contender: " + neighbors.get(i).toSchedule().makespan() );
                //System.out.println(" To beat  : " + currentbest.schedule.makespan());
                tempsced = neighbors.get(i).toSchedule();
                if(tempsced != null) {
                    if (tempsced.makespan() < currentbest.schedule.makespan()) {
                        RO = neighbors.get(i);
                        currentbest = new Result(instance, RO.toSchedule(), Result.ExitCause.Timeout);
                        blockList = blocksOfCriticalPath(RO);
                        neighbors.clear();
                        for (Block b : blockList) {
                            for (Swap s : neighbors(b)) {
                                ROtemp = RO.copy();
                                s.applyOn(ROtemp);
                                neighbors.add(ROtemp);
                            }
                        }
                        //System.out.println("NEW NEIGHBORS !");
                        i = neighbors.size();
                        //System.out.println("new neighbors i=" + i + " " + neighbors.isEmpty());
                    } else if (neighbors.get(i).toSchedule().makespan() == currentbest.schedule.makespan()) {
                        neighbors.remove(i);
                        i = neighbors.size();
                        //System.out.println("equal i=" + i + " " + neighbors.isEmpty());
                    } else {
                        neighbors.remove(i);
                        i = neighbors.size();
                        //System.out.println("less i=" + i + " " + neighbors.isEmpty());
                    }
                }
                else  {
                    neighbors.remove(i);
                    i = neighbors.size();
                }
                i--;
            }
        }
        return new Result(instance, RO.toSchedule(), Result.ExitCause.Blocked);
    }

    /** Returns a list of all blocks of the critical path. */
    List<DescentSolver.Block> blocksOfCriticalPath(ResourceOrder order) {
        Schedule sced = order.toSchedule();
        List<DescentSolver.Block> ret = new ArrayList<>();
        List<Task> cp  = sced.criticalPath();
        DescentSolver.Block b ;
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
                        b = new DescentSolver.Block(lastM,firstofblock,lastofblock);
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
            b = new DescentSolver.Block(lastM,firstofblock,lastofblock);
            ret.add(b);
        }
        return(ret);
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<DescentSolver.Swap> neighbors(DescentSolver.Block block) {
        List<DescentSolver.Swap> ret= new ArrayList<DescentSolver.Swap>();
        if(block.firstTask +1 == block.lastTask ){
            ret.add(new DescentSolver.Swap(block.machine,block.firstTask,block.lastTask));
        }
        else{
            ret.add(new DescentSolver.Swap(block.machine,block.firstTask,block.firstTask+1));
            ret.add(new DescentSolver.Swap(block.machine,block.lastTask-1,block.lastTask));
        }
        return ret;
    }

}
