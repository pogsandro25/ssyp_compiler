package Parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {
    final private List<String> tokens;
    public Parser(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<String> getFunctionTokens(String func_name, List<String> tokens) {
        boolean InFunc = false, InDesireFunc = false;
        String prevToken, thisToken;
        int begin = 0, end = 0;
        for (int k = 1; k < tokens.size() - 1; k++) {
            prevToken = tokens.get(k - 1);
            thisToken = tokens.get(k);
            if (prevToken.equals("#") && thisToken.equals("F_BEGIN")) {
                InFunc = true;
                begin = k - 1;
            } else if (prevToken.equals("#") && thisToken.equals("F_END")) {
                InFunc = false;
                if (InDesireFunc) {
                    end = k + 1;
                    break;
                }
            } else if (InFunc && tokens.get(k - 2).equals("#") && prevToken.equals("F_NAME") &&
                    thisToken.equals(func_name)) {
                InDesireFunc = true;
            }
        }
        return tokens.subList(begin, end);
    }

    int countOfTypes(List<String> tokens, int index) {
        int count = 0;
        for (int k = index; !tokens.get(k).equals(";"); k++) {
            count++;
        }
        return count - 1;
    }

    String[] getTypes(List<String> tokens, int index) {
        int length = countOfTypes(tokens, index);
        String[] types = new String[length];
        for (int k = 0; k < length; k++) {
            types[k] = tokens.get(k + index);
        }
        return types;
    }

    String getVar(List<String> tokens, int index) {
        for (int k = index; k < tokens.size(); k++) {
            if (tokens.get(k).equals(";")) {
                return tokens.get(k - 1);
            }
        }
        return "#ERROR";
    }

    int countOfVars(List<String> tokens) {
        int count = 0;
        String thisToken, nextToken;
        boolean InVars = false;
        for (int k = 0; k < tokens.size() - 1; k++) {
            thisToken = tokens.get(k);
            nextToken = tokens.get(k + 1);
            if (thisToken.equals("#") && nextToken.equals("F_VARS_BEGIN"))
            {
                InVars = true;
            } else if (thisToken.equals("#") && nextToken.equals("F_VARS_END")) {
                return count;
            } else if (nextToken.equals("@") && InVars) {
                count++;
            }
            }
        return 0;
    }

    Variable[] createVarsList(String func_name) {
        List<String> tokens = getFunctionTokens(func_name, this.tokens);
        boolean InVars = false;
        String thisToken, nextToken;
        int count = countOfVars(tokens), index = 0;
        Variable[] variables = new Variable[count];
        for (int k = 0; k < tokens.size() - 1; k++) {
            thisToken = tokens.get(k);
            nextToken = tokens.get(k + 1);
            if (thisToken.equals("#") && nextToken.equals("F_VARS_BEGIN"))
            {
                InVars = true;
            } else if (thisToken.equals("#") && nextToken.equals("F_VARS_END")) {
                return variables;
            } else if (thisToken.equals("@") && InVars) {
                variables[index] = new Variable(getVar(tokens, k + 1), getTypes(tokens, k + 1));
                index++;
            }
        }
        return new Variable[0];
    }

    int countOfArgs(List<String> tokens) {
        int count = 0;
        String thisToken, nextToken;
        boolean InArgs = false;
        for (int k = 0; k < tokens.size() - 1; k++) {
            thisToken = tokens.get(k);
            nextToken = tokens.get(k + 1);
            if (thisToken.equals("#") && nextToken.equals("F_ARGS_BEGIN"))
            {
                InArgs = true;
            } else if (thisToken.equals("#") && nextToken.equals("F_ARGS_END")) {
                return count;
            } else if (nextToken.equals("@") && InArgs) {
                count++;
            }
        }
        return 0;
    }

    Variable[] createArgsList(String func_name) {
        List<String> tokens = getFunctionTokens(func_name, this.tokens);
        int count = countOfArgs(tokens), index = 0;
        String thisToken, nextToken;
        boolean InArgs = false;
        Variable[] arguments = new Variable[count];
        for (int k = 0; k < tokens.size() - 1; k++) {
            thisToken = tokens.get(k);
            nextToken = tokens.get(k + 1);
            if (thisToken.equals("#") && nextToken.equals("F_ARGS_BEGIN"))
            {
                InArgs = true;
            } else if (thisToken.equals("#") && nextToken.equals("F_ARGS_END")) {
                return arguments;
            } else if (thisToken.equals("@") && InArgs) {
                arguments[index] = new Variable(getVar(tokens, k + 1), getTypes(tokens, k + 1));
                index++;
            }
        }
        return new Variable[0];
    }

    int countOfInstruction(List<String> tokens) {
        boolean InBody = false;
        int count = 0;
        for (int k = 1; k < tokens.size(); k++) {
            if (tokens.get(k - 1).equals("#") && tokens.get(k).equals("F_BODY_BEGIN")) {
                InBody = true;
                k += 2;  // Костыль мешающий расширяемости.
            } else if (tokens.get(k - 1).equals("#") && tokens.get(k).equals("F_BODY_END")) {
                return count;
            } else if (InBody && tokens.get(k - 1).equals(";")) {
                count++;
            }
        }
        return count;
    }

    InstructionType getInstructionType(String type) {
        return switch (type) {
            case "+" -> InstructionType.ADD;
            case "-" -> InstructionType.SUB;
            case "*" -> InstructionType.MUL;
            case "/" -> InstructionType.DIV;
            case "=" -> InstructionType.ASSIGN;
            case "[]" -> InstructionType.ARRAY_IN;
            case "][" -> InstructionType.ARRAY_OUT;
            case "IF" -> InstructionType.IF;
            case "ELIF" -> InstructionType.ELIF;
            case "ELSE" -> InstructionType.ELSE;
            case "ENDIF" -> InstructionType.ENDIF;
            case "F_RETURN" -> InstructionType.RETURN;
            default -> InstructionType.CALL;
        };
    }

    Either<String, Integer>[] toEitherArray(ArrayList<Either<String, Integer>> arrayList) {
        Either<String, Integer>[] array = new Either[arrayList.size()];
        for (int k = 0; k < arrayList.size(); k++) {
            array[k] = arrayList.get(k);
        }
        return array;
    }

    Instruction[] createInstructionList(String func_name) {
        List<String> tokens = getFunctionTokens(func_name, this.tokens);
        ArrayList<Either<String, Integer>> args = new ArrayList<>();
        InstructionType instructionType = null;
        int index = 0, count = countOfInstruction(tokens);
        Instruction[] instructions = new Instruction[count];
        boolean InBody = false, InInstruction = false;
        String instructionName = null;
        for (int k = 1; k < tokens.size(); k++) {
            if (tokens.get(k - 1).equals("#") && tokens.get(k).equals("F_BODY_BEGIN")) {
                InBody = true;
            } else if (tokens.get(k - 1).equals("#") && tokens.get(k).equals("F_BODY_END")) {
                return instructions;
            } else if (InBody && tokens.get(k - 1).equals("#") && !InInstruction) {
                InInstruction = true;
                instructionName = tokens.get(k);
                instructionType = getInstructionType(instructionName);
            } else if (InInstruction && tokens.get(k).equals(";")) {
                instructions[index] = new Instruction(instructionType,
                        toEitherArray(args),
                        Optional.of(instructionName));
                InInstruction = false;
                index++;
                args.clear();
            } else if (InInstruction) {
                if (tokens.get(k).equals("#")) {
                    args.add(Either.right(Integer.parseInt(tokens.get(k + 1))));
                    k++;
                } else {
                    args.add(Either.left(tokens.get(k)));
                }
            }
        }
        return instructions;
    }

    Function getFunction(String func_name) {
        return new Function(
                func_name,
                createArgsList(func_name),
                createVarsList(func_name),
                createInstructionList(func_name)
        );
    }

    ArrayList<String> getFunctionNames() {
        ArrayList<String> functionNames = new ArrayList<>();
        boolean InFunc = false;
        for (int k = 1; k < this.tokens.size(); k++) {
            if (this.tokens.get(k - 1).equals("#") && this.tokens.get(k).equals("F_BEGIN")) {
                InFunc = true;
            }
            else if (this.tokens.get(k - 1).equals("#") && this.tokens.get(k).equals("F_END")) {
                InFunc = false;
            }
            else if (InFunc && this.tokens.get(k - 1).equals("#") && this.tokens.get(k).equals("F_NAME")) {
                functionNames.add(tokens.get(k + 1));
            }
        }
        return functionNames;
    }

    public Function[] getFunctions() {
        ArrayList<String> functionNames = getFunctionNames();
        int length = functionNames.size();
        Function[] functions = new Function[length];
        for (int k = 0; k < length; k++) {
            functions[k] = getFunction(functionNames.get(k));
        }
        return functions;
    }
}