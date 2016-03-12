package dooglz;

import modelP.Job;
import modelP.Machine;
import modelP.Operation;
import modelP.Problem;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class DProblem {

    final public modelP.Problem pProblem;
    final public modelP.Job[] pjobs;
    final public DMachine[] machines;
    final public DJob[] jobs;
    final public int jobCount;
    final public int machineCount;


    public DProblem(modelP.Problem p)  throws IllegalStateException{
        this.pProblem = p;
        this.jobCount = p.getNumberOfJobs();
        this.machineCount = p.getNumberOfMachines();
        this.machines = new DMachine[machineCount];
        this.jobs= new DJob[jobCount];

        for (int i = 0; i < machineCount; i++) {
            this.machines[i] = new DMachine(machineCount,i);
        }

        //steal array of jobs
        modelP.Job[] unconst_jobs = new modelP.Job[p.getNumberOfJobs()];
        try {
            Field f = modelP.Problem.class.getDeclaredField("jobs");
            f.setAccessible(true);
            ArrayList<Job> aj = new ArrayList<modelP.Job>((ArrayList<modelP.Job>) f.get(p));
            unconst_jobs = aj.toArray(unconst_jobs);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
        pjobs = unconst_jobs;
        //turn pjobs into DJobs
        for (int jobid = 0; jobid < jobCount; ++jobid) {
            this.jobs[jobid] = new DJob(pjobs[jobid],machineCount, this.machines,jobid);
        }


    }
}
