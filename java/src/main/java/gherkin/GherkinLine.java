package gherkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static gherkin.StringUtils.ltrim;

public class GherkinLine implements IGherkinLine {
    private final String lineText;
    private final String trimmedLineText;

    public GherkinLine(String lineText) {
        this.lineText = lineText;
        this.trimmedLineText = ltrim(lineText);
    }

    @Override
    public Integer indent() {
        return lineText.length() - trimmedLineText.length();
    }

    @Override
    public void detach() {

    }

    @Override
    public String getLineText(int indentToRemove) {
        if (indentToRemove < 0 || indentToRemove > indent())
            return trimmedLineText;
        return lineText.substring(indentToRemove);
    }

    @Override
    public boolean isEmpty() {
        return trimmedLineText.length() == 0;
    }

    @Override
    public boolean startsWith(String prefix) {
        return trimmedLineText.startsWith(prefix);
    }

    @Override
    public String getRestTrimmed(int length) {
        return trimmedLineText.substring(length).trim();
    }

    @Override
    public List<GherkinLineSpan> getTags() {
        return getSpans(trimmedLineText, "\\s+", indent());
    }

    @Override
    public boolean startsWithTitleKeyword(String text) {
        int textLength = text.length();
        return trimmedLineText.length() > textLength &&
                trimmedLineText.startsWith(text) &&
                trimmedLineText.substring(textLength, textLength + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length())
                        .equals(GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR);
        // TODO aslak: extract startsWithFrom method for clarity
    }

    @Override
    public List<GherkinLineSpan> getTableCells() {
        return getSpans(trimmedLineText, "\\s*\\|\\s*", indent());
    }

    public static List<GherkinLineSpan> getSpans(String text, String delimiter, int indent) {
        List<GherkinLineSpan> lineSpans = new ArrayList<>();
        Scanner scanner = new Scanner(text).useDelimiter(delimiter);
        while (scanner.hasNext()) {
            String cell = scanner.next();
            int column = scanner.match().start() + indent + 1;
            lineSpans.add(new GherkinLineSpan(column, cell));
        }
        return lineSpans;
    }
}
