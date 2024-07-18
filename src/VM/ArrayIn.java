package VM;

public class ArrayIn implements Instruction{
    private final int arrayOffset;
    private final int index;
    private final int value;
    ArrayIn(int arrayOffset, int index, int value) {
        this.arrayOffset = arrayOffset;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute(VmRuntime runtime) {
        runtime.updateArray(
                runtime.stackAt(this.arrayOffset),
                runtime.stackAt(this.index),
                runtime.stackAt(this.value)
        );
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.println("ARRAY_IN");
    }
}
