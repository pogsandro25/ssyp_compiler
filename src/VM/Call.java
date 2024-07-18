package VM;

public class Call implements Instruction {
    private final String functionLabel;
    private final int newFunctionOffset;

    Call(String functionLabel, int newFunctionOffset) {
        this.functionLabel = functionLabel;
        this.newFunctionOffset = newFunctionOffset;
    }

    @Override
    public void execute(VmRuntime runtime) {
        runtime.enterNewFunction(this.functionLabel, this.newFunctionOffset);
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.printf("%d: CALL %s %d\n", runtime.getInstructionNumber(), this.functionLabel, this.newFunctionOffset);
    }
}
