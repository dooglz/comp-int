package dooglz;

import modelP.Job;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;

public class DJob implements Comparator<DJob>, Comparable<DJob>{
    final public modelP.Job pjob;
    final public modelP.Operation[] pops;
    final public DOperation[] ops;
    final public int totalProcessingTime;
    final public int id;

    public DJob(modelP.Job pjob, int opcount, DMachine[] macs,int id) throws IllegalStateException {
        this.id = id;
        this.pjob = pjob;
        ops = new DOperation[opcount];
        this.totalProcessingTime = 0;
        //steal pops
        ArrayList<modelP.Operation> ao = new ArrayList<modelP.Operation>();
        try {
            Field f = modelP.Job.class.getDeclaredField("operations");
            f.setAccessible(true);
            ao = ((ArrayList<modelP.Operation>) f.get(pjob));
        } catch (Exception e) {
            throw new IllegalStateException();
        }
        this.pops = ao.toArray(new modelP.Operation[opcount]);

        for (int i = 0; i < opcount; i++) {
            ops[i] = new DOperation(macs, this, this.pops[i]);
        }
    }

    @Override
    public int compare(DJob j1, DJob j2) {
        return j1.totalProcessingTime - j2.totalProcessingTime;
    }

    @Override
    public int compareTo(DJob o) {
        //ascending order
        return this.totalProcessingTime - o.totalProcessingTime;
    }
}
