import simulator.stateStruct;
import simulator.Simulator;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {
    stateStruct state = new stateStruct();
    Simulator simulator = new Simulator();

    simulator.run("src/machine_code/test", state);

}
