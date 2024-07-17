import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lexer {
    public static List<String> tokenize(String input) {
        assert !input.contains("\t");
        List <Integer> started = new ArrayList<>();
        List <Integer> ended = new ArrayList<>();
        char[] toCharInput = input.toCharArray();
        boolean flag = true;
        for (int i = 0; i < input.length(); ++i) {
            if (toCharInput[i] == '"' && toCharInput[i - 1] != '\\') {
                if (flag) {
                    started.add(i);
                    flag = false;
                } else {
                    ended.add(i);
                    flag = true;
                }
            }
        }
        assert started.size() == ended.size();

        char[] input1 = input.toCharArray();
        for (int i = 0; i < started.size(); ++i) {
            for (int j = started.get(i) + 1; j < ended.get(i); ++j) {
                input1[j] = '.';
            }
        }
        String input2 = "";
        for (int i = 0; i < input1.length; ++i) {
            input2 += input1[i];
        }
        List <String> tokens =  getSplitBySpc(input2);
        int ind = 0;
        for (int i = 1; i < tokens.size() - 1; ++i) {
            if (Objects.equals(tokens.get(i - 1), "\"") && Objects.equals(tokens.get(i + 1), "\"")) {
                tokens.set(i, input.substring(started.get(ind), ended.get(ind) + 1));
                ind += 1;
            }

        }
        return tokens;
    }

    private static List<String> getSplitBySpc(String input) {
        assert !input.contains("\t");

        String splitBySemicolon = input
                .replace("\r", "")
                .replace("\n", "")
                .replace(";", " ; ")
                .replace("#", " # ")
                .replace("\"", " \" ");
        List <String> splitSLines = new ArrayList<>(List.of(splitBySemicolon.split(" ")));

        int size = splitSLines.size();
        for (int i = 0; i < size; ++i) {
            if (splitSLines.get(i).isEmpty()) {
                splitSLines.remove(i);
                --i;
            }
            size = splitSLines.size();
        }
        return splitSLines;
    }
    public static List<String> tokenizeFromFile(String filename) {
        try {
            return tokenize(Files.readString(Path.of(filename), Charset.defaultCharset()));
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }
}





