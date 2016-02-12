package dooglz.Server;

import static dooglz.Constants.*;

import dooglz.Command;
import dooglz.DJSSP;
import dooglz.SolutionGenerator;
import modelP.JSSP;
import modelP.Problem;
import modelP.Solution;
import org.java_websocket.WebSocketImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {

    public static Problem problem;
    public static Net s;
    public static int[][][] population;

    public enum States {
        Lobby, PreGen, Distrubuting, Collecting
    }

    public static States state;

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Hello WProblemorld Server!");

        WebSocketImpl.DEBUG = true;
        int port = SERVER_PORT;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        s = new Net(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());
        CmdLine cmd = new CmdLine();
        cmd.s = s;
        cmd.start();

        state = States.Lobby;
    }

    public static void Load(int id) {
        if (id == 0) {
            id = ((int) (Math.random() * 141.0)) + 1;
        }
        problem = JSSP.getProblem(id);
        state = States.PreGen;
        int[][] solution = SolutionGenerator.RndGenWeight(problem, 0.3f);

        JSSP.printSolution(solution, problem);
        String filename = DJSSP.saveSolution(solution, problem, SOL_DIR);
        JSSP.displaySolution(filename);

        population = new int[MAIN_POP_SIZE][problem.getNumberOfMachines()][problem.getNumberOfJobs()];
        for (int i = 0; i < MAIN_POP_SIZE / 2; i++) {
            population[i] = SolutionGenerator.RndGenWeight(problem, i / (float) (MAIN_POP_SIZE / 2));
        }
        for (int i = 0; i < MAIN_POP_SIZE / 2; i++) {
            population[i] = JSSP.getRandomSolution(problem);
        }
        //time to distribute
        s.sendToAll( new Command(System.currentTimeMillis(), false, "Start", Proto.gson.toJson(population), ""));
        state = States.Distrubuting;
    }

    public static void Stop() {
        s.sendToAll( new Command(System.currentTimeMillis(), false, "Stop", "", ""));
        state = States.Lobby;
    }
}
