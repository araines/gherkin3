package gherkin;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GherkinTokenMatcher extends TokenMatcher {
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^\\s*#\\s*language\\s*:\\s*([a-zA-Z\\-_]+)\\s*$");
    private String activeDocStringSeparator = null;
    private int indentToRemove = 0;

    @Override
    public boolean match_Other(Token token) {
        String text = token.line.getLineText(indentToRemove); //take the entire line, except removing DocString indents
        setTokenMatched(token, Parser.TokenType.Other, text, null, 0, null);
        return true;
    }

    @Override
    public boolean match_Comment(Token token) {
        if (token.line.startsWith(GherkinLanguageConstants.COMMENT_PREFIX)) {
            String text = token.line.getLineText(0); //take the entire line
            setTokenMatched(token, Parser.TokenType.Comment, text, null, 0, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_Language(Token token) {
        Matcher matcher = LANGUAGE_PATTERN.matcher(token.line.getLineText(0));
        if (matcher.matches()) {
            String language = matcher.group(1);
            setTokenMatched(token, Parser.TokenType.Language, language, null, null, null);

            currentDialect = dialectProvider.getDialect(language, token.location);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_TagLine(Token token) {
        if (token.line.startsWith(GherkinLanguageConstants.TAG_PREFIX)) {
            setTokenMatched(token, Parser.TokenType.TagLine, null, null, null, token.line.getTags());
            return true;
        }
        return false;
    }

    @Override
    public boolean match_FeatureLine(Token token) {
        return matchTitleLine(token, Parser.TokenType.FeatureLine, currentDialect.getFeatureKeywords());
    }

    @Override
    public boolean match_BackgroundLine(Token token) {
        return matchTitleLine(token, Parser.TokenType.BackgroundLine, currentDialect.getBackgroundKeywords());
    }

    @Override
    public boolean match_ScenarioLine(Token token) {
        return matchTitleLine(token, Parser.TokenType.ScenarioLine, currentDialect.getScenarioKeywords());
    }

    @Override
    public boolean match_ScenarioOutlineLine(Token token) {
        return matchTitleLine(token, Parser.TokenType.ScenarioOutlineLine, currentDialect.getScenarioOutlineKeywords());
    }

    @Override
    public boolean match_ExamplesLine(Token token) {
        return matchTitleLine(token, Parser.TokenType.ExamplesLine, currentDialect.getExamplesKeywords());
    }

    private boolean matchTitleLine(Token token, Parser.TokenType tokenType, List<String> keywords) {
        for (String keyword : keywords) {
            if (token.line.startsWithTitleKeyword(keyword)) {
                String title = token.line.getRestTrimmed(keyword.length() + GherkinLanguageConstants.TITLE_KEYWORD_SEPARATOR.length());
                setTokenMatched(token, tokenType, title, keyword, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean match_DocStringSeparator(Token token) {
        return activeDocStringSeparator == null
                // open
                ? match_DocStringSeparator(token, GherkinLanguageConstants.DOCSTRING_SEPARATOR, true) ||
                match_DocStringSeparator(token, GherkinLanguageConstants.DOCSTRING_ALTERNATIVE_SEPARATOR, true)
                // close
                : match_DocStringSeparator(token, activeDocStringSeparator, false);
    }

    private boolean match_DocStringSeparator(Token token, String separator, boolean isOpen) {
        if (token.line.startsWith(separator)) {
            String contentType = null;
            if (isOpen) {
                contentType = token.line.getRestTrimmed(separator.length());
                activeDocStringSeparator = separator;
                indentToRemove = token.line.indent();
            } else {
                activeDocStringSeparator = null;
                indentToRemove = 0;
            }

            setTokenMatched(token, Parser.TokenType.DocStringSeparator, contentType, null, null, null);
            return true;
        }
        return false;
    }

    @Override
    public boolean match_StepLine(Token token) {
        List<String> keywords = currentDialect.getStepKeywords();
        for (String keyword : keywords) {
            if (token.line.startsWith(keyword)) {
                String stepText = token.line.getRestTrimmed(keyword.length());
                setTokenMatched(token, Parser.TokenType.StepLine, stepText, keyword, null, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean match_TableRow(Token token) {
        if (token.line.startsWith(GherkinLanguageConstants.TABLE_CELL_SEPARATOR)) {
            setTokenMatched(token, Parser.TokenType.TableRow, null, null, null, token.line.getTableCells());
            return true;
        }
        return false;
    }
}
