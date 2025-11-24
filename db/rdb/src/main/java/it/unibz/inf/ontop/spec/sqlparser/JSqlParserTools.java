package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.spec.sqlparser.exception.QueryParseException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JSqlParserTools {

    public static Select parse(String sql, boolean withSquareBracketArrayAccess) throws InvalidQueryException, QueryParseException {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql, parser -> parser.withSquareBracketQuotation(!withSquareBracketArrayAccess));
            if (!(statement instanceof Select))
                throw new InvalidQueryException("The query is not a SELECT statement", statement);

            return (Select) statement;
        }
        catch (JSQLParserException e) {
            throw new QueryParseException(sql, getJSQLParserErrorMessage(sql, e), e.getMessage());
        }
    }

    private static final Pattern pattern = Pattern.compile("at line (\\d+), column (\\d+)");
    private static final int MAX_LENGTH = 40;
    private static final String MESSAGE = "Unable to parse SQL: ";

    private static String getJSQLParserErrorMessage(String sql, JSQLParserException e) {
        try {
            if (e.getCause() instanceof ParseException)
                return getJSQLParseExceptionMessage(sql, (ParseException)e.getCause());
        }
        catch (Exception e1) {
            // NOP
        }
        return e.getCause().toString();
    }

    private static String getJSQLParseExceptionMessage(String sql, ParseException e) {
        // net.sf.jsqlparser.parser.ParseException: Encountered unexpected token: "LEFT" "LEFT"
        //    at line 1, column 165.

        Matcher matcher = pattern.matcher(e.getMessage());
        if (matcher.find()) {
            int line = Integer.parseInt(matcher.group(1));
            int col = Integer.parseInt(matcher.group(2));
            String sourceQueryLine = sql.split("\n")[line - 1];
            if (sourceQueryLine.length() > MAX_LENGTH) {
                sourceQueryLine = sourceQueryLine.substring(sourceQueryLine.length() - MAX_LENGTH);
                if (sourceQueryLine.length() > 2 * MAX_LENGTH)
                    sourceQueryLine = sourceQueryLine.substring(0, 2 * MAX_LENGTH);
                col = MAX_LENGTH;
            }
            return MESSAGE + sourceQueryLine + "\n" +
                    Strings.repeat(" ", MESSAGE.length() + col - 2) + "^\n" + e;
        }
        return e.toString();
    }


    private static Field partsField;

    public static RelationID getRelationId(QuotedIDFactory idfac, Table table) {
        if (table.getSchemaName() == null)
            return idfac.createRelationID(table.getName());
        
        if (table.getDatabase().getDatabaseName() == null)
            return idfac.createRelationID(table.getSchemaName(), table.getName());

        // a massive workaround for JSQLParser, which supports long names
        // but does NOT give direct access to the components, so use Reflection API
        if (partsField == null) {
            try {
                partsField = Table.class.getDeclaredField("partItems");
                partsField.setAccessible(true);
            }
            catch (NoSuchFieldException e) {
                throw new MinorOntopInternalBugException("Cannot find the partsItems field in JSQLParser: " + e);
            }
        }
        try {
            List<String> parts = (List<String>) partsField.get(table);
            return idfac.createRelationID(ImmutableList.copyOf(parts).reverse().toArray(new String[0]));
        }
        catch (IllegalAccessException e) {
            throw new MinorOntopInternalBugException("Cannot access the partsItems field in JSQLParser: " + e);
        }

        //String s = table.getFullyQualifiedName();
        //return idfac.createRelationID(s.split("\\."));
    }
}
