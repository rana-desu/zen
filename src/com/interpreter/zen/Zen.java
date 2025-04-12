package com.interpreter.zen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Zen {
    // static so that the REPL session utilises the same instance of the interpreter
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false; // flag to ensure execution of error-free code
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // read bytes from the file at path, store those bytes in an array of type byte
    // then, "run"/execute the read bytes by converting it into a String object.
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // indicates an error in the exit-code.
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    // interactive mode, if interpreter is executed without any args
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();

            // if end-of-line/ctrl+d, readLine retunrs null
            if (line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        /*
         * prints scanned tokens after lexxing.
         * for (Token token : tokens) {
         * System.out.println(token);
         * }
         */

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError)
            return;

        // System.out.println(new ASTPrinter().print(expression));
        interpreter.interpret(statements);
    }

    // it's a good engineering practice to:
    // separate the code that generates the error from the code that reports them.
    // minimal bit of error-handling
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    /*
     * in case a syntax error is encountered,
     * detect the error, and report it to the user.
     * 
     * tokens are used to track locations throughout the code.
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
            "\n[line " + error.token.line + "]");

        hadRuntimeError = true;
    }
}
