package dooglz;
import static dooglz.Constants.*;
import modelP.JSSP;
import modelP.Problem;


public class Client {

    public static void main(String[] args) {
        System.out.println("Hello World Client!");

        /**
         * Get a Problem instance using the static JSSP class in the range [1-142]
         */
        Problem problem = JSSP.getProblem(86);

        //print problem to std.out
        JSSP.printProblem(problem);

        /**
         * Get a randomly initialised solution for the problem
         */
        int[][] solution = JSSP.getRandomSolution(problem);

        //print to std.out (also prints fitness)
        JSSP.printSolution(solution, problem);

        /**
         * Check the solution and return its fitness (invalid solutions return Integer.MaxValue)
         */
        int fitness = JSSP.getFitness(solution, problem);
        System.out.println("Fitness = " + fitness);

        /**
         * Save the solution (defaults to the project directory saving to a .txt file prefixed with the current computer time in milliseconds)
         * The filename is returned
         */
        String filename = DJSSP.saveSolution(solution, problem,SOL_DIR);

        /**
         * load solution from default directory
         */
        int[][] solution2 = JSSP.loadSolution(filename);

        /**
         * get the problem Id from the saved solution
         */
        int id = JSSP.getProblemIdFromSolution(filename);
        /**
         * load the associated problem
         */
        Problem problem2 = JSSP.getProblem(id);

        /**
         * Check the solution and print to std.out
         */
        JSSP.printSolution(solution2, problem2);

        /**
         * Display a saved solution graphically
         */
        JSSP.displaySolution(filename);

        /**
         * Create population
         */

        int populationSize = 100;
        int[][][] population = new int[populationSize][problem.getNumberOfMachines()][problem.getNumberOfJobs()];
        for (int i = 0; i < 100; i++) {
            population[i] = JSSP.getRandomSolution(problem);
        }

    }
}
