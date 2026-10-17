package lexer;

import java.util.*;

public class Lexer {
     // R-type (add, nand)  : regA regB destReg     -> 3 fields
     // I-type (lw, sw, beq): regA regB offsetField -> 3 fields
     // J-type (jalr)       : regA regB             -> 2 fields
     // O-type (halt, noop) : ไม่มี field             -> 0 fields
    private static final Map<String, Integer> OPCODE = new TreeMap<>(Map.of(
            "ADD", 3,
            "NAND", 3,
            "LW", 3,
            "SW", 3,
            "BEQ", 3,
            "JALR", 2,
            "HALT", 0,
            "NOOP", 0
    ));

    private final String src;
    private int pos;
    private int line;
    private int column;

    public Lexer(String src) {
        this.src = src;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
    }

    public List<AssemblyLine> tokenize() {
        List<AssemblyLine> lines = new ArrayList<>();
        List<Token> lineBuffer = new ArrayList<>();

        while (true) {
            //get each token
            Token raw = scanRawToken();
            TokenType type = raw.getType();

            if (type == TokenType.NEWLINE || type == TokenType.EOF) {
                if (!lineBuffer.isEmpty()) {
                    lines.add(classifyLine(lineBuffer));
                    //comment is removed by .clear() below
                    //classifyLine() read only necessary filed
                    //check by filed count
                    lineBuffer.clear();
                }
                if (type == TokenType.EOF) break;
                //type == TokenType.NEWLINE skip to next loop
                continue;
            }
            IO.println(raw);
            lineBuffer.add(raw);
        }
        return lines;
    }

    private Token scanRawToken(){
        skipWhitespaceAndComments();
        if (isAtEnd()){
            return new Token(TokenType.EOF,"",line,column);
        }
        //lookahead token
        char c = peek();
        if (c == '\n'){
            Token tok = new Token(TokenType.NEWLINE,"\n",line,column);
            advance();
            return tok;
        }
        //scan each set of character stream then return it to token
        return scanBlob();
    }


    private void skipWhitespaceAndComments(){
        while (!isAtEnd()){
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r'){
                advance();
            }else break;
        }
    }

    private Token scanBlob(){
        int startLine = line,startCol = column;
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && isBlobChar(peek())) {
            //update position++ line++ amd column++
            sb.append(advance());
        }
        String word = sb.toString();

        //if word is ".fill"
        if (word.equalsIgnoreCase(".fill")) {
            return new Token(TokenType.DIRECTIVE, ".fill", startLine, startCol);
        }
        //if word is in opcode instruction
        if (OPCODE.containsKey(word.toUpperCase())) {
            return new Token(TokenType.MNEMONIC, word, startLine, startCol);
        }
        if (isNumberLiteral(word)) {
            return new Token(TokenType.NUMBER, word, startLine, startCol);
        }
        return new Token(TokenType.IDENTIFIER,word,startLine,startCol);
    }

    private boolean isNumberLiteral(String word){
        int i = 0;
        if( word.isEmpty()) return false;
        //i first char is "-" mean negative set i to next char
        if( word.charAt(0) == '-' ) i = 1;
        for (; i < word.length();i++){
            //if not digit
            if (!Character.isDigit(word.charAt(i))) return false;
        }
        return true;
    }

    //tab ,enter \r, \r -> newline
    private boolean isBlobChar(char c) {
        return c != ' ' && c != '\t' && c != '\r' && c != '\n';
    }

    //src.lenge is return amount of char in the string
    private boolean isAtEnd() {
        return pos >= src.length();
    }

    //look at position
    private char peek() {
        return isAtEnd() ? '\0' : src.charAt(pos);
    }

    //update next line use for list<token> is success
    private char advance() {
        char c = src.charAt(pos);
        pos++;
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private void validLabel(Token tok){
        String word = tok.getLexeme();
        //ok when first char is letter
        boolean ok = !word.isEmpty() && word.length() <= 6 && Character.isLetter(word.charAt(0));
        if(ok){
            //check each char that is letter or digit
            for (int i = 1;i < word.length();i++){
                if(!Character.isLetterOrDigit(word.charAt(i))){
                    ok = false;
                    break;
                }
            }
        }
        if(!ok){
            throw new LexicalException(
                    "label " + word + " Invalid (must start with a letter; maximum length of 6 characters)",
                    tok.getLine(),tok.getColumn()
            );
        }
    }

    private AssemblyLine classifyLine(List<Token> lineBuffer){
        int idx = 0;
        Token current = lineBuffer.get(idx);
        int lineNumber = current.getLine();

        boolean isDirective = false;
        boolean isMnemonic = false;

        //check is it a directive(.fill) or mnemonic(instruction)
        if(current.getType() == TokenType.DIRECTIVE)
            isDirective = true;
        if(current.getType() == TokenType.MNEMONIC && OPCODE.containsKey(current.getLexeme().toUpperCase()))
            isMnemonic = true;

        //if it not isDirective && isMnemonic ,it has to be 'lable'
        String label = "";
        if(!isDirective && !isMnemonic){
            validLabel(current);
            label = current.getLexeme();
            idx++;

            //first is label but don't have field next token
            if(idx >= lineBuffer.size()){
                throw new LexicalException("have label but don't have instruction",current.getLine(),current.getColumn());
            }

            //lookahead next token(by idx++ above) and set it as current token
            current = lineBuffer.get(idx);
            if(current.getType() == TokenType.DIRECTIVE) isDirective = true;
            //Is it intruction that we want.
            if(current.getType() == TokenType.MNEMONIC && OPCODE.containsKey(current.getLexeme().toUpperCase()))
                isMnemonic = true;
        }

        String instruction;
        int fieldCount;
        if(isDirective){
            instruction = ".fill";
            fieldCount = 1;
        }else if (isMnemonic){
            instruction = current.getLexeme().toUpperCase();
            fieldCount = OPCODE.get(instruction);
        }else {
            throw new LexicalException("unknow instruction or directive " + current.getLexeme() ,current.getLine(),current.getColumn());
        }
        idx++;

        String[] fields = {"","",""};
        for (int f = 0;f < fieldCount;f++){
            if (idx >= lineBuffer.size()){
                throw new LexicalException("instruction need " + fieldCount + " field but not provided", current.getLine(), current.getColumn());
            }
            Token fieldTok = lineBuffer.get(idx);
            if (fieldTok.getType() != TokenType.NUMBER){
                validLabel(fieldTok);
            }
            fields[f] = fieldTok.getLexeme();
            idx++;
        }
    return new AssemblyLine(lineNumber,label,instruction,fields[0],fields[1],fields[2]);
    }
}
