package dooglz;

import modelP.Job;
import modelP.Operation;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class DOperation {
    final DMachine machine;
    final DJob job;
    final modelP.Operation pop;
    final int duration;
    final int id;

    public DOperation(DMachine[] machines, DJob job, modelP.Operation op, int id) throws IllegalStateException {

        this.job = job;
        this.pop = op;
        this.id = id;
        int dur = -1;
        try {
            Field f = modelP.Operation.class.getDeclaredField("duration");
            f.setAccessible(true);
            dur = (int) f.get(op);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
        this.duration = dur;

        int mac = -1;
        try {
            Field f = modelP.Operation.class.getDeclaredField("machine");
            f.setAccessible(true);
            modelP.Machine m = (modelP.Machine) f.get(op);

            f = modelP.Machine.class.getDeclaredField("id");
            f.setAccessible(true);
            int mid = (int) f.get(m);
            mac = mid;
        } catch (Exception e) {
            throw new IllegalStateException();
        }
        this.machine = machines[mac];
        this.machine.popsOnMachine.add(op);
        this.machine.opsOnMachine.add(this);
    }
}
