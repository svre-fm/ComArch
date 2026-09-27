package lexer;

import java.util.*;

public class Lexer {
     /* R-type (add, nand)  : regA regB destReg     -> 3 fields
     * I-type (lw, sw, beq): regA regB offsetField -> 3 fields
     * J-type (jalr)       : regA regB             -> 2 fields
     * O-type (halt, noop) : ไม่มี field             -> 0 fields
     */
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

    public List<Token> tokenize(){
        List<Token> tokens = new ArrayList<>();
        List<Token> lineBuffer = new ArrayList<>();

        while (true){
            Token raw = scanRawToken();
            if(raw.getType() == TokenType.NEWLINE){
                if(!lineBuffer.isEmpty()){
                    classifyLine(lineBuffer,tokens);
                    tokens.add(raw);
                }
                lineBuffer.clear();
                continue;
            }
            if (raw.getType() == TokenType.EOF){
                if (!lineBuffer.isEmpty()){
                    classifyLine(lineBuffer,tokens);
                    tokens.add(new Token(TokenType.NEWLINE,"\n", raw.getLine(), raw.getColumn()));
                }
                tokens.add(raw);
                break;
            }
            lineBuffer.add(raw);
        }
        return tokens;
    }

    private Token scanRawToken(){
        skipWhitespaceAndComments();
        if (isAtEnd()){
            return new Token(TokenType.EOF,"",line,column);
        }
        char c = peek();
        if (c == '\n'){
            Token tok = new Token(TokenType.NEWLINE,"\n",line,column);
            advance();
            return tok;
        }
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
            sb.append(advance());
        }
        String word = sb.toString();

        if (word.equalsIgnoreCase(".fill")) {
            return new Token(TokenType.DIRECTIVE, ".fill", startLine, startCol);
        }
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
        if( word.charAt(0) == '-' ) i = 1;
        for (; i < word.length();i++){
            if (!Character.isDigit(word.charAt(i))) return false;
        }
        return true;
    }

    private boolean isBlobChar(char c) {
        return c != ' ' && c != '\t' && c != '\r' && c != '\n';
    }

    private boolean isAtEnd() {
        return pos >= src.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : src.charAt(pos);
    }

    private char peekNext() {
        return (pos + 1 >= src.length()) ? '\0' : src.charAt(pos + 1);
    }

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
        boolean ok = !word.isEmpty() && word.length() <= 6 && Character.isLetter(word.charAt(0));
        if(ok){
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

    private void classifyLine(List<Token> lineBuffer,List<Token> output){
        int idx = 0;
        int fieldCount = 0;
        Token current = lineBuffer.get(idx);

        boolean isDirective = false,isMnemonic = false;
        if(current.getType() == TokenType.DIRECTIVE)
            isDirective = true;
        if(current.getType() == TokenType.MNEMONIC && OPCODE.containsKey(current.getLexeme().toUpperCase()))
            isMnemonic = true;

        //first word isn't instruction or .fill -> label
        if(!isDirective && !isMnemonic){
            validLabel(current);
            output.add(new Token(TokenType.LABEL,current.getLexeme(),current.getLine(),current.getColumn()));
            idx++;

            if(idx >= lineBuffer.size()){
                throw new LexicalException("have label but don't have instruction",current.getLine(),current.getColumn());
            }
            current = lineBuffer.get(idx);
            if(current.getType() == TokenType.DIRECTIVE)
                isDirective = true;
            if(current.getType() == TokenType.MNEMONIC && OPCODE.containsKey(current.getLexeme().toUpperCase()))
                isMnemonic = true;
        }

        if (isDirective){
            output.add(new Token(TokenType.DIRECTIVE,".fill",current.getLine(),current.getColumn()));
            fieldCount = 1;
        }else if ( isMnemonic ){
            output.add(new Token(TokenType.MNEMONIC,current.getLexeme(),current.getLine(),current.getColumn()));
            fieldCount = OPCODE.get(current.getLexeme().toUpperCase());
        }
        idx++;

        for (int f = 0; f < fieldCount; f++) {
            if (idx >= lineBuffer.size()) {
                throw new LexicalException(
                        "instruction need " + fieldCount + " field but not provided", current.getLine(), current.getColumn()
                );
            }
            Token fieldTok = lineBuffer.get(idx);
            if (fieldTok.getType() == TokenType.NUMBER) {
                output.add(fieldTok);
            } else {
                validLabel(fieldTok);
                output.add(new Token(TokenType.IDENTIFIER, fieldTok.getLexeme(), fieldTok.getLine(), fieldTok.getColumn()));
            }
            idx++;
        }
        //have some token left it should be comment.
        if ( idx < lineBuffer.size()){
            Token leftOver = lineBuffer.get(idx);
            StringBuilder comment = new StringBuilder();
            for(int c = idx ; c < lineBuffer.size();c++){
                if(c > idx) comment.append(' ');
                comment.append(lineBuffer.get(c).getLexeme());
            }
            output.add(new Token(TokenType.COMMENT,comment.toString(),leftOver.getLine(),leftOver.getColumn()));
        }
    }
}
