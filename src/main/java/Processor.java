import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

/**
 * Created by bresai on 2016/12/20.
 */
public abstract class Processor {

    public abstract void printClassDeclaration(ClassOrInterfaceDeclaration node, Void arg);

    public abstract void printMethodDeclaration(MethodDeclaration node, Void arg);

    public abstract void printMethodCall(Node node, Void arg);

    public abstract void printAnnotation(AnnotationExpr node, Void arg);

    public abstract void printClassType(ClassOrInterfaceType node, Void arg);

    public abstract void printNewObjectCreation(ClassOrInterfaceType node, Void arg);

    public abstract void printConstructor(ConstructorDeclaration node, Void arg);

}
