import java.util.List;
import java.util.ArrayList;

public class Main {

    public static void main() {
        TokenToMachine encoder = new TokenToMachine();
        List<List<String>> program = new ArrayList<>();
        program.add(List.of("lw", "0", "1", "7"));
        program.add(List.of("lw", "1", "2", "3"));
        program.add(List.of("add", "1", "2", "1"));
        program.add(List.of("beq", "0", "1", "2"));
        program.add(List.of("beq", "0", "0", "-3"));
        program.add(List.of("noop"));
        program.add(List.of("halt"));
        program.add(List.of(".fill", "5"));
        program.add(List.of(".fill", "-1"));
        program.add(List.of(".fill", "2"));
        encoder.encodeAll(program);
    }

}


