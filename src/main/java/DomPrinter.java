import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

/**
 * Created by bresai on 2016/12/20.
 */
public class DomPrinter {
    private final String domType;
    private final AstVisitor visitor;

    public DomPrinter(String domType, AstVisitor visitor) {
        this.domType = domType;
        this.visitor = visitor;
    }

    public void printClassDeclaration(ClassOrInterfaceDeclaration node, Void arg){
        visitor.getPrinter().print("<span>");
        node.getName().accept(visitor, arg);
        visitor.getPrinter().print("</span>");
    }

    public void printClassCall(ClassOrInterfaceType node, Void arg ){
        visitor.getPrinter().print("<span>");
        node.accept(visitor, arg);
        visitor.getPrinter().print("</span>");
    }

    public void printVariableType(Type node, Void arg){
        if (! (node instanceof PrimitiveType)){
            visitor.getPrinter().print("<span>");
            node.accept(visitor, arg);
            visitor.getPrinter().print("</span>");
        } else{
            node.accept(visitor, arg);
        }
    }

    public void printMethodDeclaration(MethodDeclaration node, Void arg){
        visitor.getPrinter().print("<span>");
        node.getName().accept(visitor, arg);
        visitor.getPrinter().print("</span>");
    }

    public void printMethodCall(Node node, Void arg){
        visitor.getPrinter().print("<span>");
        node.accept(visitor, arg);
        visitor.getPrinter().print("</span>");
    }

    public void printParameter(Parameter node, Void arg){
        visitor.getPrinter().print("<span>");
        node.accept(visitor, arg);
        visitor.getPrinter().print("</span>");
    }

    public void printNewObjectCreation(ClassOrInterfaceType node, Void arg){
        visitor.getPrinter().print("<span>");
        node.accept(visitor, arg);
        visitor.getPrinter().print("</span>");
    }

    public void printAnnotation(AnnotationExpr node, Void arg){
        visitor.getPrinter().print("<span>");
        node.accept(visitor, arg);
        visitor.getPrinter().print("</span>");
    }

}
