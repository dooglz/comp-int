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

    public DSolution RndGenByOpId(DProblem p, float randomness) {
        DSolution newSol = new DSolution(machinecount,jobcount);
        for (int m = 0; m < machinecount; m++) {
            ArrayList<Integer> jobs = new ArrayList<>();
            for (int j = 0; j < jobcount; j++) {
                jobs.add(j);
            }
            final int mm =m;
            Collections.sort(jobs, (left, right) -> {
                int op1 = Main.problem.jobs[left].GetOperationOnMachine(Main.problem.machines[mm]).id;
                int op2 = Main.problem.jobs[right].GetOperationOnMachine(Main.problem.machines[mm]).id;
                return op1 - op2;
            });
            Collections.rotate(jobs,(int)Math.floor((-5.0 * randomness * Math.random())));
            for (int j = 0; j < jobcount; j++) {
                newSol.sol[m][j] = jobs.get(j);
            }
        }
        return newSol;
    }

    public DSolution RndGenByTime(DProblem p, float randomness, int rotate) {
        DSolution solutionArray = new DSolution(machinecount,jobcount);

        for (int i = 0; i < machinecount; i++) {
            for (int j = 0; j < jobcount; j++) {
                solutionArray.sol[i][j] = -1;
            }
        }

        //copy array of jobs
        ArrayList<DJob> sortedjobs = new ArrayList<DJob>(Arrays.asList(p.sortedjobs));
        Collections.rotate(sortedjobs, rotate);
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
        solutionArray.Score(true);
        return solutionArray;
    }
}






























