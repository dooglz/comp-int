package dooglz;

public class DMachine {
    final public modelP.Operation[] popsOnMachine;
    final public DOperation[] opsOnMachine;

    public DMachine(int opcount) {
        this.popsOnMachine = new modelP.Operation[opcount];
        this.opsOnMachine = new DOperation[opcount];
    }
}
