package TokenToMachine;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TokenToMachine {

    public void encodeAll(List<List<String>> program) {
        List<String> machineCodes = new ArrayList<>();
        int saveLine = 0;

        try {
            for (List<String> line : program) {
                saveLine++;
                String code = encode(line);
                machineCodes.add(code);
            }
            Files.write(Paths.get("MachineCode.txt"), machineCodes);
        }catch(Exception e) {
            System.err.println("Incorrect Token at Line" + " " + saveLine);
        }
    }

    private String encode(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return null;

        String mnemonic = tokens.get(0).toLowerCase();

        switch (mnemonic) {
            case "add":
                return encodeRType(0, tokens);
            case "nand":
                return encodeRType(1, tokens);
            case "lw":
                return encodeIType(2, tokens);
            case "sw":
                return encodeIType(3, tokens);
            case "beq":
                return encodeIType(4, tokens);
            case "jalr":
                return encodeJType(5, tokens);
            case "halt":
                return encodeOType(6);
            case "noop":
                return encodeOType(7);
            case ".fill":
                return encodeFill(tokens);
            default:
                throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
        }
    }

    private String encodeRType(int opcode, List<String> tokens) {
        int rs = parseNumber(tokens.get(1));
        int rt = parseNumber(tokens.get(2));
        int rd = parseNumber(tokens.get(3));

        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rt & 0x7) << 16)
                | (rd & 0x7);

        return String.valueOf(machineCode);
    }

    private String encodeIType(int opcode, List<String> tokens) {
        int rs = parseNumber(tokens.get(1));
        int rt = parseNumber(tokens.get(2));
        int offset = parseNumber(tokens.get(3));
        validateOffset(offset);

        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rt & 0x7) << 16)
                | (offset & 0xFFFF);

        return String.valueOf(machineCode);
    }

    private String encodeJType(int opcode, List<String> tokens) {
        int rs = parseNumber(tokens.get(1));
        int rd = parseNumber(tokens.get(2));

        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rd & 0x7) << 16);

        return String.valueOf(machineCode);
    }

    private String encodeOType(int opcode) {
        int machineCode = (opcode << 22);
        return String.valueOf(machineCode);
    }

    private String encodeFill(List<String> tokens) {
        return String.valueOf(parseNumber(tokens.get(1)));
    }

    private int parseNumber(String token) {
        return Integer.parseInt(token.trim());
    }

    private void validateOffset(int offset) {
        if (offset < -32768 || offset > 32767) {
            System.err.println("Invalid offset: " + offset);
            System.exit(1);
        }
    }
}