package dooglz;

import modelP.JSSP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DSolution implements Comparator<DSolution>, Comparable<DSolution> {
    public int[][] sol;
    public int age;
    private int score_;

    public DSolution(int machinecount, int jobcount) {
        this.sol = new int[machinecount][jobcount];
        this.score_ = -1;
    }

    public DSolution(int[][] sol, int machinecount, int jobcount) {
        if (sol.length != machinecount || sol[0].length != jobcount) {
            throw new IllegalArgumentException();
        }
        this.sol = sol;
        this.score_ = -1;
    }

    @Override
    public int compare(DSolution a, DSolution b) {
        return b.Score(false) - a.Score(false);
    }

    public int Score(boolean recalc) {
        if (this.score_ != -1 && !recalc) {
            return this.score_;
        } else {
            this.score_ = JSSP.getFitness(this.sol, Main.problem.pProblem);
        }
        return this.score_;
    }

    @Override
    public int compareTo(DSolution o) {
        // return this.Score() - o.Score();
        return Integer.compare(o.Score(false), this.Score(false));
    }

    public void FixPermutation(int m) {
        List<Integer> sa1 = IntStream.of(this.sol[m]).boxed().collect(Collectors.toList());
        //AL of jobs that are not in the array
        ArrayList<Integer> missingFrom = new ArrayList<>();
        //AL of positions of duplicates
        ArrayList<Integer> holes = new ArrayList<>();
        for (int i = 0; i < this.sol[m].length; i++) {
            missingFrom.add(i);
        }
        for (int i = 0; i < this.sol[m].length; i++) {
            Integer i1 = this.sol[m][i];
            if (Collections.frequency(sa1, i1) > 1) {
                holes.add(i);
                sa1.set(i, -1);
            }
            missingFrom.remove(i1);
        }

        //Collections.sort(missingFrom1);
        //replace missing jobs back on the list, the job with the
        //lowest operation ID on this machine gets priority
        Collections.sort(missingFrom, (left, right) -> {
            int op1 = Main.problem.jobs[left].GetOperationOnMachine(Main.problem.machines[m]).id;
            int op2 = Main.problem.jobs[right].GetOperationOnMachine(Main.problem.machines[m]).id;
            return op1 - op2;
        });
        for (int i = 0; i < missingFrom.size(); i++) {
            this.sol[m][holes.get(i)] = missingFrom.get(i);
        }
    }

    public void MakeFeasible(){
     //for each machine
        //schedule up the next job
        //Can it be scheduled yet?
            //yes - schedule
            //no - get next one then come back to this
        
    }
}
