package simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Simulator {

    private boolean halted = false;
    private int countIntruction = 0;

    public void run(String filename, stateStruct state) {

        try {
            ReadMachinecode(filename,state);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < state.numMemory; i++) {
            IO.println("memory[" + i + "] = " + state.memory[i]);
        }
        IO.println();

        while(!halted){
            printState(state);
            executeInstruction(state);
        }

        IO.println("machine halted");
        IO.println("total of " + countIntruction + " instructions executed");
        IO.println("final state of machine : ");
        printState(state);
    }

    private void ReadMachinecode(String filename, stateStruct state) throws FileNotFoundException {

        Scanner scanner = new Scanner(new File(filename));

        while (scanner.hasNextLine()) {

            int value = Integer.parseInt(scanner.nextLine());

            state.memory[state.numMemory] = value;
            state.numMemory++;
        }

        scanner.close();
    }

    private void printState(stateStruct state) {
        IO.println("");
        IO.println("@@@");
        IO.println("state:");
        IO.println("    pc " + state.pc);
        IO.println("    memory:");
        for(int i = 0; i < state.numMemory; i++) {
            IO.println("        mem[ " + i + " ] " + state.memory[i]);
        }
        IO.println("    registers:");
        for(int i = 0; i < state.register.length; i++) {
            IO.println("        reg[ " + i + " ] " + state.register[i]);
        }
        IO.println("end state");
    }

    //แยกบิต
    // bit 24-22
    private int getOpcode(stateStruct state) {
        int count = state.pc;
        return (state.memory[count] >> 22) & 7;
    }

    // bit 21-19
    private int getregA(stateStruct state) {
        int count = state.pc;
        return (state.memory[count] >> 19 ) & 7;
    }

    // bit 18-16
    private int getregB(stateStruct state) {
        int count = state.pc;
        return (state.memory[count] >> 16 ) & 7;
    }

    // bit 2-0
    private int getdestReg(stateStruct state) {
        return state.memory[state.pc] & 7;
    }

    //bit 15 - 0
    private int getoffset(stateStruct state) {
        int count = state.pc;
        int num =  state.memory[count] & 0xFFFF;
        return convertNum(num);
    }

    //กลับบิตเลข
    private int convertNum(int num) {
        if( (num & (1 << 15)) != 0) {
            num -= (1 << 16);
        }
        return num;
    }

    //ประมวลผล
    private void executeInstruction(stateStruct state){
        countIntruction++;

        int opcode = getOpcode(state);
        int A = getregA(state);
        int B = getregB(state);
        int D = getdestReg(state);
        int offset = getoffset(state);

        switch (opcode) {
            case 0: //add
                int result = state.register[A] + state.register[B];
                if(D != 0) {
                    state.register[D] = result;
                }
                state.pc++;
                break;
            case 1: //nand
                result = ~(state.register[A] & state.register[B]);
                if(D != 0) {
                    state.register[D] = result;
                }
                state.pc++;
                break;
            case 2: //lw
                int address = state.register[A] + offset;
                if(B != 0) {
                    state.register[B] = state.memory[address];
                }
                state.pc++;
                break;
            case 3 : //sw
                int addr = state.register[A] + offset;
                state.memory[addr] = state.register[B];
                state.pc++;
                break;
            case 4 : //beq
                if(state.register[A] == state.register[B]) {
                    state.pc += 1 + offset;
                }else state.pc++;
                break;
            case 5 : //jalr
                int nextpc = state.pc + 1;
                int target = state.register[A];

                if(B != 0) {
                    state.register[B] = nextpc;
                }
                state.pc = target;
                break;
            case 6 : // halt
                state.pc++;
                halted = true;
                break;
            case 7 : //noop
                state.pc++;
                break;
        }
    }
}
