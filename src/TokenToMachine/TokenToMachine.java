package TokenToMachine;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TokenToMachine {

    public void encodeAll(List<List<String>> AllList) { //List ข้างใน(List ย่อย)จะเก็บ Token instruction ส่วน List ข้างนอกจะเก็บ List ย่อยอีกที
        List<String> machineCodes = new ArrayList<>(); //สร้างตัวแปรเพื่อเก็บค่า Machine Code
        int saveLine = 0; //สร้างตัวแปรนับจำนวนบรรทัด

        try {
            for (List<String> line : AllList) { //line จะไปดึง List ย่อยออกมาจาก AllList
                saveLine++; //นับจำนวนบรรทัด
                String code = encode(line); //เรียกใช้ Function encode แล้วส่ง List ของ line เข้าไป
                machineCodes.add(code); //เก็บค่าของ Machine Code ที่ถูกแปลงแล้ว ลงใน List
            }
            Files.write(Paths.get("MachineCode.txt"), machineCodes); //เอาผลลัพธ์ของ machine code ที่เก็บใน List มาเขียนลงในไฟล์ Text
        }catch(Exception e) {
            System.err.println(e.getMessage()); //เจอข้อผิดพลาด แสดงผล
            System.exit(1);
        }
    }

    private String encode(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return null;

        String mnemonic = tokens.get(0).toLowerCase(); //สร้างตัวแปรที่จะเอา token ตัวแรกที่อยู่ใน List มาทำให้เป็นตัวเล็กทั้งหมด

        switch (mnemonic) { //swicth มีหน้าที่เช็คจาก Token ตัวแรกของ List ว่าควรจะส่งไปเข้าที่ case ไหน
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
                throw new IllegalArgumentException("Unknown instruction: " + mnemonic); //ถ้าไม่ตรงกับ case ไหนเลย throw exception
        }
    }

    private String encodeRType(int opcode, List<String> tokens) {
        int rs = parseNumber(tokens.get(1)); //เอาไปแปลงจาก string ให้เป็น int
        int rt = parseNumber(tokens.get(2));
        int rd = parseNumber(tokens.get(3));

        int machineCode = (opcode << 22) //shift left 22 bit
                | ((rs & 0x7) << 19) //0x7 มีหน้าที่ในการจำกัดขนาด bit ไม่ให้เกิน 3 ตัว
                | ((rt & 0x7) << 16) //0x7(0111 ฐานสอง) เมื่อนำไป and กับ bit ดังกล่าวจะทำให้ผลที่ได้ไม่เกิน 3 bit
                | (rd & 0x7);

        return String.valueOf(machineCode); //return ค่า machine code ที่ถูกแปลงเป็น String
    }

    private String encodeIType(int opcode, List<String> tokens) {
        int rs = parseNumber(tokens.get(1));
        int rt = parseNumber(tokens.get(2));
        int offset = parseNumber(tokens.get(3));
        validateOffset(offset); //เช็ค value ของ offset ไม่ให้เกิน 16 bit

        int machineCode = (opcode << 22)
                | ((rs & 0x7) << 19)
                | ((rt & 0x7) << 16)
                | (offset & 0xFFFF); //จำกัดให้ offset มีขนาดได้ไม่เกิน 16 bit

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
        return Integer.parseInt(token.trim()); //รับ Token เข้ามา ตัดช่องว่างหัวท้ายทิ้ง แปลง string เป็น int
    }

    private void validateOffset(int offset) {
        if (offset < -32768 || offset > 32767) { //กำหนดช่วงที่ห้ามเกินหรือต่ำกว่า
            System.err.println("Invalid offset: " + offset);
            System.exit(1);
        }
    }
}