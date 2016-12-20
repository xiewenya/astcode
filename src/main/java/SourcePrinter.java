import com.github.javaparser.utils.Utils;

/**
 * Created by bresai on 2016/12/20.
 */
public class SourcePrinter {
    private final String indentation;
    private int level = 0;
    private boolean indented = false;
    private final StringBuilder buf = new StringBuilder();

    SourcePrinter(String indentation) {
        this.indentation = indentation;
    }

    SourcePrinter indent() {
        ++this.level;
        return this;
    }

    SourcePrinter unindent() {
        --this.level;
        return this;
    }

    private void makeIndent() {
        for (int i = 0; i < this.level; ++i) {
            this.buf.append(this.indentation);
        }

    }

    SourcePrinter print(String arg) {
        if (!this.indented) {
            this.makeIndent();
            this.indented = true;
        }

        this.buf.append(arg);
        return this;
    }

    SourcePrinter println(String arg) {
        this.print(arg);
        this.println();
        return this;
    }

    SourcePrinter println() {
        this.buf.append(Utils.EOL);
        this.indented = false;
        return this;
    }

    public String getSource() {
        return this.buf.toString();
    }

    public String toString() {
        return this.getSource();
    }
}
