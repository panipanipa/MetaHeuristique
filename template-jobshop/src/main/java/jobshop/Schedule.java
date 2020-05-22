package jobshop;



import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.encodings.myResourceOrder;

import java.util.Arrays;

import java.util.*;
import java.util.stream.IntStream;


public class Schedule {
    public final Instance pb;
    // start times of each job and task
    // times[j][i] is the start time of task (j,i) : i^th task of the j^th job
    final int[][] times;

    public Schedule(Instance pb, int[][] times) {
        this.pb = pb;
        this.times = new int[pb.numJobs][];
        for(int j = 0 ; j < pb.numJobs ; j++) {
            this.times[j] = Arrays.copyOf(times[j], pb.numTasks);
        }
    }

    public String toString() {
        String string = "" ;
        for (int job=0; job<pb.numJobs; job++) {
            string += "job " + job + " :\n" ;
            for (int task=0; task<pb.numTasks ; task++) {
                string += "task " + task + " start at " + this.times[job][task] + "\n";
            }
            string += "\n" ;
        }
        return string ;
    }

    public JobNumbers toJobNumbers() {
        JobNumbers jn = new JobNumbers(this.pb) ;
        int[] pointeur_job = new int[pb.numJobs] ;
        Arrays.fill(pointeur_job, 0) ;
        while(jn.nextToSet<pb.numJobs*pb.numTasks) {
            int min = Integer.MAX_VALUE ;
            int next_j = -1 ;
            for (int j=0; j<pb.numJobs; j++) {
                if(pointeur_job[j]<pb.numTasks && startTime(j,pointeur_job[j])<min) {
                    min = startTime(j,pointeur_job[j]) ;
                    next_j = j ;
                }
            }
            //System.out.println("min : " + min);
            jn.jobs[jn.nextToSet++] = next_j ;
            pointeur_job[next_j]++ ;
        }
        return jn ;
    }

    public myResourceOrder toMyResourceOrder() {
        myResourceOrder ro = new myResourceOrder(this.pb) ;
        int[] pointeur_job = new int[pb.numJobs] ;
        int[] pointeur_machine = new int[pb.numMachines] ;
        Arrays.fill(pointeur_job, 0) ;
        Arrays.fill(pointeur_machine, 0) ;
        int set = 0 ;
        int toSet = pb.numJobs*pb.numTasks ;
        while(set<toSet) {
            int min = Integer.MAX_VALUE ;
            int next_j = -1 ;
            for (int j=0; j<pb.numJobs; j++) {
                if(pointeur_job[j]<pb.numTasks && startTime(j,pointeur_job[j])<min) {
                    min = startTime(j,pointeur_job[j]) ;
                    next_j = j ;
                }
            }
            int machine = this.pb.machine(next_j, pointeur_job[next_j]) ;
            ro.order[machine][pointeur_machine[machine]] = new Task(next_j, pointeur_job[next_j]) ;
            pointeur_job[next_j]++ ;
            pointeur_machine[machine]++ ;
            set++ ;
        }
        return ro ;
    }

    public ResourceOrder toResourceOrder() {
        ResourceOrder ro = new ResourceOrder(this.pb) ;
        int[] pointeur_job = new int[pb.numJobs] ;
        int[] pointeur_machine = new int[pb.numMachines] ;
        Arrays.fill(pointeur_job, 0) ;
        Arrays.fill(pointeur_machine, 0) ;
        int set = 0 ;
        int toSet = pb.numJobs*pb.numTasks ;
        while(set<toSet) {
            int min = Integer.MAX_VALUE ;
            int next_j = -1 ;
            for (int j=0; j<pb.numJobs; j++) {
                if(pointeur_job[j]<pb.numTasks && startTime(j,pointeur_job[j])<min) {
                    min = startTime(j,pointeur_job[j]) ;
                    next_j = j ;
                }
            }
            int machine = this.pb.machine(next_j, pointeur_job[next_j]) ;
            ro.tasksByMachine[machine][pointeur_machine[machine]] = new Task(next_j, pointeur_job[next_j]) ;
            pointeur_job[next_j]++ ;
            pointeur_machine[machine]++ ;
            set++ ;
        }
        return ro ;
    }

    public int startTime(int job, int task) {
        return times[job][task];
    }

    /** Returns true if this schedule is valid (no constraint is violated) */
    public boolean isValid() {
        for(int j = 0 ; j<pb.numJobs ; j++) {
            for(int t = 1 ; t<pb.numTasks ; t++) {
                if(startTime(j, t-1) + pb.duration(j, t-1) > startTime(j, t))
                    return false;
            }
            for(int t = 0 ; t<pb.numTasks ; t++) {
                if(startTime(j, t) < 0)
                    return false;
            }
        }

        for (int machine = 0 ; machine < pb.numMachines ; machine++) {
            for(int j1=0 ; j1<pb.numJobs ; j1++) {
                int t1 = pb.task_with_machine(j1, machine);
                for(int j2=j1+1 ; j2<pb.numJobs ; j2++) {
                    int t2 = pb.task_with_machine(j2, machine);

                    boolean t1_first = startTime(j1, t1) + pb.duration(j1, t1) <= startTime(j2, t2);
                    boolean t2_first = startTime(j2, t2) + pb.duration(j2, t2) <= startTime(j1, t1);

                    if(!t1_first && !t2_first)
                        return false;
                }
            }
        }

        return true;
    }

    public int makespan() {
        int max = -1;
        for(int j = 0 ; j<pb.numJobs ; j++) {
            max = Math.max(max, startTime(j, pb.numTasks-1) + pb.duration(j, pb.numTasks -1));
        }
        return max;
    }

    public int startTime(Task task) {
        return startTime(task.job, task.task);
    }

    public int endTime(Task task) {
        return startTime(task) + pb.duration(task.job, task.task);
    }

    public boolean isCriticalPath(List<Task> path) {
        if(startTime(path.get(0)) != 0) {
            return false;
        }
        if(endTime(path.get(path.size()-1)) != makespan()) {
            return false;
        }
        for(int i=0 ; i<path.size()-1 ; i++) {
            if(endTime(path.get(i)) != startTime(path.get(i+1)))
                return false;
        }
        return true;
    }

    public List<Task> criticalPath() {
        // select task with greatest end time
        Task ldd = IntStream.range(0, pb.numJobs)
                .mapToObj(j -> new Task(j, pb.numTasks-1))
                .max(Comparator.comparing(this::endTime))
                .get();
        assert endTime(ldd) == makespan();

        // list that will contain the critical path.
        // we construct it from the end, starting with the
        // task that finishes last
        LinkedList<Task> path = new LinkedList<>();
        path.add(0,ldd);

        // keep adding tasks to the path until the first task in the path
        // starts a time 0
        while(startTime(path.getFirst()) != 0) {
            Task cur = path.getFirst();
            int machine = pb.machine(cur.job, cur.task);

            // will contain the task that was delaying the start
            // of our current task
            Optional<Task> latestPredecessor = Optional.empty();

            if(cur.task > 0) {
                // our current task has a predecessor on the job
                Task predOnJob = new Task(cur.job, cur.task -1);

                // if it was the delaying task, save it to predecessor
                if(endTime(predOnJob) == startTime(cur))
                    latestPredecessor = Optional.of(predOnJob);
            }
            if(!latestPredecessor.isPresent()) {
                // no latest predecessor found yet, look among tasks executing on the same machine
                latestPredecessor = IntStream.range(0, pb.numJobs)
                        .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine)))
                        .filter(t -> endTime(t) == startTime(cur))
                        .findFirst();
            }
            // at this point we should have identified a latest predecessor, either on the job or on the machine
            assert latestPredecessor.isPresent() && endTime(latestPredecessor.get()) == startTime(cur);
            // insert predecessor at the beginning of the path
            path.add(0, latestPredecessor.get());
        }
        assert isCriticalPath(path);
        return path;
    }
}
