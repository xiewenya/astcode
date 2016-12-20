import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.imports.*;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeArguments;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.printer.PrettyPrintVisitor;
import com.github.javaparser.utils.PositionUtils;
import com.github.javaparser.utils.Utils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by bresai on 2016/12/19.
 */
public class AstVisitor extends PrettyPrintVisitor {
    private final AstConfiguration configuration;
    private final SourcePrinter printer;
    private final DomPrinter domPrinter;

    public AstVisitor(AstConfiguration configuration) {
        super(configuration);
        this.configuration = configuration;
        this.printer = new SourcePrinter(this.configuration.getIndent());
        this.domPrinter = new DomPrinter(this.configuration.getDom(), this);
    }

    public SourcePrinter getPrinter() {
        return printer;
    }

    public String getSource() {
        return this.printer.getSource();
    }

    private void printModifiers(EnumSet<Modifier> modifiers) {
        if(modifiers.size() > 0) {
            this.printer.print((String)modifiers.stream().map(Modifier::asString).collect(Collectors.joining(" ")) + " ");
        }

    }

    private void printMembers(NodeList<BodyDeclaration<?>> members, Void arg) {
        Iterator var3 = members.iterator();

        while(var3.hasNext()) {
            BodyDeclaration member = (BodyDeclaration)var3.next();
            this.printer.println();
            member.accept(this, arg);
            this.printer.println();
        }

    }

    private void printMemberAnnotations(NodeList<AnnotationExpr> annotations, Void arg) {
        if(!annotations.isEmpty()) {
            Iterator var3 = annotations.iterator();

            while(var3.hasNext()) {
                AnnotationExpr annotation = (AnnotationExpr)var3.next();
                this.domPrinter.printAnnotation(annotation, arg);
                this.printer.println();
            }

        }
    }

    private void printAnnotations(NodeList<AnnotationExpr> annotations, boolean prefixWithASpace, Void arg) {
        if(!annotations.isEmpty()) {
            if(prefixWithASpace) {
                this.printer.print(" ");
            }

            Iterator var4 = annotations.iterator();

            while(var4.hasNext()) {
                AnnotationExpr annotation = (AnnotationExpr)var4.next();
                this.domPrinter.printAnnotation(annotation, arg);
                this.printer.print(" ");
            }

        }
    }

    private void printTypeArgs(NodeWithTypeArguments<?> nodeWithTypeArguments, Void arg) {
        NodeList typeArguments = (NodeList)nodeWithTypeArguments.getTypeArguments().orElse(null);
        if(!Utils.isNullOrEmpty(typeArguments)) {
            this.printer.print("<");
            Iterator i = typeArguments.iterator();

            while(i.hasNext()) {
                Type t = (Type)i.next();
                t.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }

            this.printer.print(">");
        }

    }

    private void printTypeParameters(NodeList<TypeParameter> args, Void arg) {
        if(!Utils.isNullOrEmpty(args)) {
            this.printer.print("<");
            Iterator i = args.iterator();

            while(i.hasNext()) {
                TypeParameter t = (TypeParameter)i.next();
                t.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }

            this.printer.print(">");
        }

    }

    private void printArguments(NodeList<Expression> args, Void arg) {
        this.printer.print("(");
        if(!Utils.isNullOrEmpty(args)) {
            Iterator i = args.iterator();

            while(i.hasNext()) {
                Expression e = (Expression)i.next();
                e.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.print(")");
    }

    private void printJavaComment(Comment javacomment, Void arg) {
        if(javacomment != null) {
            javacomment.accept(this, arg);
        }

    }

    public void visit(CompilationUnit n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getPackage().isPresent()) {
            ((PackageDeclaration)n.getPackage().get()).accept(this, arg);
        }

        n.getImports().accept(this, arg);
        if(!n.getImports().isEmpty()) {
            this.printer.println();
        }

        Iterator i = n.getTypes().iterator();

        while(i.hasNext()) {
            ((TypeDeclaration)i.next()).accept(this, arg);
            this.printer.println();
            if(i.hasNext()) {
                this.printer.println();
            }
        }

        this.printOrphanCommentsEnding(n);
    }

    public void visit(PackageDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printAnnotations(n.getAnnotations(), false, arg);
        this.printer.print("package ");
        n.getName().accept(this, arg);
        this.printer.println(";");
        this.printer.println();
        this.printOrphanCommentsEnding(n);
    }

    public void visit(NameExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getName().accept(this, arg);
        this.printOrphanCommentsEnding(n);
    }

    public void visit(Name n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getQualifier().isPresent()) {
            ((Name)n.getQualifier().get()).accept(this, arg);
            this.printer.print(".");
        }

        this.printer.print(n.getIdentifier());
        this.printOrphanCommentsEnding(n);
    }

    public void visit(SimpleName n, Void arg) {
        this.printer.print(n.getIdentifier());
    }

    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printMemberAnnotations(n.getAnnotations(), arg);
        this.printModifiers(n.getModifiers());
        if(n.isInterface()) {
            this.printer.print("interface ");
        } else {
            this.printer.print("class ");
        }

        this.domPrinter.printClassDeclaration(n, arg);
        this.printTypeParameters(n.getTypeParameters(), arg);
        Iterator i;
        ClassOrInterfaceType c;
        if(!n.getExtends().isEmpty()) {
            this.printer.print(" extends ");
            i = n.getExtends().iterator();

            while(i.hasNext()) {
                c = (ClassOrInterfaceType)i.next();
                this.domPrinter.printClassCall(c, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        if(!n.getImplements().isEmpty()) {
            this.printer.print(" implements ");
            i = n.getImplements().iterator();

            while(i.hasNext()) {
                c = (ClassOrInterfaceType)i.next();
                this.domPrinter.printClassCall(c, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.println(" {");
        this.printer.indent();
        if(!Utils.isNullOrEmpty(n.getMembers())) {
            this.printMembers(n.getMembers(), arg);
        }

        this.printOrphanCommentsEnding(n);
        this.printer.unindent();
        this.printer.print("}");
    }

    public void visit(JavadocComment n, Void arg) {
        this.printer.print("/**");
        this.printer.print(n.getContent());
        this.printer.println("*/");
    }

    public void visit(ClassOrInterfaceType n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getScope().isPresent()) {
            ((ClassOrInterfaceType)n.getScope().get()).accept(this, arg);
            this.printer.print(".");
        }

        Iterator var3 = n.getAnnotations().iterator();

        while(var3.hasNext()) {
            AnnotationExpr ae = (AnnotationExpr)var3.next();
            ae.accept(this, arg);
            this.printer.print(" ");
        }

        n.getName().accept(this, arg);
        if(n.isUsingDiamondOperator()) {
            this.printer.print("<>");
        } else {
            this.printTypeArgs(n, arg);
        }

    }

    public void visit(TypeParameter n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        Iterator i = n.getAnnotations().iterator();

        while(i.hasNext()) {
            AnnotationExpr c = (AnnotationExpr)i.next();
            c.accept(this, arg);
            this.printer.print(" ");
        }

        n.getName().accept(this, arg);
        if(!Utils.isNullOrEmpty(n.getTypeBound())) {
            this.printer.print(" extends ");
            i = n.getTypeBound().iterator();

            while(i.hasNext()) {
                ClassOrInterfaceType c1 = (ClassOrInterfaceType)i.next();
                c1.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(" & ");
                }
            }
        }

    }

    public void visit(PrimitiveType n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printAnnotations(n.getAnnotations(), true, arg);
        this.printer.print(n.getType().asString());
    }

    public void visit(ArrayType n, Void arg) {
        LinkedList arrayTypeBuffer = new LinkedList();

        Object type;
        ArrayType arrayType;
        for(type = n; type instanceof ArrayType; type = arrayType.getComponentType()) {
            arrayType = (ArrayType)type;
            arrayTypeBuffer.add(arrayType);
        }

        ((Type)type).accept(this, arg);
        Iterator arrayType2 = arrayTypeBuffer.iterator();

        while(arrayType2.hasNext()) {
            ArrayType arrayType1 = (ArrayType)arrayType2.next();
            this.printAnnotations(arrayType1.getAnnotations(), true, arg);
            this.printer.print("[]");
        }

    }

    public void visit(ArrayCreationLevel n, Void arg) {
        this.printAnnotations(n.getAnnotations(), true, arg);
        this.printer.print("[");
        if(n.getDimension().isPresent()) {
            ((Expression)n.getDimension().get()).accept(this, arg);
        }

        this.printer.print("]");
    }

    public void visit(IntersectionType n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printAnnotations(n.getAnnotations(), false, arg);
        boolean isFirst = true;
        Iterator var4 = n.getElements().iterator();

        while(var4.hasNext()) {
            ReferenceType element = (ReferenceType)var4.next();
            element.accept(this, arg);
            if(isFirst) {
                isFirst = false;
            } else {
                this.printer.print(" & ");
            }
        }

    }

    public void visit(UnionType n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printAnnotations(n.getAnnotations(), true, arg);
        boolean isFirst = true;

        ReferenceType element;
        for(Iterator var4 = n.getElements().iterator(); var4.hasNext(); element.accept(this, arg)) {
            element = (ReferenceType)var4.next();
            if(isFirst) {
                isFirst = false;
            } else {
                this.printer.print(" | ");
            }
        }

    }

    public void visit(WildcardType n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printAnnotations(n.getAnnotations(), false, arg);
        this.printer.print("?");
        if(n.getExtendedTypes().isPresent()) {
            this.printer.print(" extends ");
            ((ReferenceType)n.getExtendedTypes().get()).accept(this, arg);
        }

        if(n.getSuperTypes().isPresent()) {
            this.printer.print(" super ");
            ((ReferenceType)n.getSuperTypes().get()).accept(this, arg);
        }

    }

    public void visit(UnknownType n, Void arg) {
    }

    public void visit(FieldDeclaration n, Void arg) {
        this.printOrphanCommentsBeforeThisChildNode(n);
        this.printJavaComment(n.getComment(), arg);
        this.printMemberAnnotations(n.getAnnotations(), arg);
        this.printModifiers(n.getModifiers());
        if(!n.getVariables().isEmpty()) {
            this.domPrinter.printVariableType(
                    ((VariableDeclarator)n.getVariables().get(0)).
                            getType().getElementType(), arg);
        }

        this.printer.print(" ");
        Iterator i = n.getVariables().iterator();

        while(i.hasNext()) {
            VariableDeclarator var = (VariableDeclarator)i.next();
            var.accept(this, arg);
            if(i.hasNext()) {
                this.printer.print(", ");
            }
        }

        this.printer.print(";");
    }

    public void visit(VariableDeclarator n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getName().accept(this, arg);
        LinkedList arrayTypeBuffer = new LinkedList();

        ArrayType arrayType;
        for(Type type = n.getType(); type instanceof ArrayType; type = arrayType.getComponentType()) {
            arrayType = (ArrayType)type;
            arrayTypeBuffer.add(arrayType);
        }

        Iterator arrayType2 = arrayTypeBuffer.iterator();

        while(arrayType2.hasNext()) {
            ArrayType arrayType1 = (ArrayType)arrayType2.next();
            this.printAnnotations(arrayType1.getAnnotations(), true, arg);
            this.printer.print("[]");
        }

        if(n.getInitializer().isPresent()) {
            this.printer.print(" = ");
            ((Expression)n.getInitializer().get()).accept(this, arg);
        }

    }

    public void visit(ArrayInitializerExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("{");
        if(!Utils.isNullOrEmpty(n.getValues())) {
            this.printer.print(" ");
            Iterator i = n.getValues().iterator();

            while(i.hasNext()) {
                Expression expr = (Expression)i.next();
                expr.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }

            this.printer.print(" ");
        }

        this.printer.print("}");
    }

    public void visit(VoidType n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printAnnotations(n.getAnnotations(), false, arg);
        this.printer.print("void");
    }

    public void visit(ArrayAccessExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getName().accept(this, arg);
        this.printer.print("[");
        n.getIndex().accept(this, arg);
        this.printer.print("]");
    }

    public void visit(ArrayCreationExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("new ");
        n.getElementType().accept(this, arg);
        Iterator var3 = n.getLevels().iterator();

        while(var3.hasNext()) {
            ArrayCreationLevel level = (ArrayCreationLevel)var3.next();
            level.accept(this, arg);
        }

        if(n.getInitializer().isPresent()) {
            this.printer.print(" ");
            ((ArrayInitializerExpr)n.getInitializer().get()).accept(this, arg);
        }

    }

    public void visit(AssignExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getTarget().accept(this, arg);
        this.printer.print(" ");
        this.printer.print(n.getOperator().asString());
        this.printer.print(" ");
        n.getValue().accept(this, arg);
    }

    public void visit(BinaryExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getLeft().accept(this, arg);
        this.printer.print(" ");
        this.printer.print(n.getOperator().asString());
        this.printer.print(" ");
        n.getRight().accept(this, arg);
    }

    public void visit(CastExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("(");
        n.getType().accept(this, arg);
        this.printer.print(") ");
        n.getExpression().accept(this, arg);
    }

    public void visit(ClassExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getType().accept(this, arg);
        this.printer.print(".class");
    }

    public void visit(ConditionalExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getCondition().accept(this, arg);
        this.printer.print(" ? ");
        n.getThenExpr().accept(this, arg);
        this.printer.print(" : ");
        n.getElseExpr().accept(this, arg);
    }

    public void visit(EnclosedExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("(");
        if(n.getInner().isPresent()) {
            ((Expression)n.getInner().get()).accept(this, arg);
        }

        this.printer.print(")");
    }

    public void visit(FieldAccessExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getScope().isPresent()) {
            ((Expression)n.getScope().get()).accept(this, arg);
        }

        this.printer.print(".");
        n.getField().accept(this, arg);
    }

    public void visit(InstanceOfExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getExpression().accept(this, arg);
        this.printer.print(" instanceof ");
        n.getType().accept(this, arg);
    }

    public void visit(CharLiteralExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("\'");
        this.printer.print(n.getValue());
        this.printer.print("\'");
    }

    public void visit(DoubleLiteralExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print(n.getValue());
    }

    public void visit(IntegerLiteralExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print(n.getValue());
    }

    public void visit(LongLiteralExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print(n.getValue());
    }

    public void visit(StringLiteralExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("\"");
        this.printer.print(n.getValue());
        this.printer.print("\"");
    }

    public void visit(BooleanLiteralExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print(String.valueOf(n.getValue()));
    }

    public void visit(NullLiteralExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("null");
    }

    public void visit(ThisExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getClassExpr().isPresent()) {
            ((Expression)n.getClassExpr().get()).accept(this, arg);
            this.printer.print(".");
        }

        this.printer.print("this");
    }

    public void visit(SuperExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getClassExpr().isPresent()) {
            ((Expression)n.getClassExpr().get()).accept(this, arg);
            this.printer.print(".");
        }

        this.printer.print("super");
    }

    public void visit(MethodCallExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getScope() != null) {
            n.getScope().accept(this, arg);
            this.printer.print(".");
        }

        this.printTypeArgs(n, arg);
        this.domPrinter.printMethodCall(n.getName(), arg);
        this.printArguments(n.getArguments(), arg);
    }

    public void visit(ObjectCreationExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getScope().isPresent()) {
            ((Expression)n.getScope().get()).accept(this, arg);
            this.printer.print(".");
        }

        this.printer.print("new ");
        this.printTypeArgs(n, arg);
        if(!Utils.isNullOrEmpty((Collection)n.getTypeArguments().orElse(null))) {
            this.printer.print(" ");
        }

        this.domPrinter.printNewObjectCreation(n.getType(), arg);
        this.printArguments(n.getArguments(), arg);
        if(n.getAnonymousClassBody().isPresent()) {
            this.printer.println(" {");
            this.printer.indent();
            this.printMembers((NodeList)n.getAnonymousClassBody().get(), arg);
            this.printer.unindent();
            this.printer.print("}");
        }

    }

    public void visit(UnaryExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getOperator().isPrefix()) {
            this.printer.print(n.getOperator().asString());
        }

        n.getExpression().accept(this, arg);
        if(n.getOperator().isPostfix()) {
            this.printer.print(n.getOperator().asString());
        }

    }

    public void visit(ConstructorDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printMemberAnnotations(n.getAnnotations(), arg);
        this.printModifiers(n.getModifiers());
        this.printTypeParameters(n.getTypeParameters(), arg);
        if(n.isGeneric()) {
            this.printer.print(" ");
        }

        n.getName().accept(this, arg);
        this.printer.print("(");
        Iterator i;
        if(!n.getParameters().isEmpty()) {
            i = n.getParameters().iterator();

            while(i.hasNext()) {
                Parameter name = (Parameter)i.next();
                this.domPrinter.printParameter(name, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.print(")");
        if(!Utils.isNullOrEmpty(n.getThrownExceptions())) {
            this.printer.print(" throws ");
            i = n.getThrownExceptions().iterator();

            while(i.hasNext()) {
                ReferenceType name1 = (ReferenceType)i.next();
                name1.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.print(" ");
        n.getBody().accept(this, arg);
    }

    public void visit(MethodDeclaration n, Void arg) {
        this.printOrphanCommentsBeforeThisChildNode(n);
        this.printJavaComment(n.getComment(), arg);
        this.printMemberAnnotations(n.getAnnotations(), arg);
        this.printModifiers(n.getModifiers());
        if(n.isDefault()) {
            this.printer.print("default ");
        }

        this.printTypeParameters(n.getTypeParameters(), arg);
        if(!Utils.isNullOrEmpty(n.getTypeParameters())) {
            this.printer.print(" ");
        }


        n.getType().accept(this, arg);
        this.printer.print(" ");
        this.domPrinter.printMethodDeclaration(n, arg);
//        n.getName().accept(this, arg);
        this.printer.print("(");
        Iterator i;
        if(!Utils.isNullOrEmpty(n.getParameters())) {
            i = n.getParameters().iterator();

            while(i.hasNext()) {
                Parameter name = (Parameter)i.next();
                name.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.print(")");
        if(!Utils.isNullOrEmpty(n.getThrownExceptions())) {
            this.printer.print(" throws ");
            i = n.getThrownExceptions().iterator();

            while(i.hasNext()) {
                ReferenceType name1 = (ReferenceType)i.next();
                name1.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        if(!n.getBody().isPresent()) {
            this.printer.print(";");
        } else {
            this.printer.print(" ");
            ((BlockStmt)n.getBody().get()).accept(this, arg);
        }

    }

    public void visit(Parameter n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printAnnotations(n.getAnnotations(), false, arg);
        this.printModifiers(n.getModifiers());
        if(n.getType() != null) {
            n.getType().accept(this, arg);
        }

        if(n.isVarArgs()) {
            this.printer.print("...");
        }

        this.printer.print(" ");
        n.getName().accept(this, arg);
    }

    public void visit(ExplicitConstructorInvocationStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.isThis()) {
            this.printTypeArgs(n, arg);
            this.printer.print("this");
        } else {
            if(n.getExpression().isPresent()) {
                ((Expression)n.getExpression().get()).accept(this, arg);
                this.printer.print(".");
            }

            this.printTypeArgs(n, arg);
            this.printer.print("super");
        }

        this.printArguments(n.getArguments(), arg);
        this.printer.print(";");
    }

    public void visit(VariableDeclarationExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printAnnotations(n.getAnnotations(), false, arg);
        this.printModifiers(n.getModifiers());
        if(!n.getVariables().isEmpty()) {
            ((VariableDeclarator)n.getVariables().get(0)).getType().getElementType().accept(this, arg);
        }

        this.printer.print(" ");
        Iterator i = n.getVariables().iterator();

        while(i.hasNext()) {
            VariableDeclarator v = (VariableDeclarator)i.next();
            v.accept(this, arg);
            if(i.hasNext()) {
                this.printer.print(", ");
            }
        }

    }

    public void visit(TypeDeclarationStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getTypeDeclaration().accept(this, arg);
    }

    public void visit(AssertStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("assert ");
        n.getCheck().accept(this, arg);
        if(n.getMessage().isPresent()) {
            this.printer.print(" : ");
            ((Expression)n.getMessage().get()).accept(this, arg);
        }

        this.printer.print(";");
    }

    public void visit(BlockStmt n, Void arg) {
        this.printOrphanCommentsBeforeThisChildNode(n);
        this.printJavaComment(n.getComment(), arg);
        this.printer.println("{");
        if(n.getStatements() != null) {
            this.printer.indent();
            Iterator var3 = n.getStatements().iterator();

            while(var3.hasNext()) {
                Statement s = (Statement)var3.next();
                s.accept(this, arg);
                this.printer.println();
            }

            this.printer.unindent();
        }

        this.printOrphanCommentsEnding(n);
        this.printer.print("}");
    }

    public void visit(LabeledStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print(n.getLabel());
        this.printer.print(": ");
        n.getStatement().accept(this, arg);
    }

    public void visit(EmptyStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print(";");
    }

    public void visit(ExpressionStmt n, Void arg) {
        this.printOrphanCommentsBeforeThisChildNode(n);
        this.printJavaComment(n.getComment(), arg);
        n.getExpression().accept(this, arg);
        this.printer.print(";");
    }

    public void visit(SwitchStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("switch(");
        n.getSelector().accept(this, arg);
        this.printer.println(") {");
        if(n.getEntries() != null) {
            this.printer.indent();
            Iterator var3 = n.getEntries().iterator();

            while(var3.hasNext()) {
                SwitchEntryStmt e = (SwitchEntryStmt)var3.next();
                e.accept(this, arg);
            }

            this.printer.unindent();
        }

        this.printer.print("}");
    }

    public void visit(SwitchEntryStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getLabel().isPresent()) {
            this.printer.print("case ");
            ((Expression)n.getLabel().get()).accept(this, arg);
            this.printer.print(":");
        } else {
            this.printer.print("default:");
        }

        this.printer.println();
        this.printer.indent();
        if(n.getStatements() != null) {
            Iterator var3 = n.getStatements().iterator();

            while(var3.hasNext()) {
                Statement s = (Statement)var3.next();
                s.accept(this, arg);
                this.printer.println();
            }
        }

        this.printer.unindent();
    }

    public void visit(BreakStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("break");
        if(n.getIdentifier().isPresent()) {
            this.printer.print(" ");
            this.printer.print((String)n.getIdentifier().get());
        }

        this.printer.print(";");
    }

    public void visit(ReturnStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("return");
        if(n.getExpression().isPresent()) {
            this.printer.print(" ");
            ((Expression)n.getExpression().get()).accept(this, arg);
        }

        this.printer.print(";");
    }

    public void visit(EnumDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printMemberAnnotations(n.getAnnotations(), arg);
        this.printModifiers(n.getModifiers());
        this.printer.print("enum ");
        n.getName().accept(this, arg);
        Iterator i;
        if(!n.getImplements().isEmpty()) {
            this.printer.print(" implements ");
            i = n.getImplements().iterator();

            while(i.hasNext()) {
                ClassOrInterfaceType e = (ClassOrInterfaceType)i.next();
                e.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.println(" {");
        this.printer.indent();
        if(n.getEntries() != null) {
            this.printer.println();
            i = n.getEntries().iterator();

            while(i.hasNext()) {
                EnumConstantDeclaration e1 = (EnumConstantDeclaration)i.next();
                e1.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        if(!n.getMembers().isEmpty()) {
            this.printer.println(";");
            this.printMembers(n.getMembers(), arg);
        } else if(!n.getEntries().isEmpty()) {
            this.printer.println();
        }

        this.printer.unindent();
        this.printer.print("}");
    }

    public void visit(EnumConstantDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printMemberAnnotations(n.getAnnotations(), arg);
        n.getName().accept(this, arg);
        if(!n.getArguments().isEmpty()) {
            this.printArguments(n.getArguments(), arg);
        }

        if(!n.getClassBody().isEmpty()) {
            this.printer.println(" {");
            this.printer.indent();
            this.printMembers(n.getClassBody(), arg);
            this.printer.unindent();
            this.printer.println("}");
        }

    }

    public void visit(EmptyMemberDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print(";");
    }

    public void visit(InitializerDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.isStatic()) {
            this.printer.print("static ");
        }

        n.getBlock().accept(this, arg);
    }

    public void visit(IfStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("if (");
        n.getCondition().accept(this, arg);
        boolean thenBlock = n.getThenStmt() instanceof BlockStmt;
        if(thenBlock) {
            this.printer.print(") ");
        } else {
            this.printer.println(")");
            this.printer.indent();
        }

        n.getThenStmt().accept(this, arg);
        if(!thenBlock) {
            this.printer.unindent();
        }

        if(n.getElseStmt().isPresent()) {
            if(thenBlock) {
                this.printer.print(" ");
            } else {
                this.printer.println();
            }

            boolean elseIf = n.getElseStmt().orElse(null) instanceof IfStmt;
            boolean elseBlock = n.getElseStmt().orElse(null) instanceof BlockStmt;
            if(!elseIf && !elseBlock) {
                this.printer.println("else");
                this.printer.indent();
            } else {
                this.printer.print("else ");
            }

            if(n.getElseStmt().isPresent()) {
                ((Statement)n.getElseStmt().get()).accept(this, arg);
            }

            if(!elseIf && !elseBlock) {
                this.printer.unindent();
            }
        }

    }

    public void visit(WhileStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("while (");
        n.getCondition().accept(this, arg);
        this.printer.print(") ");
        n.getBody().accept(this, arg);
    }

    public void visit(ContinueStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("continue");
        if(n.getIdentifier().isPresent()) {
            this.printer.print(" ");
            this.printer.print((String)n.getIdentifier().get());
        }

        this.printer.print(";");
    }

    public void visit(DoStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("do ");
        n.getBody().accept(this, arg);
        this.printer.print(" while (");
        n.getCondition().accept(this, arg);
        this.printer.print(");");
    }

    public void visit(ForeachStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("for (");
        n.getVariable().accept(this, arg);
        this.printer.print(" : ");
        n.getIterable().accept(this, arg);
        this.printer.print(") ");
        n.getBody().accept(this, arg);
    }

    public void visit(ForStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("for (");
        Iterator i;
        Expression e;
        if(n.getInitialization() != null) {
            i = n.getInitialization().iterator();

            while(i.hasNext()) {
                e = (Expression)i.next();
                e.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.print("; ");
        if(n.getCompare().isPresent()) {
            ((Expression)n.getCompare().get()).accept(this, arg);
        }

        this.printer.print("; ");
        if(n.getUpdate() != null) {
            i = n.getUpdate().iterator();

            while(i.hasNext()) {
                e = (Expression)i.next();
                e.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.print(") ");
        n.getBody().accept(this, arg);
    }

    public void visit(ThrowStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("throw ");
        n.getExpression().accept(this, arg);
        this.printer.print(";");
    }

    public void visit(SynchronizedStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("synchronized (");
        n.getExpression().accept(this, arg);
        this.printer.print(") ");
        n.getBody().accept(this, arg);
    }

    public void visit(TryStmt n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("try ");
        Iterator resources;
        if(!n.getResources().isEmpty()) {
            this.printer.print("(");
            resources = n.getResources().iterator();

            for(boolean c = true; resources.hasNext(); c = false) {
                this.visit((VariableDeclarationExpr)resources.next(), arg);
                if(resources.hasNext()) {
                    this.printer.print(";");
                    this.printer.println();
                    if(c) {
                        this.printer.indent();
                    }
                }
            }

            if(n.getResources().size() > 1) {
                this.printer.unindent();
            }

            this.printer.print(") ");
        }

        if(n.getTryBlock().isPresent()) {
            ((BlockStmt)n.getTryBlock().get()).accept(this, arg);
        }

        resources = n.getCatchClauses().iterator();

        while(resources.hasNext()) {
            CatchClause c1 = (CatchClause)resources.next();
            c1.accept(this, arg);
        }

        if(n.getFinallyBlock().isPresent()) {
            this.printer.print(" finally ");
            ((BlockStmt)n.getFinallyBlock().get()).accept(this, arg);
        }

    }

    public void visit(CatchClause n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print(" catch (");
        n.getParameter().accept(this, arg);
        this.printer.print(") ");
        n.getBody().accept(this, arg);
    }

    public void visit(AnnotationDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printMemberAnnotations(n.getAnnotations(), arg);
        this.printModifiers(n.getModifiers());
        this.printer.print("@interface ");
        n.getName().accept(this, arg);
        this.printer.println(" {");
        this.printer.indent();
        if(n.getMembers() != null) {
            this.printMembers(n.getMembers(), arg);
        }

        this.printer.unindent();
        this.printer.print("}");
    }

    public void visit(AnnotationMemberDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printMemberAnnotations(n.getAnnotations(), arg);
        this.printModifiers(n.getModifiers());
        n.getType().accept(this, arg);
        this.printer.print(" ");
        n.getName().accept(this, arg);
        this.printer.print("()");
        if(n.getDefaultValue().isPresent()) {
            this.printer.print(" default ");
            ((Expression)n.getDefaultValue().get()).accept(this, arg);
        }

        this.printer.print(";");
    }

    public void visit(MarkerAnnotationExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("@");
        n.getName().accept(this, arg);
    }

    public void visit(SingleMemberAnnotationExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("@");
        n.getName().accept(this, arg);
        this.printer.print("(");
        n.getMemberValue().accept(this, arg);
        this.printer.print(")");
    }

    public void visit(NormalAnnotationExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("@");
        n.getName().accept(this, arg);
        this.printer.print("(");
        if(n.getPairs() != null) {
            Iterator i = n.getPairs().iterator();

            while(i.hasNext()) {
                MemberValuePair m = (MemberValuePair)i.next();
                m.accept(this, arg);
                if(i.hasNext()) {
                    this.printer.print(", ");
                }
            }
        }

        this.printer.print(")");
    }

    public void visit(MemberValuePair n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        n.getName().accept(this, arg);
        this.printer.print(" = ");
        n.getValue().accept(this, arg);
    }

    public void visit(LineComment n, Void arg) {
        if(this.configuration.isPrintComments()) {
            this.printer.print("//");
            String tmp = n.getContent();
            tmp = tmp.replace('\r', ' ');
            tmp = tmp.replace('\n', ' ');
            this.printer.println(tmp);
        }
    }

    public void visit(BlockComment n, Void arg) {
        if(this.configuration.isPrintComments()) {
            this.printer.print("/*").print(n.getContent()).println("*/");
        }
    }

    public void visit(LambdaExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        NodeList parameters = n.getParameters();
        boolean printPar = n.isEnclosingParameters();
        if(printPar) {
            this.printer.print("(");
        }

        Iterator body = parameters.iterator();

        while(body.hasNext()) {
            Parameter p = (Parameter)body.next();
            p.accept(this, arg);
            if(body.hasNext()) {
                this.printer.print(", ");
            }
        }

        if(printPar) {
            this.printer.print(")");
        }

        this.printer.print(" -> ");
        Statement body1 = n.getBody();
        if(body1 instanceof ExpressionStmt) {
            ((ExpressionStmt)body1).getExpression().accept(this, arg);
        } else {
            body1.accept(this, arg);
        }

    }

    public void visit(MethodReferenceExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        Expression scope = n.getScope();
        String identifier = n.getIdentifier();
        if(scope != null) {
            n.getScope().accept(this, arg);
        }

        this.printer.print("::");
        this.printTypeArgs(n, arg);
        if(identifier != null) {
            this.printer.print(identifier);
        }

    }

    public void visit(TypeExpr n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        if(n.getType() != null) {
            n.getType().accept(this, arg);
        }

    }

    public void visit(NodeList n, Void arg) {
        Iterator var3 = n.iterator();

        while(var3.hasNext()) {
            Object node = var3.next();
            ((Node)node).accept(this, arg);
        }

    }

    public void visit(BadImportDeclaration n, Void arg) {
        this.printer.println("???");
    }

    public void visit(SingleStaticImportDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("import static ");
        n.getType().accept(this, arg);
        this.printer.print(".");
        this.printer.print(n.getStaticMember());
        this.printer.println(";");
        this.printOrphanCommentsEnding(n);
    }

    public void visit(SingleTypeImportDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("import ");
        n.getType().accept(this, arg);
        this.printer.println(";");
        this.printOrphanCommentsEnding(n);
    }

    public void visit(StaticImportOnDemandDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("import static ");
        n.getType().accept(this, arg);
        this.printer.println(".*;");
        this.printOrphanCommentsEnding(n);
    }

    public void visit(TypeImportOnDemandDeclaration n, Void arg) {
        this.printJavaComment(n.getComment(), arg);
        this.printer.print("import ");
        n.getName().accept(this, arg);
        this.printer.println(".*;");
        this.printOrphanCommentsEnding(n);
    }

    private void printOrphanCommentsBeforeThisChildNode(Node node) {
        if(!(node instanceof Comment)) {
            Node parent = (Node)node.getParentNode().orElse(null);
            if(parent != null) {
                LinkedList everything = new LinkedList();
                everything.addAll(parent.getChildNodes());
                PositionUtils.sortByBeginPosition(everything);
                int positionOfTheChild = -1;

                int positionOfPreviousChild;
                for(positionOfPreviousChild = 0; positionOfPreviousChild < everything.size(); ++positionOfPreviousChild) {
                    if(everything.get(positionOfPreviousChild) == node) {
                        positionOfTheChild = positionOfPreviousChild;
                    }
                }

                if(positionOfTheChild == -1) {
                    throw new AssertionError("I am not a child of my parent.");
                } else {
                    positionOfPreviousChild = -1;

                    int i;
                    for(i = positionOfTheChild - 1; i >= 0 && positionOfPreviousChild == -1; --i) {
                        if(!(everything.get(i) instanceof Comment)) {
                            positionOfPreviousChild = i;
                        }
                    }

                    for(i = positionOfPreviousChild + 1; i < positionOfTheChild; ++i) {
                        Node nodeToPrint = (Node)everything.get(i);
                        if(!(nodeToPrint instanceof Comment)) {
                            throw new RuntimeException("Expected comment, instead " + nodeToPrint.getClass() + ". Position of previous child: " + positionOfPreviousChild + ", position of child " + positionOfTheChild);
                        }

                        nodeToPrint.accept(this, null);
                    }

                }
            }
        }
    }

    private void printOrphanCommentsEnding(Node node) {
        LinkedList everything = new LinkedList();
        everything.addAll(node.getChildNodes());
        PositionUtils.sortByBeginPosition(everything);
        if(!everything.isEmpty()) {
            int commentsAtEnd = 0;
            boolean findingComments = true;

            while(findingComments && commentsAtEnd < everything.size()) {
                Node i = (Node)everything.get(everything.size() - 1 - commentsAtEnd);
                findingComments = i instanceof Comment;
                if(findingComments) {
                    ++commentsAtEnd;
                }
            }

            for(int var6 = 0; var6 < commentsAtEnd; ++var6) {
                ((Node)everything.get(everything.size() - commentsAtEnd + var6)).accept(this, null);
            }

        }
    }
}
