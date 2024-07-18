package VM;

public class Jmp implements Instruction {
    private final CompareType cmpType;
    private final int offsetLeft;
    private final int offsetRight;
    private final int destination;

    Jmp(int cmpType, int offsetLeft, int offsetRight, int destination) {
        this.cmpType = CompareType.values()[cmpType];
        this.offsetLeft = offsetLeft;
        this.offsetRight = offsetRight;
        this.destination = destination;
    }

    @Override
    public void execute(VmRuntime runtime) {
        boolean shouldJmp;

        switch (this.cmpType) {
            case NoCmp -> shouldJmp = true;
            case Equal -> shouldJmp = runtime.stackAt(this.offsetLeft) == runtime.stackAt(this.offsetRight);
            case NotEqual -> shouldJmp = runtime.stackAt(this.offsetLeft) != runtime.stackAt(this.offsetRight);
            case Lower -> shouldJmp = runtime.stackAt(this.offsetLeft) < runtime.stackAt(this.offsetRight);
            case LowerEqual -> shouldJmp = runtime.stackAt(this.offsetLeft) <= runtime.stackAt(this.offsetRight);
            case Greater -> shouldJmp = runtime.stackAt(this.offsetLeft) > runtime.stackAt(this.offsetRight);
            case GreaterEqual -> shouldJmp = runtime.stackAt(this.offsetLeft) >= runtime.stackAt(this.offsetRight);
            default -> {
                assert false;
                shouldJmp = false;
            }
        }

        if (shouldJmp)
            runtime.jumpBy(this.destination);
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.printf("%d: JMP: %d %s %d to %d\n", runtime.getInstructionNumber(), runtime.stackAt(this.offsetLeft), this.cmpType.toString(), runtime.stackAt(this.offsetRight), this.destination);
    }
}

