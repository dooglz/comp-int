package dooglz;

import dooglz.DJSSP;
import modelP.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SolutionGenerator {
    public static int[][] RndGenWeight(Problem p, float randomness) {
        int nMachines = p.getNumberOfMachines();
        int nJobs = p.getNumberOfJobs();
        int[][] solutionArray = new int[nMachines][nJobs];

        for (int i = 0; i < nMachines; i++) {
            for (int j = 0; j < nJobs; j++) {
                solutionArray[i][j] = -1;
            }
        }

        //steal array of jobs
        ArrayList<Job> aj = null;
        try {
            Field f = modelP.Problem.class.getDeclaredField("jobs");
            f.setAccessible(true);
            aj = new ArrayList<Job>((ArrayList<Job>) f.get(p));
        } catch (Exception e) {
        }

        //sort descending by total processing time
        Collections.sort(aj, new Comparator<Job>() {
            @Override
            public int compare(Job j1, Job j2) {
                try {
                    Method m = Job.class.getDeclaredMethod("getTotalProcessingTime");
                    m.setAccessible(true);
                    int j1s = (int) m.invoke(j1);
                    int j2s = (int) m.invoke(j2);
                    return j1s < j2s ? 1 : j1s == j2s ? 0 : -1;
                } catch (Exception e) {
                }
                return 0;
            }
        });

        //foreach job
        for (int i = 0; i < aj.size(); i++) {
            Job j = aj.get(i);
            int jid = -1;
            int jopcount = -1;
            try {
                Field f = modelP.Job.class.getDeclaredField("id");
                f.setAccessible(true);
                jid = ((int) f.get(j));
                f = modelP.Job.class.getDeclaredField("operations");
                f.setAccessible(true);
                ArrayList<Operation> ao = ((ArrayList<Operation>) f.get(j));
                jopcount = ao.size();
            } catch (Exception e) {
            }
            for (int k = 0; k < jopcount; k++) {
                int machine = p.getOperationMachineId(jid, k);

                for (int m = 0; m < nJobs; m++) {
                    if (solutionArray[machine][m] == -1) {
                        solutionArray[machine][m] = jid;
                        break;
                    }
                }
            }
        }
        if (randomness > 0.0f) {
            final int swapcount = (int) ((float) (nMachines * nJobs) * randomness);
            for (int k = 0; k < swapcount; k++) {
                final int mid = (int) (Math.floor(Math.random() * (float) nMachines));
                final int j1 = (int) (Math.floor(Math.random() * (float) nJobs));
                final int j2 = (int) (Math.floor(Math.random() * (float) nJobs));
                if (j1 == j2) {
                    continue;
                }
                int a = solutionArray[mid][j1];
                solutionArray[mid][j1] = solutionArray[mid][j2];
                solutionArray[mid][j2] = a;
            }
        }

        return solutionArray;
    }
}






























