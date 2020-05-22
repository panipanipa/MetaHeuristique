package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.util.Arrays;
import java.util.HashSet;

import static java.lang.Integer.max;


public class myResourceOrder extends Encoding {

    public final Task[][] order;
    public int nextToSet = 0;

    //tuple -> (job, task)
    public myResourceOrder(Instance instance) {
        super(instance);

        this.order = new Task[instance.numMachines][instance.numJobs];
        for (int machine = 0; machine < instance.numMachines; machine++) {
            for (int job = 0; job < instance.numJobs; job++) {
                order[machine][job] = new Task(-1, -1);
            }
        }
    }

    @Override
    public String toString() {
        String s = "";
        for(int machine=0; machine<instance.numMachines; machine++) {
            s += "machine " + machine + ":\n";
            for(int i=0; i<instance.numJobs; i++) {
                s += order[machine][i] + "\n" ;
            }
        }
        return s ;
    }

    @Override
    public Schedule toSchedule() {
        //list task add to a schedule
        HashSet<Task> isSchedul = new HashSet<Task>() ;

        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];
        int[] startmachine = new int[instance.numMachines] ;
        Arrays.fill(startmachine, 0) ;
        boolean finish = false ;
        int taille =  instance.numJobs * instance.numTasks ;

        //pointeur
        int[] pointeur = new int[instance.numMachines] ;
        Arrays.fill(pointeur, 0) ;

        while (!finish) {
            for (int machine=0; machine<instance.numMachines;machine++) {
                if (pointeur[machine] < instance.numJobs && order[machine][pointeur[machine]].task == 0) {
                    isSchedul.add(order[machine][pointeur[machine]]);
                    startTimes[order[machine][pointeur[machine]].job][order[machine][pointeur[machine]].task] = startmachine[machine] ;
                    startmachine[machine] += instance.duration(order[machine][pointeur[machine]].job, order[machine][pointeur[machine]].task) ;
                    pointeur[machine] += 1;
                }
                else if (pointeur[machine] < instance.numJobs && isSchedul.contains(new Task(order[machine][pointeur[machine]].job, order[machine][pointeur[machine]].task - 1))) {
                    int j = order[machine][pointeur[machine]].job ;
                    int t = order[machine][pointeur[machine]].task ;
                    startTimes[j][t] = max(startmachine[machine], startTimes[j][t-1]+instance.duration(j,t-1)) ;
                    startmachine[machine] = startTimes[j][t]+ instance.duration(j,t) ;
                    isSchedul.add(order[machine][pointeur[machine]]);
                    pointeur[machine] += 1;
                }
            }
            finish = isSchedul.size()==taille ;
        }

        return new Schedule(instance, startTimes) ;
    }
}

