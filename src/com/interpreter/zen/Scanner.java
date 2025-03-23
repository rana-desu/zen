package com.interpreter.zen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* static imports:
   allows us to use the static members from a class
   without specifying the classname. 
 */
import static com.interpreter.zen.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // keeping tack of where the scanner is in the src code.
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // mapping a specific token type to each lexeme identified.
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("none",   NONE);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    /* raw source code is stored as a simple string.
       tokens ArrayList is used to store generated tokens,
       as the scanner progresses through the source code.
     */
    Scanner(String source) {
        this.source = source;
    }

    /* scans through the entire string until it reaches the end,
       adding the generated token to the ArrayList,
       then it appends one final EOF token at the end. 
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
    
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            case '!':
                addToken(match('=') ? NOT_EQUAL : NOT);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '<':
                addToken(match('-') ? LESS_EQUAL : LESS);
            
            case '/':
                // if "//", then it's a comment
                // a comment goes until EOL.
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
            
            // ignore whitespace.
            case ' ':
            case '\r':
            case '\t':
                break;
            
            case '\n':
                line++;
                break;

            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Zen.error(line, "Unexpected character.");
                }
                break;
        }
    }
    
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // returns the *next* character in the src string.
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // extract the lexeme and add it as a token.
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void string() {
        // identify the end of the string literal,
        // if there's a newline, then increment line counter
        // and advance to next character.
        while (peek() != '"' && !isAtEnd()) {
            if (peek() != '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Zen.error(line, "Unterminated string literal.");
            return;
        }

        // consume the terminating ".
        advance();

        // trim the quotes, and add the token with literal.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        // keep advancing until the lookahead character is a digit.
        while (isDigit(peek())) advance();

        // check for a fractional part,
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the "."
            advance();

            // keep advancing after decimal until digits exist.
            while (isDigit(peek())) advance();
        }

        // parses the string into a numeric value and then adds the token and literal.
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // identifier also handles reserved keywords,
    // since they are identifiers reserved for the interpreter's use.
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String lexeme = source.substring(start, current);
        TokenType type = keywords.get(lexeme);
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }

    private boolean match(char expectedNextChar) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expectedNextChar) return false;

        current++;
        return true;
    }

    // this is a lookahead method, it doesn't consume chars unlike advance()
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // scanner looksahead at most for two characters.
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // Character.isDigit() allows Devanagari digits??? LMAO.
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
