import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.PrettyPrintVisitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by bresai on 2016/12/15.
 */
public class AstPrinter extends VoidVisitorAdapter {
    private PrettyPrintVisitor visitor;

    public String print(Node node) {
        node.accept(visitor, null);
        return visitor.getSource();
    }

    public static void main(String[] args) throws FileNotFoundException {

        FileInputStream inputStream = new FileInputStream("src/main/java/source/Test1.java");

        CompilationUnit cu = JavaParser.parse(inputStream);

        AstVisitor visitor = new AstVisitor(new AstConfiguration());
        cu.accept(visitor,  null);
        // prints the resulting compilation unit to default system output
        System.out.println(visitor.getSource());
    }
}


