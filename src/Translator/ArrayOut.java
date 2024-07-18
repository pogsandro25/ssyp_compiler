package Translator;

public class ArrayOut extends TernaryBytecodeInstructionTemplate {
    public ArrayOut(int array, int index, int value) {
        super(array, index, value, "ARRAY_OUT");
    }
}
