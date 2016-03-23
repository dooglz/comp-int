package dooglz;
import static dooglz.Constants.*;
import java.io.IOException;
import modelP.JSSP;
import static dooglz.Constants.MAIN_POP_SIZE;

public class Main {
    public static DProblem problem;
    public static DSolution population[];
    public static void main(String[] args) throws InterruptedException, IOException {
        modelP.Problem mpp = JSSP.getProblem(PROBLEM_ID);
        problem = new DProblem(mpp);
        SolutionGenerator sg = new SolutionGenerator(problem.machineCount,problem.jobCount);
        GeneticAlgorithm ga = new GeneticAlgorithm(problem.machineCount,problem.jobCount);

        population = new DSolution[MAIN_POP_SIZE];
        System.out.println("Problem "+PROBLEM_ID+" Lb:"+problem.lb+" popsize: "+MAIN_POP_SIZE);
        DSolution t = new DSolution(JSSP.getRandomSolution(mpp), problem.machineCount, problem.jobCount);

        GenAlgResult res =  ga.Start(population,problem,10512);
        System.out.println(res.result+"\t Score:"+res.bestScore+"\t Gen: "+res.generation+"\t Time: "+res.runtime);
    }
}
