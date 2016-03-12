package dooglz;
import java.util.Comparator;

public class DSolution implements Comparator<DSolution> {
    public int[][] sol;
    public int age;
    public int score;
    public DSolution(int machinecount, int jobcount){
        this.sol = new int[machinecount][jobcount];
    }
    public DSolution(int[][] sol, int machinecount, int jobcount){
        if(sol.length != machinecount || sol[0].length != jobcount){
            throw new IllegalArgumentException();
        }
        this.sol = sol;
    }
    @Override
    public int compare(DSolution a, DSolution b) {
        return a.score - b.score;
    }
}
