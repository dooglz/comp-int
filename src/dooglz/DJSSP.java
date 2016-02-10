package dooglz;

import modelP.Problem;
import java.io.PrintStream;

//Class to add functionality to the JSSP library

public class DJSSP {

    public static String saveSolution(int[][] solution, Problem problem, String filename) {
        filename += System.currentTimeMillis() + ".txt";

        try {
            PrintStream e = new PrintStream(filename);
            String str = "" + problem.getId();

            for (int i = 0; i < solution.length; ++i) {
                int[] machineOps = solution[i];
                str = str + "\r\n";

                for (int j = 0; j < machineOps.length; ++j) {
                    str = str + machineOps[j];
                    if (j < machineOps.length - 1) {
                        str = str + ",";
                    }
                }
            }

            e.println(str);
            e.flush();
            e.close();
            return filename;
        } catch (Exception var8) {
            System.err.println("Error Writing File");
            return null;
        }
    }

}
