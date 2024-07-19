package VM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

public class VmRuntime {
    private final List<Integer> stack;
    private final List<Pair<String, Integer>> callStack;
    private final int endInstruction = Integer.MAX_VALUE;
    private final HashMap<String, List<Instruction>> instructions = new HashMap<>();
    private final boolean IsDebugging;
    private final HashMap<Integer, Integer[]> heap = new HashMap<>();
    private List<Instruction> currentInstructions;
    private String currentFunctionName = "main";
    private int stackBase;
    private int currentInstruction = 0;
    private int thisKey = 1;

    VmRuntime(InputReader reader, int size, boolean isDebugging) {
        this.stack = new ArrayList<>(size + 1);
        this.callStack = new ArrayList<>(size + 1);
        this.IsDebugging = isDebugging;


        for (int i : IntStream.range(0, size + 1).toArray()) {
            this.stack.add(0);
        }

        this.stack.set(size, this.endInstruction);
        this.stackBase = size - 1;

        while (reader.hasNext()) {
            Pair<String, ArrayList<Instruction>> pair = reader.nextFunction();
            this.instructions.put(pair.get0(), pair.get1());
        }

        if (!this.instructions.containsKey("main")) {
            System.out.println("entry function main doesn't exist");
            throw new RuntimeException();
        }

        this.callStack.add(new Pair<>(currentFunctionName, this.currentInstruction));
        this.currentInstructions = this.instructions.get(currentFunctionName);
    }

    public void run() {
        while (this.currentInstructions.size() > this.currentInstruction) {
            this.runNext();
            assert Integer.signum(this.currentInstruction) == 1;
        }
    }

    private void runNext() {
        if (this.IsDebugging)
            this.currentInstructions.get(this.currentInstruction).println(this);

        this.currentInstructions.get(this.currentInstruction).execute(this);
        this.currentInstruction++;
    }

    public int stackAt(int offset) {
        return this.stack.get(this.stackBase + offset);
    }

    public void setStackAt(int offset, int value) {
        int stackField = this.stackBase + offset;

        this.stack.set(stackField, value);
    }

    public void enterNewFunction(String functionLabel, int offset) {
        this.callStack.getLast().set1(this.currentInstruction);

        if (!this.instructions.containsKey(functionLabel)) {
            System.out.println(STR."function \{functionLabel} doesn't exist");
            throw new RuntimeException();
        }

        this.currentInstructions = this.instructions.get(functionLabel);

        assert Integer.signum(offset) == -1;

        int previousStackBase = this.stackBase;
        this.stackBase += offset - 1; // advance by one from the free slot
        this.currentInstruction = -1; // instruction will get incremented back to 0 after this

        this.setStackAt(1, previousStackBase);

        this.currentFunctionName = functionLabel;
        this.callStack.add(new Pair<>(this.currentFunctionName, 0));
    }

    public void returnWith(int obj) {
        if (this.stackAt(1) == this.endInstruction || this.callStack.size() == 1) {
            this.currentInstruction = this.currentInstructions.size();
            this.stackBase = this.stack.size() - 1;

            System.out.println(STR."out: \{obj}");
        } else {
            this.callStack.removeLast(); // remove current function from stack
            this.currentInstruction = this.callStack.getLast().get1();
            this.currentFunctionName = this.callStack.getLast().get0();
            this.currentInstructions = this.instructions.get(this.currentFunctionName);

            int base = this.stackAt(1);
            this.setStackAt(1, obj);
            this.stackBase = base;
        }
    }

    public void jumpBy(int offset) {
        this.currentInstruction = offset;

        assert Integer.signum(this.currentInstruction) == 1 && this.currentInstruction < this.currentInstructions.size();

        this.currentInstruction--; // TODO: check for correctness
    }

    public String getCurrentFunctionName() {
        return this.currentFunctionName;
    }

    public int createNewArray(int size) {
        heap.put(thisKey, new Integer[size]);
        thisKey++;
        return thisKey - 1;
    }

    public void updateArray(int key, int index, int value) {
        heap.get(key)[index] = value;
    }

    public Integer[] getArray(int key) {
        return heap.get(key);
    }

    public int getInstructionNumber() {
        return this.currentInstruction;
    }
}