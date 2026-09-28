
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lexer.AssemblyLine;
import lexer.Lexer;
import lexer.LexicalException;

public class Main {
    public static void main(String[] args) throws IOException {
        runOn("samples/test.asm");
    }

    private static void runOn(String path) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append("===== Tokenizing: ").append(path).append(" =====\n");

        String src = Files.readString(Path.of(path));
        Lexer lexer = new Lexer(src);
        List<AssemblyLine> lines = lexer.tokenize();

        System.out.println("ต้องการ list token ตัวแรก");
        System.out.println(lines.get(0));
        System.out.println("************************");

        System.out.println("ต้องการ list token ตัวแรก");
        System.out.println(lines.get(0).getInstruction() + " " + lines.get(0).getField0() + " " + lines.get(0).getField1() + " " + lines.get(0).getField2() );
        System.out.println("************************");


//        สำหรับให้แสดงทุก token ใน list
//        for (AssemblyLine line : lines) {
//            String text = line.toString();
//            System.out.println(text + "\n");
//        }
        

    }
}