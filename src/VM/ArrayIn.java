package VM;

import java.util.Arrays;

public class ArrayIn implements Instruction {
    private final int arrayOffset;
    private final int indexOffset;
    private final int value;

    ArrayIn(int arrayOffset, int indexOffset, int value) {
        this.arrayOffset = arrayOffset;
        this.indexOffset = indexOffset;
        this.value = value;
    }

    @Override
    public void execute(VmRuntime runtime) {
        assert runtime.getArray(runtime.stackAt(this.arrayOffset)).length > runtime.stackAt(this.indexOffset);

        runtime.updateArray(
                runtime.stackAt(this.arrayOffset),
                runtime.stackAt(this.indexOffset),
                runtime.stackAt(this.value)
        );
    }

    @Override
    public void println(VmRuntime runtime) {
        System.out.printf("ARRAY_IN: %s[%d] = %d\n", Arrays.toString(runtime.getArray(this.arrayOffset)), runtime.stackAt(this.indexOffset), runtime.stackAt(this.value));
    }
}
