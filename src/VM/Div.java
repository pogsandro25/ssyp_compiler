package VM;

public class Div implements Instruction {
    private final int offsetLeft;
    private final int offsetRight;
    private final int destination;

    Div(int destination, int offsetLeft, int offsetRight) {
        this.destination = destination;
        this.offsetLeft = offsetLeft;
        this.offsetRight = offsetRight;
    }

    @Override
    public void execute(VmRuntime runtime) {
        runtime.setStackAt(this.destination, runtime.stackAt(this.offsetLeft) / runtime.stackAt(this.offsetRight));
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.printf("%d: DIV: %d + %d\n", runtime.getInstructionNumber(), runtime.stackAt(this.offsetLeft), runtime.stackAt(this.offsetRight));
    }
}
