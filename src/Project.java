
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Project {

    public static void main(String[] args) throws IOException {
        String[] inputs = {
            "x = 001;",
            "x_2 = 0;",
            "x = 0\ny = x;\nz = ---(x+y);",
            "x = 1;\ny = 2;\nz = ---(x+y)*(x+-y);"
        };

        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            System.out.println("Input " + (i + 1));
            System.out.println();
            System.out.println(input);
            System.out.println();
            System.out.println("Output " + (i + 1));
            System.out.println();

            try {
                // Convert the input
                ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());

                // Create the lexer and interpreter
                Token.Lexer lexer = new Token.Lexer(inputStream);
                Token.Interpreter interpreter = new Token.Interpreter(lexer);

                // Interpret
                interpreter.interpret();
            } catch (RuntimeException e) {
                // Print the error message
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }
}
