
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Token {

    public static final int NUM = 256, ID = 257, TRUE = 258, FALSE = 259, EOF = 260;
    public final int tag;

    public Token(int t) {
        tag = t;
    }

    public static class Num extends Token {

        public final int value;

        public Num(int v) {
            super(NUM);
            value = v;
        }
    }

    public static class Word extends Token {

        public final String lexeme;

        public Word(int t, String s) {
            super(t);
            lexeme = s;
        }
    }

    public static class Lexer {
        // Input stream for reading characters
        private final InputStream input;
        private char peek = ' ';
        public int line = 1;

        public Lexer(InputStream input) {
            this.input = input;
        }

        // Scans the next token
        public Token scan() throws IOException {
            // Skip whitespaces and track line numbers
            for (;; peek = (char) input.read()) {
                if (peek == ' ' || peek == '\t') {
                    continue;
                }
                if (peek == '\n') {
                    line++;
                    continue;
                }
                break;
            }

            // Handle numbers
            if (Character.isDigit(peek)) {
                int value = 0;
                if (peek == '0') {
                    peek = (char) input.read();
                    if (!Character.isDigit(peek)) {
                        return new Num(0);
                    } else {
                        throw new RuntimeException("Error: Invalid Number Format -> No Leading Zeros");
                    }
                }
                do {
                    value = 10 * value + Character.digit(peek, 10);
                    peek = (char) input.read();
                } while (Character.isDigit(peek));
                return new Num(value);
            }

            // Handle TRUE || FALSE
            if (peek == 'T') {
                if (matchWord("RUE")) {
                    return new Word(TRUE, "TRUE");
                }
            }
            if (peek == 'F') {
                if (matchWord("ALSE")) {
                    return new Word(FALSE, "FALSE");
                }
            }

            // Handle ID and Keywords
            if (Character.isLetter(peek) || peek == '_') {
                StringBuilder buffer = new StringBuilder();
                do {
                    buffer.append(peek);
                    peek = (char) input.read();
                } while (Character.isLetterOrDigit(peek) || peek == '_');
                return new Word(ID, buffer.toString());
            }

            // Handle end
            if (peek == (char) -1) {
                return new Token(EOF);
            }

            // Handle character tokens 
            Token token = new Token(peek);
            peek = ' ';
            return token;
        }

        // Helper to match a specific word (True or False)
        private boolean matchWord(String expected) throws IOException {
            for (int i = 0; i < expected.length(); i++) {
                peek = (char) input.read();
                if (peek != expected.charAt(i)) {
                    return false;
                }
            }
            peek = (char) input.read();
            return true;
        }
    }

    public static class Interpreter {

        private final Lexer lexer;
        private Token currentToken;
        private final Map<String, Integer> variables = new HashMap<>();

        public Interpreter(Lexer lexer) throws IOException {
            this.lexer = lexer;
            currentToken = lexer.scan();
        }

        public void interpret() throws IOException {
            while (currentToken != null && currentToken.tag != EOF) {
                // Handle variable
                if (currentToken instanceof Word) {
                    String varName = ((Word) currentToken).lexeme;
                    currentToken = lexer.scan();
                    if (currentToken.tag != '=') {
                        throw new RuntimeException("Error: Expected '='");
                    }
                    currentToken = lexer.scan();
                    int value = evaluateExpression();
                    variables.put(varName, value);
                    if (currentToken.tag != ';') {
                        throw new RuntimeException("Error: Expected ';'");
                    }
                    currentToken = lexer.scan();
                } else {
                    throw new RuntimeException("Error: Invalid statement");
                }
            }

            // Print values
            for (Map.Entry<String, Integer> entry : variables.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }

        private int evaluateExpression() throws IOException {
            // Evaluate the first term
            int result = evaluateTerm();
            while (currentToken != null && (currentToken.tag == '+' || currentToken.tag == '-')) {
                char operator = (char) currentToken.tag;
                currentToken = lexer.scan();

                // Evaluate next
                int nextTerm = evaluateTerm();
                if (operator == '+') {
                    result += nextTerm;
                } else if (operator == '-') {
                    result -= nextTerm;
                }
            }
            return result;
        }

        private int evaluateTerm() throws IOException {
            // Evaluate the first factor
            int result = evaluateFactor();
            while (currentToken != null && currentToken.tag == '*') {
                currentToken = lexer.scan();
                result *= evaluateFactor();
            }
            return result;
        }

        private int evaluateFactor() throws IOException {
            // Handle negative numbers
            if (currentToken.tag == '-') {
                currentToken = lexer.scan();
                return -evaluateFactor();
            }
            // Handle parentheses 
            if (currentToken.tag == '(') {
                currentToken = lexer.scan();
                int result = evaluateExpression();
                if (currentToken.tag != ')') {
                    throw new RuntimeException("Error: Missing ')'");
                }
                currentToken = lexer.scan();
                return result;
            }
            // Handle numbers
            if (currentToken instanceof Num) {
                int value = ((Num) currentToken).value;
                currentToken = lexer.scan();
                return value;
            }
            // Handle variables
            if (currentToken instanceof Word) {
                String varName = ((Word) currentToken).lexeme;
                currentToken = lexer.scan();
                if (!variables.containsKey(varName)) {
                    throw new RuntimeException("Error: Uninitialized variable " + varName);
                }
                return variables.get(varName);
            }
            throw new RuntimeException("Error: Expression is invalid");
        }
    }
}
