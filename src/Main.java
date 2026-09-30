
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lexer.AssemblyLine;
import lexer.LabelAddressResolver;
import lexer.Lexer;

public class Main {
    public static void main(String[] args) throws IOException {
        runOn("samples/test.asm");
    }

    private static void runOn(String path) throws IOException {
        String src = Files.readString(Path.of(path));
        Lexer lexer = new Lexer(src);
        List<AssemblyLine> lines = lexer.tokenize();
        List<AssemblyLine> resolvedLines = new LabelAddressResolver().resolve(lines);

        System.out.println("Instructions with resolved label addresses:");
        for (int address = 0; address < resolvedLines.size(); address++) {
            System.out.printf("address %-2d %s%n", address, resolvedLines.get(address));
        }
    }
}