
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lexer.Lexer;
import lexer.LexicalException;
import lexer.Token;
import lexer.TokenType;

public class Main {
    public static void main(String[] args) throws IOException {
        runOn("samples/test.asm");
    }

    private static void runOn(String path) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append("===== Tokenizing: ").append(path).append(" =====\n");

        String src = Files.readString(Path.of(path));
        Lexer lexer = new Lexer(src);

        try {
            List<Token> tokens = lexer.tokenize();
            int count = 0;
            for (Token t : tokens) {
                if (t.getType() == TokenType.NEWLINE) continue;
                String line = t.toString();
                output.append(line).append("\n");
                System.out.println(line);
                count++;
            }
            String total = "total: " + count + " tokens (not NEWLINE/EOF)";
            output.append(total).append("\n");


            System.out.println(total);
        } catch (LexicalException e) {
            String message = e.getMessage();
            output.append(message).append("\n");
            System.err.println(message);
        }

        Path outputFile = Path.of("output.txt");
        Files.writeString(outputFile, output.toString());
        System.out.println("Saved output to: " + outputFile.toAbsolutePath());
    }
}