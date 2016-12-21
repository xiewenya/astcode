package theme;

/**
 * Created by bresai on 2016/12/21.
 */
public enum KeywordEnum {
    //modifier
    PUBLIC,
    PROTECTED,
    PRIVATE,
    ABSTRACT,
    STATIC,
    FINAL,
    TRANSIENT,
    VOLATILE,
    SYNCHRONIZED,
    NATIVE,
    STRICTFP,
    //language keyword
    PACKAGE,
    IMPORT,
    EXTENDS,
    IMPLEMENTS,
    CLASS,
    INTERFACE,
    ENUM,
    NEW,
    TRY,
    CATCH,
    THROWS,
    THROW,
    FINALLY,
    IF,
    ELSE,
    WHILE,
    DO,
    FOR,
    SWITCH,
    CASE,
    BREAK,
    CONTINUE,
    DEFAULT,
    ASSERT,
    RETURN,
    SUPER,
    THIS,
    VOID,
    NULL,
    INSTANCEOF;

    final String codeRepresentation;

    KeywordEnum() {
        this.codeRepresentation = name().toLowerCase();
    }

    public String getCodeRepresentation() {
        return codeRepresentation;
    }

}
