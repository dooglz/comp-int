package dooglz;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;

public class DJob implements Comparator<DJob>, Comparable<DJob> {
    final public modelP.Job pjob;
    final public modelP.Operation[] pops;
    final public DOperation[] ops;
    final public int totalProcessingTime;
    final public int id;

    public DJob(modelP.Job pjob, int opcount, DMachine[] macs, int id) throws IllegalStateException {
        this.id = id;
        this.pjob = pjob;
        ops = new DOperation[opcount];

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

        int ods = 0;
        for (int i = 0; i < opcount; i++) {
            ops[i] = new DOperation(macs, this, this.pops[i], i);
            ods += ops[i].duration;
        }

        this.totalProcessingTime = ods;
    }

    @Override
    public int compare(DJob j1, DJob j2) {
        return j2.totalProcessingTime - j1.totalProcessingTime;
    }

    @Override
    public int compareTo(DJob o) {
        //ascending order
        return o.totalProcessingTime - this.totalProcessingTime;
    }

    public DOperation GetOperationOnMachine(DMachine m) {
        for (DOperation d : this.ops) {
            if (d.machine == m) {
                return d;
            }
        }
        return null;
    }
}
