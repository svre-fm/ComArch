package simulator;

public class stateStruct {
    int pc = 0;
    int[] register = new int[8];
    int[] memory = new int[65536];
    int numMemory = 0;
}
