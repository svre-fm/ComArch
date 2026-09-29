import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TokenToMachine {

    public void encodeAll(List<List<String>> program) throws IOException {
        List<String> machineCodes = new ArrayList<>();

        for (List<String> line : program) {
            String code = encode(line);
            machineCodes.add(code);
        }

        Files.write(Paths.get("MachineCode.txt"), machineCodes);
    }

    public String encode(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return null;

        String mnemonic = tokens.get(0).toLowerCase();

        switch (mnemonic) {
            case "add":
                return encodeRType(0, tokens); // opcode 000
            case "nand":
                return encodeRType(1, tokens); // opcode 001
            case "lw":
                return encodeIType(2, tokens); // opcode 010
            case "sw":
                return encodeIType(3, tokens); // opcode 011
            case "beq":
                return encodeIType(4, tokens); // opcode 100
            case "jalr":
                return encodeJType(5, tokens); // opcode 101
            case "halt":
                return encodeOType(6);         // opcode 110
            case "noop":
                return encodeOType(7);         // opcode 111
            case ".fill":
                return encodeFill(tokens);     // Pseudo-instruction
            default:
                throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
        }
    }

    // --- R-Type: add, nand (field0=regA, field1=regB, field2=destReg) ---
    private String encodeRType(int opcode, List<String> tokens) {
        int rs = parseNumber(tokens.get(1)); // regA (rs)
        int rt = parseNumber(tokens.get(2)); // regB (rt)
        int rd = parseNumber(tokens.get(3)); // destReg (rd)

        // Format: [24-22] opcode | [21-19] rs | [18-16] rt | [15-3] 0 | [2-0] rd
        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rt & 0x7) << 16)
                | (rd & 0x7); // บิตที่ 15-3 จะเป็น 0 โดยอัตโนมัติ

        return String.valueOf(machineCode);
    }

    // --- I-Type: lw, sw, beq (field0=regA, field1=regB, field2=offsetField) ---
    private String encodeIType(int opcode, List<String> tokens) {
        int rs = parseNumber(tokens.get(1)); // regA (rs)
        int rt = parseNumber(tokens.get(2)); // regB (rt)
        int offset = parseNumber(tokens.get(3)); // offsetField

        // Format: [24-22] opcode | [21-19] rs | [18-16] rt | [15-0] offset
        // & 0xFFFF เพื่อตัดให้เหลือ 16-bit ล่าง สำหรับเลขติดลบ (2's complement)
        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rt & 0x7) << 16)
                | (offset & 0xFFFF);

        return String.valueOf(machineCode);
    }

    // --- J-Type: jalr (field0=regA, field1=regB) ---
    private String encodeJType(int opcode, List<String> tokens) {
        int rs = parseNumber(tokens.get(1)); // regA (rs)
        int rd = parseNumber(tokens.get(2)); // regB (rd)

        // Format: [24-22] opcode | [21-19] rs | [18-16] rd | [15-0] 0
        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rd & 0x7) << 16);

        return String.valueOf(machineCode);
    }

    // --- O-Type: halt, noop (ไม่มี field) ---
    private String encodeOType(int opcode) {
        // Format: [24-22] opcode | [21-0] 0
        int machineCode = (opcode << 22);

        return String.valueOf(machineCode);
    }

    // --- .fill (field0=value) ---
    private String encodeFill(List<String> tokens) {
        return String.valueOf(parseNumber(tokens.get(1)));
    }

    private int parseNumber(String token) {
        return Integer.parseInt(token.trim());
    }
}