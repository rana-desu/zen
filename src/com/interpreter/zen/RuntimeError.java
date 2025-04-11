package com.interpreter.zen;

class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    /* unlike java exception classes,
     * this implementation tracks the erroneous token
     * reason: helps the user know where to fix their code.
     */
}
