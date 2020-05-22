package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.List;
import java.util.Map;

public class TabooSolver extends DescentSolver {

    class Taboo {

        final int dureeTaboo ;
        final int numTask ;

        public int[][] forbiden ;


        Taboo(int nbTask, int nbJob, int numTask, int dureeTaboo) {
            int total = nbJob*nbTask ;
            this.dureeTaboo = dureeTaboo ;
            this.numTask = numTask ;
            forbiden = new int[nbTask*nbJob][nbTask*nbJob] ;
            for(int i=0; i<total ; i++) {
                for(int j=0; j<total; j++) {
                    forbiden[i][j] = 0 ;
                }
            }
        }


        //foiré -> faut interdire (numjob, num task) -> (numjob*NumTASK/JOB)+numtask
        public void forbidSwap(Task t1, Task t2,  int k) {
            int id1 = t1.job*this.numTask+t1.task ;
            int id2 = t2.job*this.numTask+t2.task ;
            this.forbiden[id2][id1] = k + dureeTaboo ;
        }

        public boolean isAllowed(Task t1, Task t2, int k) {
            int id1 = t1.job*this.numTask+t1.task ;
            int id2 = t2.job*this.numTask+t2.task ;
            return (k>=this.forbiden[id1][id2]) ;
        }

    }

    final int maxIter = 300;

    @Override
    public Result solve(Instance instance, long deadline) {
       //init
        Solver solver = new mySolverGloutons("EST_LRPT") ;
        Taboo taboo = new Taboo(instance.numTasks, instance.numJobs, instance.numTasks, 9) ;
        Result result = solver.solve(instance, deadline) ;
        ResourceOrder order = result.schedule.toResourceOrder() ;
        int k = 0 ;
        //System.out.println("init : " + order);

        //loop
        while(k<maxIter) {
            int myBestNeibour = Integer.MAX_VALUE ;
            //var memory for loop
            ResourceOrder nextOrder = order.copy() ;
            //LinkedList<ResourceOrder> myNeighbors = new LinkedList<>() ;
            List<Block> myBlocks = blocksOfCriticalPath(order) ;
            Swap swapToNext = null ;
            //actions
            for (Block b : myBlocks) {
               // System.out.println("loop " + index + " machine " + b.machine + " : " + b.firstTask + " " + b.lastTask) ;
                List<Swap> mySwaps = neighbors(b) ;
                for(Swap s : mySwaps) {
                    Task t1 = order.tasksByMachine[s.machine][s.t1] ;
                    Task t2 = order.tasksByMachine[s.machine][s.t2] ;
                    if(taboo.isAllowed(t1, t2, k)) {
                        ResourceOrder tmp = order.copy() ;
                        s.applyOn(tmp);
                        Schedule sched = tmp.toSchedule() ;
                        //choice of the best neighbor
                        if(sched!=null) {
                            int tmpValue = sched.makespan() ;
                            if(tmpValue<myBestNeibour) {
                                myBestNeibour = tmpValue ;
                                nextOrder = tmp ;
                                swapToNext = s.copy() ;
                            }
                        }
                    }
                }
            }
            if (swapToNext != null){
                Task t1 = order.tasksByMachine[swapToNext.machine][swapToNext.t1] ;
                Task t2 = order.tasksByMachine[swapToNext.machine][swapToNext.t2] ;
                taboo.forbidSwap(t1,t2, k);
            }
            order = nextOrder ;
            k++ ;
        }
        return new Result(instance, order.toSchedule(), Result.ExitCause.Blocked) ;
    }

}
