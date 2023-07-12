package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * Represents the REGEXP_MATCH function symbol as a `[a] LIKE [b]` expression. The version with arity 3 only supports the flag 'i'.
 */
public class DBRegexMatchAsLikeFunctionSymbolImpl extends AbstractTypedDBFunctionSymbol
        implements DBBooleanFunctionSymbol {

    protected DBRegexMatchAsLikeFunctionSymbolImpl(String name, DBTermType dbStringType,
                                                   DBTermType dbBooleanType, int arity) {
        super(name, arity == 2 ? ImmutableList.of(dbStringType, dbStringType) : ImmutableList.of(dbStringType, dbStringType, dbStringType), dbBooleanType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    /**
     * Splits a regular expression into tokens for the purpose of translation to a LIKE expression.
     */
    private ImmutableList<String> tokenizeRegEx(String regEx) {
        ImmutableList.Builder<String> tokens = ImmutableList.builder();
        int bracketDepth = 0;
        String current = "";
        for(var character : regEx.toCharArray()) {
            if(!current.equals("\\")
                    && bracketDepth == 0
                    && !ImmutableSet.of('*', '+', '?').contains(character)) {
                tokens.add(current);
                current = "";
            }
            current += character;
            if(character == '[' || character == '(')
                bracketDepth += 1;
            if(character == ']' || character == ')')
                bracketDepth -= 1;
        }
        if(bracketDepth != 0) {
            throw new MinorOntopInternalBugException("Error parsing RegEx: expected ']' or ')'.");
        }
        if(current.equals("\\")) {
            throw new MinorOntopInternalBugException("Error parsing RegEx: Invalid escape sequence.");
        }
        tokens.add(current);

        return tokens.build();
    }

    /**
     * Translates a token to its corresponding LIKE expression equivalent.
     */
    private String translateToken(String token) {
        /*
         * The following RegEx patterns are supported and can be translated:
         *          .*              =>          %
         *          .               =>          _
         *          .+              =>          _%
         *          [x]             =>          [x]
         *          [^x]            =>          [^x]
         *          \d, \w, \s      =>          [0-9], [0-9A-z], [ \t\n]
         *          \D, \W, \S      =>          [^0-9], [^0-9A-z], [^ \t\n]
         *          \[, \]          =>          [[], []]
         *          \<symbol>       =>          <symbol>
         * The following literals must be escaped:
         *          _               =>          [_]
         *          %               =>          [%]
         * The following RegEx patterns cannot be translated (and must fail explicitly):
         *          (x)*, (x)+
         *          ?
         *          |
         *          {n}
         *          ()
         *          $ and ^ when not at start or end of pattern
         */
        if(token.equals("_"))
            return "[_]";
        if(token.equals("%"))
            return "[%]";
        if(token.equals(".*"))
            return "%";
        if(token.equals("."))
            return "_";
        if(token.equals(".+"))
            return "_%";
        if(token.equals("\\d"))
            return "[0-9]";
        if(token.equals("\\w"))
            return "[0-9A-z]";
        if(token.equals("\\s"))
            return "[ \\t\\n]";
        if(token.equals("\\D"))
            return "[^0-9]";
        if(token.equals("\\W"))
            return "[^0-9A-z]";
        if(token.equals("\\S"))
            return "[^ \\t\\n]";
        if(token.equals("\\[") || token.equals("\\]"))
            return String.format("[%s]", token.substring(1));
        if(ImmutableSet.of("\\*", "\\.", "\\^", "\\$", "\\?", "\\+", "\\(", "\\)", "\\{", "\\}", "\\|", "\\\\").contains(token)) {
            return token.substring(1);
        }
        if(token.endsWith("*") || token.endsWith("+") || token.endsWith("?")) {
            throw new MinorOntopInternalBugException("Multiplicity modifiers are only allowed after the '.' wildcard when translating a RegEx to a LIKE comparison.");
        }
        if(token.startsWith("[") && token.endsWith("]")) {
            return token;
        }
        if(token.startsWith("(") || token.endsWith(")")) {
            throw new MinorOntopInternalBugException("Round parentheses are not allowed when translating a RegEx to a LIKE comparison.");
        }
        if(token.startsWith("{") || token.endsWith("}")) {
            throw new MinorOntopInternalBugException("Curly parentheses are not allowed when translating a RegEx to a LIKE comparison.");
        }
        if(token.contains("|")) {
            throw new MinorOntopInternalBugException("The '|' operator is not allowed when translating a RegEx to a LIKE comparison.");
        }
        if(token.contains("$") || token.contains("^")) {
            throw new MinorOntopInternalBugException("The '^' and '$' operators are only allowed at the pattern start and end respectively, when translating a RegEx to a LIKE comparison.");
        }

        return token;
    }

    /**
     * Translates a RegEx into a LIKE expression pattern by first tokenizing it and then translating every individual token.
     */
    private String translateRegex(String regex) {
        var tokens = tokenizeRegEx(regex);
        return tokens.stream()
                .map(this::translateToken)
                .collect(Collectors.joining());
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        if(!(terms.get(1) instanceof Constant))
            throw new MinorOntopInternalBugException("Regex patterns must be constants when used with SQLServer.");
        String pattern = ((Constant)terms.get(1)).getValue();
        boolean forceStart = false;
        boolean forceEnd = false;
        if(pattern.startsWith("^")) {
            forceStart = true;
            pattern = pattern.substring(1);
        }
        if(pattern.endsWith("$")) {
            forceEnd = true;
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        String newPattern = translateRegex(pattern);
        if(!forceStart) {
            newPattern = "%" + newPattern;
        }
        if(!forceEnd) {
            newPattern = newPattern + "%";
        }

        String str = termConverter.apply(terms.get(0));
        if(terms.size() == 2)
            return String.format("(%s LIKE '%s')", str, newPattern);
        else
            return String.format("(CASE WHEN %s = 'i' THEN LOWER(%s) ELSE %s END LIKE CASE WHEN %s = 'i' THEN LOWER(%s) ELSE '%s' END)",
                    termConverter.apply(terms.get(2)),
                    str,
                    str,
                    termConverter.apply(terms.get(2)),
                    newPattern,
                    newPattern);
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }
}
