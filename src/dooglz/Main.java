package dooglz;
import static dooglz.Constants.*;
import java.io.IOException;
import modelP.JSSP;
import static dooglz.Constants.MAIN_POP_SIZE;

public class Main {
    public static DProblem problem;
    public static DSolution population[];
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Hello World!");
        modelP.Problem mpp = JSSP.getProblem(100);

        problem = new DProblem(mpp);
        SolutionGenerator sg = new SolutionGenerator(problem.machineCount,problem.jobCount);
        Tournament tournament = new Tournament(problem.machineCount,problem.jobCount);
        population = new DSolution[MAIN_POP_SIZE];

        DSolution t = new DSolution(JSSP.getRandomSolution(mpp), problem.machineCount, problem.jobCount);
        System.out.println(t.Score(true));
        t.MakeFeasible();
        System.out.println(t.Score(true));
    /*

        for (int i = 0; i < MAIN_POP_SIZE / 2; i++) {
            if(i < 20){
                population[i] = sg.RndGenByOpId(problem, ((float)i) / 20.0f);
            }
            population[i] = sg.RndGenByTime(problem, (float)i / (float) (MAIN_POP_SIZE / 2), i);
        }

        for (int i = MAIN_POP_SIZE / 2; i < MAIN_POP_SIZE; i++) {
            population[i] = new DSolution(JSSP.getRandomSolution(mpp), problem.machineCount, problem.jobCount);
        }
*/

        for (int i = 0 ; i < MAIN_POP_SIZE; i++) {
            population[i] = new DSolution(JSSP.getRandomSolution(mpp), problem.machineCount, problem.jobCount);
        }
        tournament.Churn(population,problem,10512);
        int a = 6;
    }
}
