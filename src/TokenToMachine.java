public class TokenToMachine {

    public String encode(String[] tokens) {
        if (tokens == null || tokens.length == 0) return null;

        String mnemonic = tokens[0].toLowerCase();

        switch (mnemonic) {
            case "add":
                return encodeRType(0, tokens); // opcode 000[cite: 2]
            case "nand":
                return encodeRType(1, tokens); // opcode 001[cite: 2]
            case "lw":
                return encodeIType(2, tokens); // opcode 010[cite: 2]
            case "sw":
                return encodeIType(3, tokens); // opcode 011[cite: 2]
            case "beq":
                return encodeIType(4, tokens); // opcode 100[cite: 2]
            case "jalr":
                return encodeJType(5, tokens); // opcode 101[cite: 2]
            case "halt":
                return encodeOType(6);         // opcode 110[cite: 2]
            case "noop":
                return encodeOType(7);         // opcode 111[cite: 2]
            case ".fill":
                return encodeFill(tokens);     // Pseudo-instruction[cite: 3]
            default:
                throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
        }
    }

    // --- R-Type: add, nand (field0=regA, field1=regB, field2=destReg)[cite: 3] ---
    private String encodeRType(int opcode, String[] tokens) {
        int rs = parseNumber(tokens[1]); // regA (rs)[cite: 3]
        int rt = parseNumber(tokens[2]); // regB (rt)[cite: 3]
        int rd = parseNumber(tokens[3]); // destReg (rd)[cite: 3]

        // Format: [24-22] opcode | [21-19] rs | [18-16] rt | [15-3] 0 | [2-0] rd
        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rt & 0x7) << 16)
                | (rd & 0x7); // บิตที่ 15-3 จะเป็น 0 โดยอัตโนมัติจากการเลื่อนบิตข้ามไป

        return String.valueOf(machineCode); // คืนค่าเป็น String ของเลขฐาน 10[cite: 3, 4]
    }

    // --- I-Type: lw, sw, beq (field0=regA, field1=regB, field2=offsetField)[cite: 3] ---
    private String encodeIType(int opcode, String[] tokens) {
        int rs = parseNumber(tokens[1]); // regA (rs)[cite: 3]
        int rt = parseNumber(tokens[2]); // regB (rt)[cite: 3]
        int offset = parseNumber(tokens[3]); // offsetField[cite: 3]

        // Format: [24-22] opcode | [21-19] rs | [18-16] rt | [15-0] offset
        // & 0xFFFF เพื่อตัดให้เหลือเฉพาะ 16-bit ล่าง สำหรับกรณีเป็นเลขติดลบ (2's complement)[cite: 1]
        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rt & 0x7) << 16)
                | (offset & 0xFFFF);

        return String.valueOf(machineCode);
    }

    // --- J-Type: jalr (field0=regA, field1=regB)[cite: 3] ---
    private String encodeJType(int opcode, String[] tokens) {
        int rs = parseNumber(tokens[1]); // regA (rs)[cite: 3]
        int rd = parseNumber(tokens[2]); // regB (rd)[cite: 3]

        // Format: [24-22] opcode | [21-19] rs | [18-16] rd | [15-0] 0[cite: 1]
        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rd & 0x7) << 16);

        return String.valueOf(machineCode);
    }

    // --- O-Type: halt, noop (ไม่มี field)[cite: 3] ---
    private String encodeOType(int opcode) {
        // Format: [24-22] opcode | [21-0] 0[cite: 1]
        int machineCode = (opcode << 22);

        return String.valueOf(machineCode);
    }

    // --- .fill (field0=value)[cite: 3] ---
    private String encodeFill(String[] tokens) {
        // แปลงค่า field ท้ายสุดให้เป็นตัวเลขตรงๆ[cite: 3]
        return String.valueOf(parseNumber(tokens[1]));
    }

    // ฟังก์ชันช่วยแปลงค่า (สมมติว่า Parser ได้แปลง Label เป็นตัวเลข Address หรือ Offset มาให้แล้ว)
    private int parseNumber(String token) {
        return Integer.parseInt(token.trim());
    }

    public static void main(String[] args) {
        TokenToMachine encoder = new TokenToMachine();

        // ทดสอบจากภาพที่ 3 (start add 1 2 1)[cite: 3]
        String[] addTokens = {".fill", "-1"};
        System.out.println(encoder.encode(addTokens));
    }
}