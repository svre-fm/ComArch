package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//สำหรับแปลงชื่อ Label เป็นตัวเลข Address หรือ Offset
public final class LabelAddressResolver {

    //หลักสำหรับแปลง Label ทั้งหมดในโปรแกรมเป็นตัวเลข
    public List<AssemblyLine> resolve(List<AssemblyLine> lines) {
        //สร้าง Symbol Table เก็บตำแหน่งของทุก Label (Pass 1)
        Map<String, Integer> symbolTable = buildSymbolTable(lines);
        List<AssemblyLine> resolvedLines = new ArrayList<>();

        //วนลูปแปลงค่า Label แต่ละบรรทัดเป็นตัวเลข
        for (int address = 0; address < lines.size(); address++) {
            AssemblyLine line = lines.get(address);
            String field0 = line.getField0();
            String field1 = line.getField1();
            String field2 = line.getField2();

            //เช็คประเภทคำสั่งเพื่อเลือกแปลง field ที่มีการอ้างอิงถึง Label
            switch (line.getInstruction().toUpperCase(Locale.ROOT)) {
                case "LW", "SW" -> field2 = resolveOffset(field2, symbolTable, address, false, line);
                case "BEQ" -> field2 = resolveOffset(field2, symbolTable, address, true, line);
                case ".FILL" -> field0 = resolveOffset(field0, symbolTable, address, false, line);
                default -> {
                }
            }

            resolvedLines.add(new AssemblyLine(line.getLineNumber(), line.getLabel(),
                    line.getInstruction(), field0, field1, field2));
        }

        return resolvedLines;
    }

    //สร้าง Symbol Table จับคู่ชื่อ Label กับเลขบรรทัด Address
    private Map<String, Integer> buildSymbolTable(List<AssemblyLine> lines) {
        Map<String, Integer> symbolTable = new HashMap<>();
        for (int address = 0; address < lines.size(); address++) {
            AssemblyLine line = lines.get(address);

            //ถ้าบรรทัดนี้ไม่มี Label ให้ข้ามไป
            if (!line.hasLabel()) {
                continue;
            }

            //นำชื่อ Label ใส่ตารางแปลงเป็นตัวพิมพ์เล็กถ้าเจอชื่อซ้ำให้แจ้ง error
            String key = line.getLabel().toLowerCase();
            if (symbolTable.putIfAbsent(key, address) != null) {
                throw resolutionError(line, "duplicate label '" + line.getLabel() + "'");
            }
        }
        return symbolTable;
    }

    //แปลงค่าข้อความหรือ Label ให้เป็นตัวเลข Address หรือ Offset
    private String resolveOffset(String text, Map<String, Integer> symbolTable,
                                 int currentAddress, boolean relative, AssemblyLine line) {
        //ถ้าเป็นตัวเลขอยู่แล้วคืนค่าเดิม
        if (isInteger(text)) {
            return text;
        }

        //ค้นหา Address ของ Label จาก Symbol Table
        Integer labelAddress = symbolTable.get(text.toLowerCase());
        if (labelAddress == null) {
            throw resolutionError(line, "undefined label '" + text + "'");
        }

        //คำนวณค่าถ้าเป็น relative เช่น BEQ ให้คิดแบบ Offset: target - (current + 1)
        int Offset;
        if (relative) {
            Offset = labelAddress - (currentAddress + 1);
        } else {
            Offset = labelAddress;
        }
        return Integer.toString(Offset);
    }

    //ตรวจสอบว่าข้อความส่งมาเป็นตัวเลขหรือไม่
    private boolean isInteger(String text) {
        if (text.isEmpty()) {
            return false;
        }

        int start = text.charAt(0) == '-' ? 1 : 0;
        if (start == text.length()) {
            return false;
        }
        for (int index = start; index < text.length(); index++) {
            if (!Character.isDigit(text.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    //สำหรับสร้าง Exception แจ้ง Error เเละบอกเลขบรรทัดที่เกิดปัญหา
    private IllegalArgumentException resolutionError(AssemblyLine line, String message) {
        return new IllegalArgumentException("Address resolution error at source line "
                + line.getLineNumber() + ": " + message);
    }
}