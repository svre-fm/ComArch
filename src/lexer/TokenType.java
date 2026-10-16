package lexer;

public enum TokenType {
    MNEMONIC,   // machine instruction: ADD, NAND, LOAD, STORE, BEQ, JALR, HALT, NOOP
    DIRECTIVE,  // pseudo-op ".fill"
    NUMBER,     // integer literal (positive/negative), e.g. 5, -1, 32766
    IDENTIFIER, // Label address referencing
    NEWLINE,    // end of one instruction line
    EOF         // end of file
}
