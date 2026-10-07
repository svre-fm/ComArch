package lexer;

public enum TokenType {
    LABEL,      // label declaration at the start of a line, e.g. "start", "five" Also use in address referencing another label , e.g. in LOAD/BEQ/.fill fields
    MNEMONIC,   // machine instruction: ADD, NAND, LOAD, STORE, BEQ, JALR, HALT, NOOP
    DIRECTIVE,  // pseudo-op ".fill"
    NUMBER,     // integer literal (positive/negative), e.g. 5, -1, 32766
    NEWLINE,    // end of one instruction line
    EOF         // end of file
}
