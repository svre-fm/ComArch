import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main() {
        TokenToMachine encoder = new TokenToMachine();

        List<List<String>> program = new ArrayList<>();
        program.add(List.of("add", "1", "2", "1"));
        program.add(List.of("lw", "1", "2", "555"));
        program.add(List.of("halt"));
        program.add(List.of("noop"));
        encoder.encodeAll(program);

    }
}