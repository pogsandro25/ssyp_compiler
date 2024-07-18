import Parsing.Function;
import Parsing.Parser;
import Translator.BytecodeFile;
import Translator.Translator;
import VM.VM;

import java.io.File;
import java.util.List;
import java.util.Objects;

interface FileFunction {
    void run(String str);
}

public class Test {
    public static void main(String[] args) {
        runFromDir("tests/ir/", Test::runIr);
        runFromDir("tests/bytecode", Test::runByteCode);
    }

    static void runFromDir(String dir, FileFunction func) {
        File folder = new File(dir);

        if (folder.isFile()) {
            return;
        }

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile()) {
                func.run(file.getPath());
            }
        }
    }

    static void runIr(String pathToFile) {
        List<String> tokens = Lexer.tokenizeFromFile(pathToFile);
      
        Parser parser = new Parser(tokens);
        Function[] functions = parser.getFunctions();
        for (Function function : functions) {
            System.out.println(STR."\n\{function.toString()}");
        }

        BytecodeFile file = new BytecodeFile("out/bytecode.bc");
        file.init();

        Translator.translate(functions, file);

        file.write();

        runByteCode("out/bytecode.bc");
    }

    static void runByteCode(String pathToFile) {
        VM.main(new String[]{ pathToFile });
    }
}
