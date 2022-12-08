package moe.mewore.e2e.output;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DummyLineScanner implements LineScanner {

    private final Queue<String> lines;

    public DummyLineScanner(final String... lines) {
        this.lines = Arrays.stream(lines)
                .collect(Collectors.toCollection((Supplier<Queue<String>>) () -> new ArrayDeque<>(lines.length)));
    }

    @Override
    public String nextLine() {
        return lines.remove();
    }

    @Override
    public boolean mayHaveNextLine() {
        return !lines.isEmpty();
    }

    @Override
    public void close() {
        lines.clear();
    }
}