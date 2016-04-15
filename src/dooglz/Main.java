package dooglz;

import modelP.JSSP;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class worker extends Thread {
    public GeneticAlgorithm ga;
    private boolean b;
    public void Stop(){
        b = false;
        ga.HandleCmd("stop");
    }
    @Override
    public void run() {
        b = true;
        final long id = currentThread().getId();
        while (b) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            //get job
            System.out.println("Worker " + id + " getting new job");
            workOrder wo = null;
            wo = workOrder.GetFromDispatch();
            System.out.println("Worker " + id + " new work order " + wo.dispatchID + " " + wo.params.problemID);

            //process
            ga = new GeneticAlgorithm(wo.params);
            GenAlgResult res = ga.Start();

            workResponce wr = new workResponce();
            wr.dispatchID = wo.dispatchID;
            wr.result = res;
            workResponce.sendToServer(wr);
        }
    }
}

public class Main {
    public static String ip = "0.0.0.0";

    public static void main(String[] args) throws InterruptedException, IOException {
        boolean serveMode = false;
        boolean workerMode = false;
        int t = 0;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-server":
                    serveMode = true;
                    workerMode = false;
                    break;
                case "-worker":
                    serveMode = false;
                    workerMode = true;
                    break;
                case "-t":
                    t = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "-ip":
                    ip = args[i + 1];
                    i++;
                    break;
                default:
                    System.out.println("unknown param " + args[i]);
            }
        }

        worker[] threads = null;
        DispatchServer da = null;

        if (serveMode) {
            da = new DispatchServer();
            da.Start();
        } else if (workerMode) {
            if (t < 1) {
                t = Runtime.getRuntime().availableProcessors();
            }
            threads = new worker[t];
            for (worker w : threads) {
                w = new worker();
                w.start();
            }
        }else{
            //passthrough Mode
            int pid = 1;
            da = new DispatchServer();
            GenAlgParams params = da.getBestforPid(pid);
            params.maxTime = 120000;
            //params.goal = 55;
            params.seedRange = Math.min(params.seedRange,20);
           // params.popsize = Math.min(params.popsize,800);
            //GenAlgParams params = new GenAlgParams(32, 128, 10, 128, 200, 55, 1024, 5, 119, 3000, 600000);
            GeneticAlgorithm ga = new GeneticAlgorithm(params);
            GenAlgResult res = ga.Start();
            System.out.println("\n########\n" + res.result + "\t Score:" + res.bestScore + "\t Gen: " + res.generation + "\t Time: " + res.runtime);

            //DProblem Dprob = new DProblem();
            //DSolution ds = new DSolution(Dprob,Dprob.machineCount,Dprob.jobCount);
             //ds = DSolution.getRand(Dprob,false,0,0);


            modelP.Problem pp =  JSSP.getProblem(params.problemID);
            JSSP.printSolution(res.sol.sol,pp);
            String filename = JSSP.saveSolution(res.sol.sol,pp);
            JSSP.displaySolution(filename);


        }
        System.out.println(" ... and we are off to the races");
        boolean loop  =true;
        try {
            while(loop) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String input;
                while (loop && (input = br.readLine()) != null) {
                    switch (input.toLowerCase()){
                        case "quit":
                            loop = false;
                            break;
                        case "exit":
                            loop = false;
                            break;
                        default:
                            if(serveMode){
                                da.HandleCmd(input);
                            }
                            if(workerMode){
                                for (worker w : threads){
                                    w.ga.HandleCmd(input);
                                }
                            }
                            break;
                    }
                }
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
        if(serveMode){
            da.Stop();
        }
        if(workerMode){
            for (worker w : threads){
                w.Stop();
            }
        }
/*

        Thread.sleep(200);
        workOrder wo = workOrder.GetFromDispatch();
        System.out.println("new work order: " + wo.dispatchID + " " + wo.params.problemID);

        Thread.sleep(500);
        workResponce wr = new workResponce();
        wr.dispatchID = wo.dispatchID;
        wr.result = new GenAlgResult("done", 66, 70, 1012);
        wr.sendToServer();
        */
/*
        GenAlgParams params = new GenAlgParams(32, 128, 10, 128, 200, 55, 1024, 5, 119, 3000, 600000);
        GeneticAlgorithm ga = new GeneticAlgorithm(params);
        GenAlgResult res = ga.Start();
        System.out.println("\n########\n" + res.result + "\t Score:" + res.bestScore + "\t Gen: " + res.generation + "\t Time: " + res.runtime);
        da.Stop();
*/
    }
}
