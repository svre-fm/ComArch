package lexer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Execute {
    public List<List<String>> runOn(String path) throws IOException {
        String src = Files.readString(Path.of(path));
        Lexer lexer = new Lexer(src);
        List<AssemblyLine> lines = lexer.tokenize();
        List<AssemblyLine> resolvedLines = new LabelAddressResolver().resolve(lines);

        List<List<String>> program = new ArrayList<>();
        for (AssemblyLine line : resolvedLines) {
            List<String> tokens = new ArrayList<>();
            tokens.add(line.getInstruction());
            addToken(tokens, line.getField0());
            addToken(tokens, line.getField1());
            addToken(tokens, line.getField2());
            program.add(tokens);
        }
        return program;
    }

    private static void addToken(List<String> tokens, String field) {
        if (!field.isEmpty()) {
            tokens.add(field);
        }
    }
}
