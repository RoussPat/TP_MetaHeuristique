package jobshop;

import java.io.IOException;

public interface Solver {

    Result solve(Instance instance, long deadline) throws IOException;

}
