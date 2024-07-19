package VM;

import java.util.Arrays;

public class ArrayOut implements Instruction {
    private final int arrayOffset;
    private final int indexOffset;
    private final int varOffset;
    ArrayOut(int arrayOffset, int indexOffset, int varOffset) {
        this.arrayOffset = arrayOffset;
        this.indexOffset = indexOffset;
        this.varOffset = varOffset;
    }

    @Override
    public void execute(VmRuntime runtime) {
        assert runtime.getArray(runtime.stackAt(this.arrayOffset)).length > runtime.stackAt(this.indexOffset);

        runtime.setStackAt(
                this.varOffset,
                runtime.getArray(runtime.stackAt(this.arrayOffset))[runtime.stackAt(this.indexOffset)]);
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.printf("ARRAY_OUT: $%d = %s[%d]\n", this.varOffset, Arrays.toString(runtime.getArray(this.arrayOffset)), runtime.stackAt(this.indexOffset));
    }
}
