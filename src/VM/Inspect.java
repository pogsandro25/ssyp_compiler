package VM;

public class Inspect implements Instruction {

    private final int offset;

    Inspect(int offset) {
        this.offset = offset;
    }

    @Override
    public void execute(VmRuntime runtime) {
        System.out.println("inspect: " + runtime.stackAt(this.offset));
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.println("INSPECT");
    }
}
