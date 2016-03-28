package dooglz;

import modelP.JSSP;

import java.io.IOException;

class worker extends Thread {

    @Override
    public void run() {
        final long id = currentThread().getId();
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            //get job
            System.out.println("Worker " + id + " getting new job");
            workOrder wo = null;
            wo = workOrder.GetFromDispatch();
            System.out.println("Worker " + id + " new work order " + wo.dispatchID + " " + wo.params.problemID);

            //process
            GeneticAlgorithm ga = new GeneticAlgorithm(wo.params);
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
        boolean workerMode =false;
        int t = 0;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-server":
                    serveMode = true;
                    workerMode = false;
                    break;
                case "-worker":
                    serveMode = false;
                    workerMode =true;
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

        if (serveMode) {
            DispatchServer da = new DispatchServer();
            da.Start();
        } else if(workerMode) {
            if (t < 1) {
                t = Runtime.getRuntime().availableProcessors();
            }
            worker[] threads = new worker[t];
            for (worker w : threads) {
                w = new worker();
                w.start();
            }
        }else{
            //passthrough

            DProblem Dprob = new DProblem(JSSP.getProblem(101));
            DSolution ds = new DSolution(Dprob,Dprob.machineCount,Dprob.jobCount);
            ds = DSolution.getRand(Dprob,false,0,0);
            JSSP.printSolution(ds.sol,Dprob.pProblem);
            String filename = JSSP.saveSolution(ds.sol,Dprob.pProblem);
            JSSP.displaySolution(filename);
            ds.MakeFeasible();;
            JSSP.printSolution(ds.sol,Dprob.pProblem);
            filename = JSSP.saveSolution(ds.sol,Dprob.pProblem);
            JSSP.displaySolution(filename);

        }
        System.out.println(" ... and we are off to the races");
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
