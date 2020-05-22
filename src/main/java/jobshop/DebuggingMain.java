package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.TabooSolver;

import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the ft06 instance
            Instance instance = Instance.fromFile(Paths.get("instances/la39"));
            long deadline = System.currentTimeMillis() + 1000;
            // build a solution that should be equal to the result of BasicSolver
            TabooSolver TSolver = new TabooSolver(2000,2);
            System.out.println("Dsolver created");
            Result resD = TSolver.solve(instance,deadline);
            System.out.println("Dsolver.solve terminated");
            assert resD.schedule.isValid();
            System.out.println("Dsolver : \n" + resD.schedule.toString());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
