package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.io.IOException;
import java.nio.file.Paths;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the ft06 instance
            Instance instance = Instance.fromFile(Paths.get("instances/ft06"));
            int j,i,totaltime=0;
            for(i=0;i<instance.numJobs;i++){
                for(j=0;j<instance.numTasks;j++){
                    totaltime+=instance.duration(i,j);
                }
            }
            System.out.print(totaltime);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
