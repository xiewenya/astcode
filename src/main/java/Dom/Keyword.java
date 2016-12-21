package Dom;

import theme.KeywordEnum;

/**
 * Created by bresai on 2016/12/21.
 */
public class Keyword extends Dom {
    private KeywordEnum keywordEnum;

    public Keyword(KeywordEnum keywordEnum) {
        this.keywordEnum = keywordEnum;
    }

    public Keyword(String keyword) {
        this.keywordEnum = KeywordEnum.valueOf(keyword.toUpperCase());
    }

    public String asString() {
        this.addClass(this.getClass().getSimpleName().toLowerCase());
        return this.getDomStart() + keywordEnum.getCodeRepresentation() + this.getDomEnd();
    }
}
