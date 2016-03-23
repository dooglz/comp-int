package dooglz.Client;

import static dooglz.Constants.*;

import modelP.Problem;
import org.java_websocket.drafts.Draft_17;

import java.net.URI;
import java.net.URISyntaxException;


public class Client {
    public static int[][][] population;
    public static Problem problem;

    public enum States {
        Connecting, Connected, Running
    }

    public static States state;

    public static void main(String[] args) {
        population = null;
        problem = null;
        state = States.Connecting;
        System.out.println("Hello World Client!");
        Net c = null;
        final String uri = "ws://" + DEF_HOST + ":" + SERVER_PORT;
        while (c == null || c.getConnection() == null || !c.getConnection().isOpen()) {
            System.out.println("Connecting to " + uri);
            try {
                c = new Net(new URI(uri), new Draft_17());
            } catch (URISyntaxException e) {
                System.out.println(e);
                return;
            }

            c.connect();

            try {
                Thread.sleep(CLIENT_RETRY_INTERVAL);
            } catch (InterruptedException e) {
            }
        }
        state = States.Connected;
        while (c.getConnection().isOpen()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Client Shutting down");
/*
        //Get a Problem instance using the static JSSP class in the range [1-142]
        Problem problem = JSSP.getProblem(86);

        //print problem to std.out
        JSSP.printProblem(problem);
        // Get a randomly initialised solution for the problem
        int[][] solution = JSSP.getRandomSolution(problem);
        //print to std.out (also prints fitness)
        JSSP.printSolution(solution, problem);
        int fitness = JSSP.getFitness(solution, problem);
        System.out.println("Fitness = " + fitness);
        String filename = DJSSP.saveSolution(solution, problem, SOL_DIR);
        int[][] solution2 = JSSP.loadSolution(filename);
        int id = JSSP.getProblemIdFromSolution(filename);
        Problem problem2 = JSSP.getProblem(id);
        JSSP.printSolution(solution2, problem2);

        JSSP.displaySolution(filename);

        int populationSize = 100;
        int[][][] population = new int[populationSize][problem.getNumberOfMachines()][problem.getNumberOfJobs()];
        for (int i = 0; i < 100; i++) {
            population[i] = JSSP.getRandomSolution(problem);
        }
        */
    }

    public static void Start(){
        if(population == null || problem == null){
            System.out.println("Params not ready for start");
            return;
        }
        while( true) {
           // GeneticAlgorithm.Churn(population, problem, 100);
        }
    }

}




















