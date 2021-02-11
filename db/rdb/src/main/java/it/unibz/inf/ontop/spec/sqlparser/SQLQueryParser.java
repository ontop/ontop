package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.TokenMgrException;
import net.sf.jsqlparser.statement.select.SelectBody;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-level SQL query parser
 */
public class SQLQueryParser {

    private final CoreSingletons coreSingletons;
    private final DBTypeFactory dbTypeFactory;

    @Inject
    private SQLQueryParser(CoreSingletons coreSingletons, TypeFactory typeFactory) {
        this.coreSingletons = coreSingletons;
        this.dbTypeFactory = typeFactory.getDBTypeFactory();
    }

    public RAExpression getRAExpression(String sourceQuery, MetadataLookup metadataLookup) throws InvalidSelectQueryException {
        SelectQueryParser sqp = new SelectQueryParser(metadataLookup, coreSingletons);
        ImmutableList<QuotedID> attributes;
        try {
            SelectBody selectBody = JSqlParserTools.parse(sourceQuery);
            try {
                return sqp.parse(selectBody);
            }
            catch (UnsupportedSelectQueryException e) {
                DefaultSelectQueryAttributeExtractor sqae = new DefaultSelectQueryAttributeExtractor(metadataLookup, coreSingletons);
                ImmutableMap<QuotedID, ImmutableTerm> attrs = sqae.getRAExpressionAttributes(selectBody).getUnqualifiedAttributes();
                attributes = ImmutableList.copyOf(attrs.keySet());
            }
        }
        catch (JSQLParserException e) {
            // TODO: LOGGER.warn() should be instead after revising the logging policy
            System.out.println(String.format("FAILED TO PARSE: %s %s", sourceQuery, getJSQLParserErrorMessage(sourceQuery, e)));

            ApproximateSelectQueryAttributeExtractor sqae = new ApproximateSelectQueryAttributeExtractor(metadataLookup.getQuotedIDFactory());
            attributes = sqae.getAttributes(sourceQuery);
        }
        catch (UnsupportedSelectQueryException e) {
            ApproximateSelectQueryAttributeExtractor sqae = new ApproximateSelectQueryAttributeExtractor(metadataLookup.getQuotedIDFactory());
            attributes = sqae.getAttributes(sourceQuery);
        }
        // TODO: LOGGER.warn() should be instead after revising the logging policy
        System.out.println("PARSER VIEW FOR " + sourceQuery);
        ParserViewDefinition view = new ParserViewDefinition(attributes, sourceQuery, dbTypeFactory);
        return sqp.translateParserView(view);
    }

    private static String getJSQLParserErrorMessage(String sourceQuery, JSQLParserException e) {
        try {
            // net.sf.jsqlparser.parser.TokenMgrException: Lexical error at line 1, column 165.
            if (e.getCause() instanceof TokenMgrException) {
                Pattern pattern = Pattern.compile("at line (\\d+), column (\\d+)");
                Matcher matcher = pattern.matcher(e.getCause().getMessage());
                if (matcher.find()) {
                    int line = Integer.parseInt(matcher.group(1));
                    int col = Integer.parseInt(matcher.group(2));
                    String sourceQueryLine = sourceQuery.split("\n")[line - 1];
                    final int MAX_LENGTH = 40;
                    if (sourceQueryLine.length() > MAX_LENGTH) {
                        sourceQueryLine = sourceQueryLine.substring(sourceQueryLine.length() - MAX_LENGTH);
                        if (sourceQueryLine.length() > 2 * MAX_LENGTH)
                            sourceQueryLine = sourceQueryLine.substring(0, 2 * MAX_LENGTH);
                        col = MAX_LENGTH;
                    }
                    return "FAILED TO PARSE: " + sourceQueryLine + "\n" +
                            Strings.repeat(" ", "FAILED TO PARSE: ".length() + col - 2) + "^\n" + e.getCause();
                }
            }
        }
        catch (Exception e1) {
            // NOP
        }
        return e.getCause().toString();
    }
}
