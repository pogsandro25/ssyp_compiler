package Translator;

import Parsing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.IntStream;

public class Translator {
    public static void translate(Function[] functions, BytecodeFile file) {
        for (Function func : functions) {
            generateFunction(func, file);
        }

        file.add_func("print");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));

        file.add_func("print_array");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));

        file.add_func("cprint_array");
        file.add_instructions(new Extern());
        file.add_instructions(new Return(0));
    }

    private static void generateFunction(Function func, BytecodeFile file) {
        ArrayList<String> virtualStack = new ArrayList<>();
        virtualStack.addAll(Arrays.stream(func.arguments()).map(Variable::name).toList());
        virtualStack.addAll(Arrays.stream(func.locals()).map(Variable::name).toList());

        HashMap<String, String[]> typeMap = new HashMap<>();
        Arrays.stream(func.arguments()).forEach(arg -> typeMap.put(arg.name(), arg.type()));
        Arrays.stream(func.locals()).forEach(arg -> typeMap.put(arg.name(), arg.type()));

        file.add_func(func.name());

        ArrayList<Block> block = new ArrayList<>();
        ArrayList<BytecodeInstruction> instructions = new ArrayList<>();

        for (Variable local : func.locals()) {
            if (local.type()[0].equals("Array")) {
                instructions.add(new CreateArray(-virtualStack.indexOf(local.name()), Integer.parseInt(local.type()[1])));
            }
        }

        for (Instruction ins : func.instructions()) {
            generateInstruction(ins, block, instructions, virtualStack, typeMap);
        }

        if (!block.isEmpty()) {
            System.out.println("unclosed block found");
            throw new RuntimeException();
        }

        for (BytecodeInstruction bytecode : instructions) {
            file.add_instructions(bytecode);
        }

        if (func.instructions()[func.instructions().length - 1].type() != InstructionType.RETURN) {
            file.add_instructions(new Return(0));
        }
    }

    private static void generateInstruction(Instruction ins, ArrayList<Block> blocks, ArrayList<BytecodeInstruction> instructions, ArrayList<String> virtualStack, HashMap<String, String[]> typeMap) {
        switch (ins.type()) {
            case ADD -> instructions.add(
                    new Add(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions))
            );

            case SUB -> instructions.add(
                    new Sub(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions))
            );

            case DIV -> instructions.add(
                    new Div(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions))
            );

            case MUL -> instructions.add(
                    new Mul(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions))
            );

            case ASSIGN -> {
                if (getVarType(ins, 0, typeMap).equals("Int")) {
                    Optional<Integer> arg1 = getVarOnlyAddress(ins, 0, virtualStack);
                    if (arg1.isEmpty()) {
                        System.out.println();
                        throw new RuntimeException();
                    }

                    Optional<Integer> arg2 = getVarOnlyAddress(ins, 1, virtualStack);
                    arg2.ifPresent(arg -> instructions.add(
                            new Set(arg1.get(),
                                    arg
                            )
                    ));

                    if (arg2.isEmpty()) {
                        instructions.add(
                                new Mov(getVarAddress(ins, 0, virtualStack, instructions),
                                        ins.get(1).flatMap(Either::getRight).get()
                                )
                        );
                    }
                }
            }

            case CALL -> {
                Optional<Integer> returnArgsAddress = getVarOnlyAddress(ins, 0, virtualStack);

                // gather address of args
                int i = returnArgsAddress.isEmpty() ? 0 : 1;
                ArrayList<Integer> varAddress = new ArrayList<>();
                while (ins.get(i).isPresent()) {
                    varAddress.add(getVarAddress(ins, i, virtualStack, instructions));
                    i++;
                }

                // MOV instructions
                int functionFreeSpace = -virtualStack.size() - 1; // invert sign
                for (int e : IntStream.range(0, varAddress.size()).toArray()) {
                    instructions.add(new Mov(varAddress.get(e),
                            functionFreeSpace - e - 1
                    ));
                }

                // CALL instruction
                ins.functionName().ifPresent(functionName -> instructions.add(
                        new Call(functionName,
                                functionFreeSpace
                        )
                ));

                // SET return
                returnArgsAddress.ifPresent(returnAdr -> instructions.add(
                        new Mov(functionFreeSpace,
                                returnAdr
                        )
                ));
            }

            case RETURN -> instructions.add(
                    new Return(getVarAddress(ins, 0, virtualStack, instructions))
            );

            case IF -> {
                ins.get(0).flatMap(Either::getLeft).ifPresentOrElse(cmpType -> instructions.add(
                        new Jump(CompareTypes.fromSymbol(cmpType).invert(),
                                getVarAddress(ins, 1, virtualStack, instructions),
                                getVarAddress(ins, 2, virtualStack, instructions),
                                Integer.MAX_VALUE
                        )
                ), () -> {
                    System.out.println("if should have a cmp operand");
                    throw new RuntimeException();
                });

                blocks.add(new Block(instructions.size() - 1));
            }

            case ELIF -> {
                ins.get(0).flatMap(Either::getLeft).ifPresentOrElse(cmpType -> instructions.add(
                        new Jump(CompareTypes.fromSymbol(cmpType).invert(),
                                getVarAddress(ins, 1, virtualStack, instructions),
                                getVarAddress(ins, 2, virtualStack, instructions),
                                Integer.MAX_VALUE
                        )
                ), () -> {
                    System.out.println("if should have a cmp operand");
                    throw new RuntimeException();
                });

                blocks.getLast().mid.add(instructions.size() - 1);

                instructions.add(new Jump(CompareTypes.NoCmp, 0, 0, 0));
                blocks.getLast().addJmpInfo(instructions.size() - 1);
            }

            case ELSE -> {
                instructions.add(new Jump(CompareTypes.NoCmp, 0, 0, 0));
                blocks.getLast().addJmpInfo(instructions.size() - 1);
                blocks.getLast().elseStart = Optional.of(instructions.size()); // TODO: check for correctness #2
            }

            case ENDIF -> {
                blocks.getLast().end = instructions.size(); // TODO: check for correctness

                blocks.getLast().fixJumps(instructions);
                blocks.removeLast();
            }

            case ARRAY_IN -> {
                if (!getVarType(ins, 0, typeMap).equals("Array")) {
                    System.out.println("Ошибка: У ARRAY_IN первый аргумент должен быть массивом.");
                    throw new RuntimeException();
                } else if (!getVarType(ins, 1, typeMap).equals("Int")) {
                    System.out.println("Ошибка: У ARRAY_IN второй аргумент должен быть числом.");
                    throw new RuntimeException();
                } else if (!getVarType(ins, 2, typeMap).equals("Int")) {
                    System.out.println("Ошибка: У ARRAY_IN третий аргумент должен быть числом.");
                    throw new RuntimeException();
                } else {
                    instructions.add(new ArrayIn(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions)
                    ));
                }
            }

            case ARRAY_OUT -> {
                if (!getVarType(ins, 0, typeMap).equals("Array")) {
                    System.out.println("Ошибка: У ARRAY_OUT первый аргумент должен быть массивом.");
                    throw new RuntimeException();
                } else if (!getVarType(ins, 1, typeMap).equals("Int")) {
                    System.out.println("Ошибка: У ARRAY_OUT второй аргумент должен быть числом.");
                    throw new RuntimeException();
                } else if (!getVarType(ins, 2, typeMap).equals("Int")) {
                    System.out.println("Ошибка: У ARRAY_OUT третий аргумент долже быть числом.");
                    throw new RuntimeException();
                } else {
                    instructions.add(new ArrayOut(getVarAddress(ins, 0, virtualStack, instructions),
                            getVarAddress(ins, 1, virtualStack, instructions),
                            getVarAddress(ins, 2, virtualStack, instructions)
                    ));
                }
            }
        }
    }

    private static String getVarType(Instruction ins, int index, HashMap<String, String[]> typeMap) {
        Optional<Either<String, Integer>> var = ins.get(index);
        if (var.isPresent() && var.get().getLeft().isPresent()) {
            return typeMap.get(var.get().getLeft().get())[0];
        } else {
            return "Int";
        }
    }

    private static int getVarAddress(Instruction ins, int index, ArrayList<String> virtualStack, ArrayList<BytecodeInstruction> instructions) {

        if (ins.get(index).isPresent()) {
            Either<String, Integer> arg = ins.get(index).get();

            // args must be flipped
            if (arg.getLeft().isPresent()) {
                int adr = virtualStack.indexOf(arg.getLeft().get());
                if (adr == -1) {
                    System.out.printf("var %s is not found on stack\n", arg.getLeft().get());
                    throw new RuntimeException();
                }
                return -adr;
            } else if (arg.getRight().isPresent()) {
                Optional<Integer> lit = arg.getRight();
                return -orCreateStack(lit.get(), virtualStack, instructions);
            } else {
                System.out.println("all variants of either are not valid");
                throw new RuntimeException();
            }
        } else {
            System.out.printf("missing arg %d for %s\n", index, ins.type().toString());
            throw new RuntimeException();
        }
    }

    private static Optional<Integer> getVarOnlyAddress(Instruction ins, int index, ArrayList<String> virtualStack) {

        if (ins.get(index).isPresent()) {
            Either<String, Integer> arg = ins.get(index).get();

            // args must be flipped
            if (arg.getLeft().isPresent()) {
                int adr = virtualStack.indexOf(arg.getLeft().get());
                if (adr == -1) {
                    System.out.printf("var %s is not found on stack\n", arg.getLeft().get());
                    throw new RuntimeException();
                }
                return Optional.of(-adr);
            } else if (arg.getRight().isPresent()) {
                return Optional.empty();
            } else {
                System.out.println("all variants of either are not valid");
                throw new RuntimeException();
            }
        } else {
            System.out.printf("missing arg %d for %s\n", index, ins.type().toString());
            throw new RuntimeException();
        }
    }

    private static int orCreateStack(int literal, ArrayList<String> virtualStack, ArrayList<BytecodeInstruction> instructions) {
        int pos = virtualStack.indexOf(STR."#\{literal}");

        if (pos == -1) {
            virtualStack.add(STR."#\{literal}"); // literals will have a # before them
            pos = (virtualStack.size()) - 1;
        }

        // Could in the future bring constants to the top
        instructions.add(new Set(-pos, literal));

        return pos;
    }
}

class Block {
    int start;
    int startJump;
    ArrayList<Integer> mid;
    ArrayList<Integer> midJump;
    Optional<Integer> elseStart;
    int end;

    Block(int start) {
        this.start = start;
        this.mid = new ArrayList<>();
        this.midJump = new ArrayList<>();
        this.end = Integer.MAX_VALUE;
        this.elseStart = Optional.empty();
    }

    public void fixJumps(ArrayList<BytecodeInstruction> instructions) {
        assert this.end != Integer.MAX_VALUE;

        if (instructions.get(this.start) instanceof Jump) {
            ((Jump) instructions.get(this.start)).setJumpDestination(this.mid.isEmpty() ? this.elseStart.orElseGet(() -> this.end) : this.mid.getFirst());
        } else {
            System.out.println("invalid if jmp instruction at index");
            throw new RuntimeException();
        }

        if (instructions.get(this.startJump) instanceof Jump) {
            ((Jump) instructions.get(this.startJump)).setJumpDestination(this.end);
        } else {
            System.out.println("invalid if jmp jmp instruction at index");
            throw new RuntimeException();
        }

        for (int i : this.mid) {
            if (instructions.get(i) instanceof Jump) {
                ((Jump) instructions.get(i)).setJumpDestination(this.mid.size() < i + 1 ? this.elseStart.orElseGet(() -> this.end) : this.mid.get(i + 1));
            } else {
                System.out.println("invalid else if jmp instruction at index");
                throw new RuntimeException();
            }
        }

        for (int i : this.midJump) {
            if (instructions.get(i) instanceof Jump) {
                ((Jump) instructions.get(i)).setJumpDestination(this.end);
            } else {
                System.out.println("invalid else if jmp jmp instruction at index");
                throw new RuntimeException();
            }
        }
    }

    void addJmpInfo(int instructionIndex) {
        if (this.midJump.isEmpty()) {
            this.startJump = instructionIndex;
        } else {
            midJump.add(instructionIndex);
        }
    }
}
