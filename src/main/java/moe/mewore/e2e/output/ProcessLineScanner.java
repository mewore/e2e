package moe.mewore.e2e.output;

public class ProcessLineScanner extends FiniteInputLineScanner {

    public ProcessLineScanner(final Process process) {
        super(process.getInputStream());
    }
}
