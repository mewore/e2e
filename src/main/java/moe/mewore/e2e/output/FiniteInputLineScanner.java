package moe.mewore.e2e.output;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FiniteInputLineScanner implements LineScanner {

    private final Scanner scanner;

    public FiniteInputLineScanner(final InputStream stream) {
        scanner = new Scanner(stream, StandardCharsets.UTF_8);
    }

    @Override
    public String nextLine() {
        return scanner.nextLine();
    }

    @Override
    public boolean mayHaveNextLine() {
        return scanner.hasNext();
    }

    @Override
    public void close() {
    }
}
