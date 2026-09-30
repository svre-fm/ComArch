
import java.io.IOException;

import lexer.Execute;

public class Main {
    public static void main(String[] args) throws IOException {
        Execute run = new Execute();
        TokenToMachine encoder = new TokenToMachine();
        encoder.encodeAll(run.runOn("samples/test.asm"));
    }






}