package lexer;

import TokenToMachine.TokenToMachine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Execute {
    public List<List<String>> runOn(String path) throws IOException {
        //read source code from file
        String src = Files.readString(Path.of(path));
        Lexer lexer = new Lexer(src);
        List<AssemblyLine> lines = lexer.tokenize();

        //show line instruction form lexer
        for (int i = 0;i < lines.size();i++){
            System.out.println(lines.get(i));
        }

        //resolve labels into addresses
        List<AssemblyLine> resolvedLines = new LabelAddressResolver().resolve(lines);

        //store final list of tokens
        List<List<String>> program = new ArrayList<>();
        for (AssemblyLine line : resolvedLines) {
            List<String> tokens = new ArrayList<>();
            tokens.add(line.getInstruction());

            //add instruction fields
            addToken(tokens, line.getField0());
            addToken(tokens, line.getField1());
            addToken(tokens, line.getField2());
            program.add(tokens);
        }
        return program;
    }


    //add field only if it is not empty
    private static void addToken(List<String> tokens, String field) {
        if (!field.isEmpty()) {
            tokens.add(field);
        }
    }
}