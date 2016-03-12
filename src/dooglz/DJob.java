package dooglz;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class DJob {
    final public modelP.Job pjob;
    final public modelP.Operation[] pops;
    final public DOperation[] ops;

    public DJob(modelP.Job pjob, int opcount, DMachine[] macs) {
        this.pjob = pjob;
        ops = new DOperation[opcount];

        //steal pops
        ArrayList<modelP.Operation> ao = new ArrayList<modelP.Operation>();
        try {
            Field f = modelP.Job.class.getDeclaredField("operations");
            f.setAccessible(true);
            ao = ((ArrayList<modelP.Operation>) f.get(pjob));
        } catch (Exception e) {
        }
        this.pops = ao.toArray(new modelP.Operation[opcount]);

        for (int i = 0; i < opcount; i++) {
            ops[i] = new DOperation(macs,this,this.pops[i]);
        }
        /*



         try {
                Field f = modelP.Problem.class.getDeclaredField("operations");
                f.setAccessible(true);
                ArrayList<Operation> ao = ((ArrayList<Operation>) f.get(jobs[jobid]));

                for (int jobid = 0; jobid < jobCount; ++jobid) {

                }

                for(Machine m : am){

                }

            } catch (Exception e) {

            }


         */
    }

}
