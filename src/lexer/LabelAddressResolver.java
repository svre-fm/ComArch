package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LabelAddressResolver {
    public List<AssemblyLine> resolve(List<AssemblyLine> lines) {
        Map<String, Integer> symbolTable = buildSymbolTable(lines);
        List<AssemblyLine> resolvedLines = new ArrayList<>();

        for (int address = 0; address < lines.size(); address++) {
            AssemblyLine line = lines.get(address);
            String field0 = line.getField0();
            String field1 = line.getField1();
            String field2 = line.getField2();

            switch (line.getInstruction().toUpperCase(Locale.ROOT)) {
                case "LW", "SW" -> field2 = resolveOffset(field2, symbolTable, address, false, line);
                case "BEQ" -> field2 = resolveOffset(field2, symbolTable, address, true, line);
                case ".FILL" -> field0 = resolveOffset(field0, symbolTable, address, false, line);
                default -> {
                }
            }

            resolvedLines.add(new AssemblyLine(line.getLineNumber(), line.getLabel(),
                    line.getInstruction(), field0, field1, field2));
            System.out.println(resolvedLines.get(address));
        }

        return resolvedLines;
    }

    private Map<String, Integer> buildSymbolTable(List<AssemblyLine> lines) {
        Map<String, Integer> symbolTable = new HashMap<>();
        for (int address = 0; address < lines.size(); address++) {
            AssemblyLine line = lines.get(address);
            if (!line.hasLabel()) {
                continue;
            }

            String key = line.getLabel().toLowerCase();
            if (symbolTable.putIfAbsent(key, address) != null) {
                throw resolutionError(line, "duplicate label '" + line.getLabel() + "'");
            }
        }
        return symbolTable;
    }

    private String resolveOffset(String text, Map<String, Integer> symbolTable,
                                int currentAddress, boolean relative, AssemblyLine line) {
        if (isInteger(text)) {
            return text;
        }

        Integer labelAddress = symbolTable.get(text.toLowerCase());
        if (labelAddress == null) {
            throw resolutionError(line, "undefined label '" + text + "'");
        }

        int Offset;
        if(relative){
            Offset =labelAddress - (currentAddress + 1);
        }else {
            Offset = labelAddress;
        }
        return Integer.toString(Offset);
    }

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

    private IllegalArgumentException resolutionError(AssemblyLine line, String message) {
        return new IllegalArgumentException("Address resolution error at source line "
                + line.getLineNumber() + ": " + message);
    }
}