package simulator;

public class MachineState {
    boolean halted = false;
    int pc = 0;
    int[] register = new int[8];
    int[] memory = new int[65536];
    int numMemory = 0;
    int countIntructions = 0;
}
