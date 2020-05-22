package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.GreedySolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class EncodingTests {

    @Test
    public void testJobNumbers() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        // TODO: make it print something meaningful
        // by implementing the toString() method
        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 12;



        // numéro de jobs : 1 1 2 2 1 2
        enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        sched = enc.toSchedule();
        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 14;
    }

    @Test
    public void testmyResourceOrder() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        myResourceOrder enc = new myResourceOrder(instance);
        enc.order[0][0] = new Task(0,0);
        enc.order[0][1] = new Task(1,1);
        enc.order[1][0] = new Task(1,0);
        enc.order[1][1] = new Task(0,1);
        enc.order[2][0] = new Task(0,2);
        enc.order[2][1] = new Task(1,2);

        Schedule sched = enc.toSchedule();

        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 12;

        enc = new myResourceOrder(instance);
        enc.order[0][0] = new Task(0,0);
        enc.order[0][1] = new Task(1,1);
        enc.order[1][0] = new Task(0,1);
        enc.order[1][1] = new Task(1,0);
        enc.order[2][0] = new Task(0,2);
        enc.order[2][1] = new Task(1,2);
        sched = enc.toSchedule();
        System.out.println(sched);
        assert sched.isValid();
        assert sched.makespan() == 14;

    }

    @Test
    public void testSchedule() throws  IOException{
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
        int[][] times = new int[instance.numJobs][instance.numTasks] ;
        times[0][0] = 0 ;
        times[0][1] = 3 ;
        times[0][2] = 6 ;
        times[1][0] = 0 ;
        times[1][1] = 3 ;
        times[1][2] = 8 ;
        Schedule schedule = new Schedule(instance, times) ;

        JobNumbers jn = schedule.toJobNumbers() ;
        System.out.println(jn);
        System.out.println();

        myResourceOrder myro = schedule.toMyResourceOrder() ;
        System.out.println(myro) ;

        ResourceOrder ro = schedule.toResourceOrder() ;
        System.out.println(ro) ;
    }

    @Test
    public void testSchedule2() throws  IOException{
        Instance instance = Instance.fromFile(Paths.get("instances/aaa2"));
        int[][] times = new int[instance.numJobs][instance.numTasks] ;
        times[0][0] = 21 ;
        times[0][1] = 25 ;
        times[0][2] = 28 ;
        times[1][0] = 0 ;
        times[1][1] = 7 ;
        times[1][2] = 25 ;
        times[2][0] = 0 ;
        times[2][1] = 7 ;
        times[2][2] = 17 ;
        Schedule schedule = new Schedule(instance, times) ;

        JobNumbers jn = schedule.toJobNumbers() ;
        System.out.println(jn);
        System.out.println();

        myResourceOrder myro = schedule.toMyResourceOrder() ;
        System.out.println(myro) ;

        ResourceOrder ro = schedule.toResourceOrder() ;
        System.out.println(ro) ;
    }

    @Test
    public void testGreedySolver() throws IOException{
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        myResourceOrder enc = new myResourceOrder(instance);
        enc.order[0][0] = new Task(1,1);
        enc.order[0][1] = new Task(0,0);
        enc.order[1][0] = new Task(1,0);
        enc.order[1][1] = new Task(0,1);
        enc.order[2][0] = new Task(0,2);
        enc.order[2][1] = new Task(1,2);

        Schedule sched = enc.toSchedule();
        assert sched.isValid();

        Solver solver = new GreedySolver("SPT") ;
        Result result = solver.solve(instance, System.currentTimeMillis() + 10) ;
        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan

        solver = new GreedySolver("LRPT") ;
        result = solver.solve(instance, System.currentTimeMillis() + 10) ;
        assert result.schedule.isValid();
    }

    @Test
    public void testGreedySolverInstance() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/ft06"));

        Solver solver = new GreedySolver("SPT") ;
        Result result = solver.solve(instance, System.currentTimeMillis() + 10) ;
        assert result.schedule.isValid();
        System.out.println( result.schedule.makespan() );

        solver = new GreedySolver("LRPT") ;
        result = solver.solve(instance, System.currentTimeMillis()+10) ;
        assert result.schedule.isValid();
        System.out.println( result.schedule.makespan() );

        solver = new GreedySolver("EST_SPT") ;
        result = solver.solve(instance, System.currentTimeMillis()+10) ;
        assert result.schedule.isValid();
        System.out.println( result.schedule.makespan() );

        solver = new GreedySolver("EST_LRPT") ;
        result = solver.solve(instance, System.currentTimeMillis()+10) ;
        assert result.schedule.isValid();
        System.out.println( result.schedule.makespan() );
    }

    @Test
    public void testBasicSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }

}
