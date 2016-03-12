package dooglz;

import modelP.Job;
import modelP.Operation;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class DOperation {
    final DMachine machine;
    final DJob job; //todo
    final modelP.Operation pop;
    final int duration;

    public DOperation(DMachine[] machines, DJob job, modelP.Operation op) {

        this.job = job;
        this.pop = op;

        int dur = -1;
        try {
            Field f = modelP.Operation.class.getDeclaredField("duration");
            f.setAccessible(true);
            dur = (int) f.get(op);
        } catch (Exception e) {
            System.exit(1);
        }
        this.duration = dur;

        int mac = -1;
        try {
            Field f = modelP.Operation.class.getDeclaredField("machine");
            f.setAccessible(true);
            modelP.Machine m = (modelP.Machine) f.get(op);

            f = modelP.Machine.class.getDeclaredField("id");
            f.setAccessible(true);
            int id = (int) f.get(op);
            mac = id;
        } catch (Exception e) {
            System.exit(1);
        }
        this.machine = machines[mac];
    }
}
