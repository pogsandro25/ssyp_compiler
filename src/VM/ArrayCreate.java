package VM;

public class ArrayCreate implements Instruction {
    private final int arrayOffset;
    private final int sizeOffset;
    ArrayCreate(int arrayOffset, int sizeOffset) {
        this.arrayOffset = arrayOffset;
        this.sizeOffset = sizeOffset;
    }

    @Override
    public void execute(VmRuntime runtime) {
        int key = runtime.createNewArray(runtime.stackAt(this.sizeOffset));
        runtime.setStackAt(arrayOffset, key);
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.println("ARRAY_CREATE");
    }
}
