package moe.mewore.e2e.output;

import java.io.Closeable;
import java.io.IOException;

public interface LineScanner extends Closeable {

    String nextLine() throws InterruptedException, IOException;

    boolean mayHaveNextLine();

    @Override
    void close() throws IOException;
}
