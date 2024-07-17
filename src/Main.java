import Parsing.Function;
import Parsing.Parser;
import Translator.BytecodeFile;
import Translator.Translator;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("provide a file argument");
            return;
        }

        List<String> tokens = Lexer.tokenizeFromFile(args[0]);

        for (String tk : tokens) {
            System.out.println(tk);
        }

        Parser parser = new Parser(tokens);
        Function[] functions = parser.getFunctions();

        // TODO: support different bytecode output
        BytecodeFile file = new BytecodeFile(args[0] + ".bc");
        file.init();

        Translator.translate(functions, file);

        file.write();
    }
}
