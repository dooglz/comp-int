package dooglz;

import java.util.ArrayList;

public class DMachine {
    final public ArrayList<modelP.Operation> popsOnMachine;
    final public ArrayList<DOperation> opsOnMachine;
    final public int id;

    public DMachine(int opcount, int id) {
        this.popsOnMachine = new ArrayList();
        this.opsOnMachine = new ArrayList();
        this.id = id;
    }
}
