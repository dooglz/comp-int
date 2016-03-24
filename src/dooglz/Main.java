package dooglz;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        DispatchServer da = new DispatchServer();
       da.Start();
/*
        GenAlgParams params = new GenAlgParams(32, 128, 10, 128, 200, 55, 1024, 5, 119, 3000, 600000);
        GeneticAlgorithm ga = new GeneticAlgorithm(params);
        GenAlgResult res = ga.Start();
        System.out.println("\n########\n" + res.result + "\t Score:" + res.bestScore + "\t Gen: " + res.generation + "\t Time: " + res.runtime);
        da.Stop();
*/
    }
}
