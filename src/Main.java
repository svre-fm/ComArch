import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        TokenToMachine encoder = new TokenToMachine();

        List<List<String>> program = new ArrayList<>();
        program.add(List.of("add", "1", "2", "1"));
        program.add(List.of("lw", "1", "2", "3"));
        program.add(List.of("halt"));
        encoder.encodeAll(program);

       List token = Arrays.asList("add", "1", "2", "1");
        System.out.println(encoder.encode(token));
    }
}