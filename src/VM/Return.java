package VM;

public class Return implements Instruction {
    private final int returnOffset;

    Return(int returnOffset) {
        this.returnOffset = returnOffset;
    }

    @Override
    public void execute(VmRuntime runtime) {
        runtime.returnWith(runtime.stackAt(returnOffset));
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.printf("%d: RETURN: %d\n", runtime.getInstructionNumber(), runtime.stackAt(this.returnOffset));
    }
}
