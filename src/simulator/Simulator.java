package simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Simulator {
    //set up ค่าเริ่มต้นของ Simulator
    private boolean halted = false;
    private int countIntructions = 0;
    final int MAX_INSTRUCTIONS = 500000;

    //function สำหรับแสดงผลการทำงานของ Simulator
    public void run(String filename, stateStruct state) {
        //ทดสอบการอ่านไฟล์
        try {
            ReadMachinecode(filename,state);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // แสดงผลค่า Machine code ในแต่ละ memory
        for (int i = 0; i < state.numMemory; i++) {
            IO.println("memory[" + i + "] = " + state.memory[i]);
        }
        IO.println();

        //ลูปการทำงานของ Simulator
        while(!halted){
            if (countIntructions > MAX_INSTRUCTIONS) {
                System.out.println("error: program did not halt within " + MAX_INSTRUCTIONS + " instructions (possible infinite loop)");
                System.exit(1);
            }

            checkMemoryBounds(state.pc, state);

            printState(state);
            executeInstruction(state);
        }

        //แสดงผลลัพธ์การทำงานของ Simulator
        IO.println("machine halted");
        IO.println("total of " + countIntructions + " instructions executed");
        IO.println("final state of machine : ");
        printState(state);
    }

    //function  การอ่านไฟล์ Machine code
    private void ReadMachinecode(String filename, stateStruct state) throws FileNotFoundException {

        Scanner scanner = new Scanner(new File(filename));

        while (scanner.hasNextLine()) {

            int value = Integer.parseInt(scanner.nextLine());

            state.memory[state.numMemory] = value;
            state.numMemory++;
        }

        scanner.close();
    }

    //function printState ตาม workspace
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

    //Decode Machine code มาเป็น Opcode, field 0,field 1, field2

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

    //ประมวลผล Instructions
    private void executeInstruction(stateStruct state){
        countIntructions++;
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

    // check ขอบเขต address
    private void checkMemoryBounds(int address, stateStruct state) {
        if (address < 0 || address >= state.numMemory) {
            System.out.println("error: memory address out of bounds: " + address);
            System.exit(1);
        }
    }

}
