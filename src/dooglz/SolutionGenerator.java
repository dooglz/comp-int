package dooglz;

import dooglz.DJSSP;
import modelP.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class SolutionGenerator {
    public static int machinecount;
    public static int jobcount;
    public SolutionGenerator(int machinecount, int jobcount){
        this.machinecount = machinecount;
        this.jobcount = jobcount;
    }

    public DSolution RndGenWeight(DProblem p, float randomness) {
        DSolution solutionArray = new DSolution(machinecount,jobcount);

        for (int i = 0; i < machinecount; i++) {
            for (int j = 0; j < jobcount; j++) {
                solutionArray.sol[i][j] = -1;
            }
        }

        //copy array of jobs
        ArrayList<DJob> sortedjobs = new  ArrayList<DJob>(Arrays.asList(p.jobs));
        //System.arraycopy(p.jobs, 0, sortedjobs, 1, this.jobcount);

        //sort descending by total processing time
        Collections.sort(sortedjobs);
        Collections.reverse(sortedjobs);

        //foreach job
        for (int i = 0; i < sortedjobs.size(); i++) {
            DJob j = sortedjobs.get(i);
            int jid = j.id;
            for (int k = 0; k < this.machinecount; k++) {
                int machine = j.ops[k].machine.id;
                for (int m = 0; m < jobcount; m++) {
                    if (solutionArray.sol[machine][m] == -1) {
                        solutionArray.sol[machine][m] = jid;
                        break;
                    }
                }
            }
        }
        if (randomness > 0.0f) {
            final int swapcount = (int) ((float) (this.machinecount * this.jobcount) * randomness);
            for (int k = 0; k < swapcount; k++) {
                final int mid = (int) (Math.floor(Math.random() * (float) this.machinecount));
                final int j1 = (int) (Math.floor(Math.random() * (float) this.jobcount));
                final int j2 = (int) (Math.floor(Math.random() * (float) this.jobcount));
                if (j1 == j2) {
                    continue;
                }
                int a = solutionArray.sol[mid][j1];
                solutionArray.sol[mid][j1] = solutionArray.sol[mid][j2];
                solutionArray.sol[mid][j2] = a;
            }
        }

       // JSSP.
        solutionArray.age =0;
        solutionArray.Score();
        return solutionArray;
    }
}






























