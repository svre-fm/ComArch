package lexer;

public final class AssemblyLine {
    private final int lineNumber;
    private final String label;
    private final String instruction;  // ADD, LOAD,..., .fill
    private final String field0;
    private final String field1;
    private final String field2;

    public AssemblyLine(int lineNumber, String label, String instruction,
                        String field0, String field1, String field2) {
        this.lineNumber = lineNumber;
        this.label = label;
        this.instruction = instruction;
        this.field0 = field0;
        this.field1 = field1;
        this.field2 = field2;
    }

    public int getLineNumber()     { return lineNumber; }
    public String getLabel()       { return label; }
    public String getInstruction() { return instruction; }
    public String getField0()      { return field0; }
    public String getField1()      { return field1; }
    public String getField2()      { return field2; }

    public boolean hasLabel() { return !label.isEmpty(); }

    @Override
    public String toString() {
        return String.format("line %-2d [%s]\t[%s]\t[%s]\t[%s]\t[%s]",
                lineNumber, label, instruction, field0, field1, field2);
    }
}