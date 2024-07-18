package Translator;

public class CreateArray extends BinaryBytecodeInstructionTemplate {
    public CreateArray(int address, int size) {
        super(address, size, "ARRAY_CREATE");
    }
}
