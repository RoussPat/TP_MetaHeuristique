package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.GreedySolver;
import jobshop.solvers.TabooSolver;
import org.junit.Test;
import sun.awt.X11.XSystemTrayPeer;

import java.io.IOException;
import java.nio.file.Paths;

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
        System.out.println("Schedule de TestJobNumber avec toString : " + sched + " fin ") ;
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
        assert sched.isValid();
        assert sched.makespan() == 14;
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

    @Test
    public void testToSchedule() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        ResourceOrder enc = new ResourceOrder(instance);

        enc.tasksByMachine[0][0] = new Task(0,0);
        enc.tasksByMachine[0][1] = new Task(1,1);
        enc.tasksByMachine[1][0] = new Task(1,0);
        enc.tasksByMachine[1][1] = new Task(0,1);
        enc.tasksByMachine[2][0] = new Task(0,2);
        enc.tasksByMachine[2][1] = new Task(1,2);

        Schedule sched = enc.toSchedule();
        System.out.println("Schedule de TestToSchedule : " + sched + " fin ") ;
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }
/*
    @Test
    public void testGreedySolverft06() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/ft06"));
        long deadline = System.currentTimeMillis() + 1000000000;
        // build a solution that should be equal to the result of BasicSolver
        GreedySolver solverSPT = new GreedySolver(0);
        System.out.println("SolverSPT created");
        Result resSPT = solverSPT.solve(instance,deadline);
        System.out.println("SolverSPT.solve terminated");
        assert resSPT.schedule.isValid();
        System.out.println("resSPT : \n" + resSPT.schedule.toString());

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);
        System.out.println("Solution du Basic Solver" + result.schedule);
        assert result.schedule.isValid();
        //assert result.schedule.makespan() == resSPT.schedule.makespan(); // should have the same makespan
        //assert result.schedule.makespan() == resLRPT.schedule.makespan(); // should have the same makespan
    }*/
    @Test
    public void DescentSolver_la01() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/la01"));
        long deadline = System.currentTimeMillis() + 1000;
        // build a solution that should be equal to the result of BasicSolver
        DescentSolver Dsolver = new DescentSolver(0);
        System.out.println("Dsolver created");
        Result resD = Dsolver.solve(instance,deadline);
        System.out.println("Dsolver.solve terminated");
        assert resD.schedule.isValid();
        System.out.println("Dsolver : \n" + resD.schedule.toString());

    }
    @Test
    public void TabouSolver200_20_la40() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/la40"));
        long deadline = System.currentTimeMillis() + 1000;
        // build a solution that should be equal to the result of BasicSolver
        TabooSolver TSolver = new TabooSolver(200,20);
        System.out.println("Dsolver created");
        Result resD = TSolver.solve(instance,deadline);
        System.out.println("Dsolver.solve terminated");
        assert resD.schedule.isValid();
        System.out.println("Dsolver : \n" + resD.schedule.toString());

    }
    @Test
    public void TabouSolver2000_20_ft06() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/ft10"));
        long deadline = System.currentTimeMillis() + 1000;
        // build a solution that should be equal to the result of BasicSolver
        TabooSolver TSolver = new TabooSolver(2000,20);
        System.out.println("Dsolver created");
        Result resD = TSolver.solve(instance,deadline);
        System.out.println("Dsolver.solve terminated");
        assert resD.schedule.isValid();
        System.out.println("Dsolver : \n" + resD.schedule.toString());

    }
    @Test
    public void TabouSolver20000_20_la40() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/la40"));
        long deadline = System.currentTimeMillis() + 1000;
        // build a solution that should be equal to the result of BasicSolver
        TabooSolver TSolver = new TabooSolver(20000,20);
        System.out.println("Dsolver created");
        Result resD = TSolver.solve(instance,deadline);
        System.out.println("Dsolver.solve terminated");
        assert resD.schedule.isValid();
        System.out.println("Dsolver : \n" + resD.schedule.toString());

    }
    @Test
    public void TabouSolver20000_10_ft06() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/ft10"));
        long deadline = System.currentTimeMillis() + 1000;
        // build a solution that should be equal to the result of BasicSolver
        TabooSolver TSolver = new TabooSolver(20000,10);
        System.out.println("Dsolver created");
        Result resD = TSolver.solve(instance,deadline);
        System.out.println("Dsolver.solve terminated");
        assert resD.schedule.isValid();
        System.out.println("Dsolver : \n" + resD.schedule.toString());

    }
}
