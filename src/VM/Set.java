package VM;

public class Set implements Instruction {
    private final int offset;
    private final int value;

    Set(int offset, int value) {
        this.offset = offset;
        this.value = value;
    }

    @Override
    public void execute(VmRuntime runtime) {
        runtime.setStackAt(this.offset, value);
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.printf("%d: SET: %d to addr %d\n", runtime.getInstructionNumber(), this.value, runtime.stackAt(this.offset));
    }
}
