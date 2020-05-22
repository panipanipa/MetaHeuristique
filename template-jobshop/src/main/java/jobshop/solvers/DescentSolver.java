package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.myResourceOrder;
import jobshop.encodings.Task;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class DescentSolver implements Solver {

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
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
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

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn_old(ResourceOrder order) {
            List<Task> path = order.toSchedule().criticalPath() ;
            Task task1 = path.get(t1) ;
            Task task2 = path.get(t2) ;
            int find = 2 ;
            int index = 0 ;
            while(find!=0) {
                if (order.tasksByMachine[this.machine][index].equals(task1)) {
                    //System.out.println("find it");
                    find-- ;
                    order.tasksByMachine[this.machine][index] = task2 ;
                }
                else if (order.tasksByMachine[this.machine][index].equals(task2)) {
                   // System.out.println("find it too");
                    find-- ;
                    order.tasksByMachine[this.machine][index] = task1 ;
                }
                index++ ;
            }
           // System.out.println(order);
        }

        public Swap copy() {
            return new Swap(this.machine, this.t1, this.t2) ;
        }

        public void applyOn(ResourceOrder order) {
            Task tmp = order.tasksByMachine[this.machine][t1] ;
            order.tasksByMachine[this.machine][t1] = order.tasksByMachine[this.machine][this.t2] ;
            order.tasksByMachine[this.machine][this.t2] = tmp ;
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        //init
        Solver solver = new mySolverGloutons("EST_LRPT") ;
        Result result = solver.solve(instance, deadline) ;
        ResourceOrder order = result.schedule.toResourceOrder() ;
        //System.out.println("init : " + order);

        //loop
        int myBest = result.schedule.makespan() ;
        boolean findNewBest = true ;
        int index = 0 ;
        while(findNewBest) {
            findNewBest = false ;
            ResourceOrder nextOrder = order.copy() ;
            int nextValue = myBest ;
            //LinkedList<ResourceOrder> myNeighbors = new LinkedList<>() ;
            List<Block> myBlocks = blocksOfCriticalPath(order) ;
            for (Block b : myBlocks) {
               // System.out.println("loop " + index + " machine " + b.machine + " : " + b.firstTask + " " + b.lastTask) ;
                List<Swap> mySwaps = neighbors(b) ;
                for(Swap s : mySwaps) {
                    ResourceOrder tmp = order.copy() ;
                    s.applyOn(tmp);
                    Schedule sched = tmp.toSchedule() ;
                    if(sched!=null) {
                        int tmpValue = sched.makespan() ;
                        if(tmpValue<myBest) {
                            nextOrder = tmp ;
                            myBest = tmpValue ;
                            findNewBest = true ;
                        }
                    }
                }
            }
            order = nextOrder ;
            index++ ;
        }
        return new Result(instance, order.toSchedule(), Result.ExitCause.Blocked) ;
    }

    /** Returns a list of all blocks of the critical path. */
     List<Block> blocksOfCriticalPath(ResourceOrder order) {
        Instance instance = order.instance ;
        Schedule sched = order.toSchedule() ;
        List<Task> path = sched.criticalPath() ;
        /*
        for(Task t: path) {
            System.out.println("task : " + t + " machine " + instance.machine(t) ) ;
        }

         */
        LinkedList<Block> result = new LinkedList<>();

        //int machineBlock = -1 ;
        int startBlock = -1 ;
        int endBlock = -1 ;
        int pred_machine = -1 ;
        boolean findOne = false ;
        int index = 0 ;
        for(Task myTask : path) {
            if(pred_machine!=instance.machine(myTask)) {
                if (findOne) {
                    boolean find = false ;
                    int index_order = 0 ;
                    int index_startTask = 0 ;
                    while(!find) {
                        if(order.tasksByMachine[pred_machine][index_order].equals(path.get(startBlock))) {
                            find = true ;
                            index_startTask = index_order ;
                        }

                        index_order++ ;
                    }
                    result.add(new Block(pred_machine, index_startTask, index_startTask+endBlock-startBlock)) ;
                }
                findOne = false ;
                pred_machine = instance.machine(myTask) ;
                startBlock = index ;
            }
            else {
                endBlock = index ;
                findOne = true ;
            }
            index++ ;
        }
        return result;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        LinkedList<Swap> result = new LinkedList<>();
        int size = block.lastTask - block.firstTask + 1 ;
        if(size==2) {
            result.add(new Swap(block.machine, block.lastTask, block.firstTask)) ;
        }
        else {
            result.add(new Swap(block.machine, block.firstTask, block.firstTask+1)) ;
            result.add(new Swap(block.machine, block.lastTask-1, block.lastTask)) ;
        }
        return result ;
    }

}
