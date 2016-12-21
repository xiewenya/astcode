import Dom.Dom;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by bresai on 2016/12/20.
 */
public class DomPrinter extends Processor {
    private final String domType;
    private final AstVisitor visitor;
    private final List<String> blackList =
            Arrays.asList("Map", "List", "ArrayList", "LinkedList");

    private boolean isBlack(String arg) {
        return blackList.contains(arg);
    }

    public DomPrinter(String domType, AstVisitor visitor) {
        this.domType = domType;
        this.visitor = visitor;
    }

    private void printDom(Node node, Void arg, Dom dom){
        visitor.getPrinter().print(dom.getDomStart());
        node.accept(visitor, arg);
        visitor.getPrinter().print(dom.getDomEnd());
    }

    @Override
    public void printClassDeclaration(ClassOrInterfaceDeclaration node, Void arg) {
        Dom dom = new Dom(domType);
        dom.setType("class_declare");
        printDom(node.getName(), arg, dom);
    }

    @Override
    public void printMethodDeclaration(MethodDeclaration node, Void arg) {
        Dom dom = new Dom(domType);
        dom.setType("method_declare");
        printDom(node.getName(), arg, dom);
    }

    @Override
    public void printMethodCall(Node node, Void arg) {
        Dom dom = new Dom(domType);
        dom.setType("method_call");
        printDom(node, arg, dom);
    }

    @Override
    public void printAnnotation(AnnotationExpr node, Void arg) {
        Dom dom = new Dom(domType);
        dom.setType("use_annotation");
        printDom(node, arg, dom);
    }

    @Override
    public void printClassType(ClassOrInterfaceType node, Void arg) {
        if (isBlack(node.getName().getIdentifier())){
            node.getName().accept(visitor, arg);
            return;
        }
        Dom dom = new Dom(domType);
        dom.setType("class");
        printDom(node.getName(), arg, dom);
    }

    @Override
    public void printNewObjectCreation(ClassOrInterfaceType node, Void arg) {
        Dom dom = new Dom(domType);
        dom.setType("new");
        printDom(node.getName(), arg, dom);
    }

    @Override
    public void printConstructor(ConstructorDeclaration node, Void arg){
        Dom dom = new Dom(domType);
        dom.setType("constructor");
        printDom(node.getName(), arg, dom);
    }
}
