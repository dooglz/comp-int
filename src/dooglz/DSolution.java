package dooglz;

import modelP.JSSP;

import java.io.Console;
import java.security.spec.DSAGenParameterSpec;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DSolution implements Comparator<DSolution>, Comparable<DSolution> {
    public int[][] sol;
    public int age;
    public DProblem prob;
    private int score_;

    public DSolution(DProblem prob, int machinecount, int jobcount) {
        this.prob = prob;
        this.sol = new int[machinecount][jobcount];
        this.score_ = -1;
    }

    public DSolution(DProblem prob, int[][] sol, int machinecount, int jobcount) {
        this.prob = prob;
        if (sol.length != machinecount || sol[0].length != jobcount) {
            throw new IllegalArgumentException();
        }
        this.sol = sol;
        this.score_ = -1;
    }

    public static boolean isEqual(DSolution a, DSolution b) {
        for (int i = 0; i < a.sol.length; i++) {
            for (int j = 0; j < a.sol[i].length; j++) {
                if (a.sol[i][j] != b.sol[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int compare(DSolution a, DSolution b) {
        return a.Score(false) - b.Score(false);
    }

    public int Score(boolean recalc) {
        if (this.score_ != -1 && !recalc) {
            return this.score_;
        } else {
            this.score_ = JSSP.getFitness(this.sol, prob.pProblem);
        }
        return this.score_;
    }

    @Override
    public int compareTo(DSolution o) {
        // return this.Score() - o.Score();
        return Integer.compare(this.Score(false), o.Score(false));
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
            int op1 = prob.jobs[left].GetOperationOnMachine(prob.machines[m]).id;
            int op2 = prob.jobs[right].GetOperationOnMachine(prob.machines[m]).id;
            return op1 - op2;
        });
        for (int i = 0; i < missingFrom.size(); i++) {
            this.sol[m][holes.get(i)] = missingFrom.get(i);
        }
    }

    static DSolution getRand(DProblem prob, boolean optimised, int searchspace, int goal) {
        if (searchspace < 1) {
            searchspace = 1;
        }
        DSolution ss[] = new DSolution[searchspace];
        for (int i = 0; i < searchspace; i++) {
            ss[i] = new DSolution(prob, JSSP.getRandomSolution(prob.pProblem), prob.machineCount, prob.jobCount);
            if (optimised) {
                ss[i].MakeFeasible();
            }
            if (ss[i].Score(true) <= goal) {
                System.out.println("yolo");
                return ss[i];
            }
        }
        Arrays.sort(ss);
        return ss[0];
    }

    public boolean IsFeasible() {
        for (int m = 0; m < this.sol.length; m++) {
            int lastop = -1;
            for (int j = 0; j < this.sol[m].length; j++) {
                final int op = prob.jobs[j].GetOperationOnMachine(prob.machines[m]).id;
                if (op < lastop) {
                    System.out.println("false");
                    return false;
                }
                lastop = op;
            }
        }
        return true;
    }

    public void MakeFeasible() {

        int[][] solcpy = new int[this.sol.length][this.sol[0].length];
        int[][] schedule = new int[solcpy.length][solcpy[0].length];
        for (int i = 0; i < this.sol.length; i++) {
            for (int j = 0; j < this.sol[0].length; j++) {
                solcpy[i][j] = this.sol[i][j];
                schedule[i][j] = -1;
            }
        }


        //for each 'tick;
        for (int i = 0; i < solcpy[0].length; i++) {

            List<Integer> nothappy = IntStream.rangeClosed(0, solcpy.length - 1).boxed().collect(Collectors.toList());
            int prev = 0;
            int searchahead = 0;
            while (nothappy.size() > 0) {
                boolean canLookahead = false;
                boolean failthrough = false;
                if (prev == nothappy.size()) {
                    canLookahead = true;
                    searchahead++;
                    if (searchahead + i >= solcpy[0].length) {
                        failthrough = true;
                    }
                } else {
                    searchahead = 0;
                }
                prev = nothappy.size();

                for (int m = 0; m < solcpy.length; m++) {
                    //just incase we've already scheduled this
                    if (schedule[m][i] != -1) {
                        continue;
                    }
                    //what does our solution say?
                    int job = solcpy[m][i];

                    if (failthrough) {
                        //oh dear
                        schedule[m][i] = job;
                        nothappy.remove(Integer.valueOf(m));
                        canLookahead = false;
                        continue;
                    } else if (canLookahead) {
                        job = solcpy[m][i + searchahead];
                    }
                    //what operation is that
                    final int op = prob.jobs[job].GetOperationOnMachine(prob.machines[m]).id;
                    boolean happy = false;
                    if (op == 0 || i == solcpy[0].length - 1) { //TODO check that range
                        happy = true;
                    } else {
                        //has the op before this been scheduled, in this batch?
                        final int machineForPreviousOP = prob.jobs[job].ops[op - 1].machine.id;
                        if (solcpy[machineForPreviousOP][i] == job) {
                            happy = true;
                        } else {
                            //in a previous batch?
                            for (int j = 0; j < i; j++) {
                                if (schedule[machineForPreviousOP][j] == job) {
                                    happy = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (happy) {
                        schedule[m][i] = job;
                        nothappy.remove(Integer.valueOf(m));
                        canLookahead = false;
                        if (searchahead > 0) {
                            solcpy[m][i + searchahead] = -1;
                            System.arraycopy(solcpy[m], i, solcpy[m], i + 1, searchahead);
                        }
                        solcpy[m][i] = -1;
                    }

                }
            }
        }
        for (int i = 0; i < this.sol.length; i++) {
            for (int j = 0; j < this.sol[0].length; j++) {
                this.sol[i][j] = schedule[i][j];
            }
        }

    }
}
