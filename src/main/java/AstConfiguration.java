import com.github.javaparser.printer.PrettyPrinterConfiguration;

/**
 * Created by bresai on 2016/12/20.
 */
public class AstConfiguration extends PrettyPrinterConfiguration{
    private final String dom = "span";

    public String getDom() {
        return dom;
    }
}
