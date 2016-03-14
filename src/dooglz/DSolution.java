package dooglz;
import modelP.JSSP;

import java.util.Comparator;

public class DSolution implements Comparator<DSolution> ,Comparable<DSolution> {
    public int[][] sol;
    public int age;
    private int score_;
    public DSolution(int machinecount, int jobcount){
        this.sol = new int[machinecount][jobcount];
        this.score_ = -1;
    }
    public DSolution(int[][] sol, int machinecount, int jobcount){
        if(sol.length != machinecount || sol[0].length != jobcount){
            throw new IllegalArgumentException();
        }
        this.sol = sol;
        this.score_ = -1;
    }
    @Override
    public int compare(DSolution a, DSolution b) {
        return  b.Score() - a.Score();
    }
    public int Score(){
        if (this.score_ != -1){
            return this.score_;
        }else{
            this.score_ =  JSSP.getFitness(this.sol,Main.problem.pProblem);
        }
        return this.score_;
    }

    @Override
    public int compareTo(DSolution o) {
       // return this.Score() - o.Score();
        return Integer.compare( o.Score(),this.Score());
    }
}
