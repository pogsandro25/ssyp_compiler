package VM;

public class ArrayCreate implements Instruction {
    private final int arrayOffset;
    private final int size;
    ArrayCreate(int arrayOffset, int size) {
        this.arrayOffset = arrayOffset;
        this.size = size;
    }

    @Override
    public void execute(VmRuntime runtime) {
        int key = runtime.createNewArray(this.size);
        runtime.setStackAt(arrayOffset, key);
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.println("ARRAY_CREATE");
    }
}
