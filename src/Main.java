import TokenToMachine.TokenToMachine;
import lexer.Execute;
import simulator.Simulator;
import simulator.stateStruct;

public boolean runAssembly(String filename) {
    Execute execute = new Execute();
    TokenToMachine encoder = new TokenToMachine();
    List<List<String>> program = new ArrayList<>();

    try {
        program = execute.runOn(filename);
        encoder.encodeAll(program);
        return true;
    }catch (Exception e){
        IO.println("Error: " + e.getMessage());
        return false;
    }


}

void main() {
    Simulator simulator = new Simulator();
    stateStruct state = new stateStruct();

    boolean assembleSuccess = runAssembly("src/fileAssembly/multiplication");

    if (!assembleSuccess) {
        System.exit(1);
    }

    simulator.run("MachineCode.txt",state);
}