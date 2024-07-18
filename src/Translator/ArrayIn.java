package Translator;

public class ArrayIn extends TernaryBytecodeInstructionTemplate {
    public ArrayIn(int array, int index, int value) {
        super(array, index, value, "ARRAY_IN");
    }
}
