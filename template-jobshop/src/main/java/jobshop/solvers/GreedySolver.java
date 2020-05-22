package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.Task;
import jobshop.encodings.myResourceOrder;

import java.util.Arrays;
import java.util.HashSet;

public class GreedySolver implements Solver {

    private String priority ;

    public GreedySolver(String priority) {
        this.priority = priority ;
    }

    @Override
    public Result solve(Instance instance, long deadline) {
        myResourceOrder sol = new myResourceOrder(instance) ;
        int[] pointeur = new int[instance.numMachines] ;
        Arrays.fill(pointeur, 0) ;
        HashSet<Task> realizable = new HashSet<>() ;
        for(int i=0; i<instance.numJobs;i++) {
            realizable.add(new Task(i, 0)) ;
        }
       // HashSet<Task> est_task = new HashSet<>() ;
       // System.out.println("init " + realizable);

        int[] releaseTimeOfMachine = new int[instance.numMachines];
        int [][] startTimes = new int [instance.numJobs][instance.numTasks];

        //compute

        while(!realizable.isEmpty()) {
            HashSet<Task> est_task = new HashSet<>() ;
            int min = Integer.MAX_VALUE ;
            int max = Integer.MIN_VALUE ;
            Task nextTask = null ;
            for (Task mytask: realizable) {
                //SPT Priority
                if(this.priority.equals("SPT")) {
                    int duration = instance.duration(mytask.job, mytask.task) ;
                    if (duration<min) {
                        min = duration ;
                        nextTask = mytask ;
                    }
                }
                else if(this.priority.equals("LRPT")) {
                    int job_duration = 0 ;
                    int jobtask = mytask.task ;
                    while(jobtask<instance.numTasks) {
                        job_duration+=instance.duration(mytask.job, jobtask) ;
                        jobtask++ ;
                    }
                    if(job_duration>max) {
                        max = job_duration ;
                        nextTask = mytask ;
                    }
                }
                else if(this.priority.contains("EST")) {
                    int est = mytask.task == 0 ? 0 : startTimes[mytask.job][mytask.task-1] + instance.duration(mytask.job, mytask.task-1);
                    est = Math.max(est, releaseTimeOfMachine[instance.machine(mytask)]);
                    startTimes[mytask.job][mytask.task] = est;
                    int mymachine =  instance.machine(mytask.job, mytask.task) ;
                    //System.out.println("task : "+ mytask + " est : "+ est);
                    if(est<min) {
                        min = est ;
                        est_task.clear() ;
                        est_task.add(mytask) ;
                    }
                    else if (est==min) {
                        est_task.add(mytask) ;
                    }
                }
            }
            if(this.priority.equals("EST_SPT")) {
                min = Integer.MAX_VALUE ;
                for (Task mytask: est_task) {
                    int duration = instance.duration(mytask.job, mytask.task);
                    if (duration < min) {
                        min = duration;
                        nextTask = mytask;
                    }
                }
                releaseTimeOfMachine[instance.machine(nextTask)] = startTimes[nextTask.job][nextTask.task] + instance.duration(nextTask);
                //System.out.println(nextTask);
            }
            else if(this.priority.equals("EST_LRPT")) {
                max = Integer.MIN_VALUE ;
                for (Task mytask: est_task) {
                    int job_duration = 0 ;
                    int jobtask = mytask.task ;
                    while(jobtask<instance.numTasks) {
                        job_duration+=instance.duration(mytask.job, jobtask) ;
                        jobtask++ ;
                    }
                    if(job_duration>max) {
                        max = job_duration ;
                        nextTask = mytask ;
                    }
                }
                releaseTimeOfMachine[instance.machine(nextTask)] = startTimes[nextTask.job][nextTask.task] + instance.duration(nextTask);
            }
            //System.out.println(nextTask);
            int machine = instance.machine(nextTask.job, nextTask.task) ;
            sol.order[machine][pointeur[machine]] = nextTask ;
            pointeur[machine]++ ;
            realizable.remove(nextTask) ;
            if(nextTask.task<instance.numTasks-1)
                realizable.add(new Task(nextTask.job, nextTask.task+1)) ;
           // System.out.println(realizable);
        }

        //System.out.println(sol);
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked) ;
    }
}
