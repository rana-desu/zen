# zen: Interpreted, Dynamically Typed, and Object-Oriented Programming Language

*zen*, is a simple take on an interpreted programming langauge, crafted by following the implementation of the *lox* interpreter in Java.

## Build and Run
1. Clone the repository and change directory: 
```bash
git clone https://github.com/rana-desu/zen.git && cd zen
```

2. Compile the project:
```bash
javac -d target/ src/com/interpreter/zen/*.java
```

3. Run the interpreter, can be used in two different modes:
- Prompt-based mode, do not provide any arguments while running the interpreter.
```bash
java -cp target com.interpreter.zen.Zen
```
- Provide the file_path as an argument to output a result.
```bash
java -cp target com.interpreter.zen.Zen path/to/file.zen
```
