package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.spec.sqlparser.exception.QueryParseException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JSqlParserTools {

    public static Select parse(String sql, boolean withSquareBracketArrayAccess) throws InvalidQueryException, QueryParseException {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql, parser -> parser.withSquareBracketQuotation(!withSquareBracketArrayAccess));
            if (!(statement instanceof Select))
                throw new InvalidQueryException("The query is not a SELECT statement", statement);

            validateJoinModifiers(sql, (Select) statement);
            return (Select) statement;
        }
        catch (JSQLParserException e) {
            Throwable cause = e;
            while (cause.getCause() != null && !(cause instanceof ParseException))
                cause = cause.getCause();
            throw new QueryParseException(sql, getJSQLParserErrorMessage(sql, cause), cause.getMessage());
        }
    }

    private static void validateJoinModifiers(String sql, Select select) throws QueryParseException {
        // 5.4 accepts some contradictory modifiers and can silently discard one of them.
        for (Token token = select.getASTNode().jjtGetFirstToken(); token != null; token = token.next) {
            if (token.next == null || token.next.next == null)
                continue;
            String modifier = token.image.toUpperCase(java.util.Locale.ROOT);
            String next = token.next.image.toUpperCase(java.util.Locale.ROOT);
            if ("JOIN".equalsIgnoreCase(token.next.next.image)
                    && (("NATURAL".equals(modifier) && ("INNER".equals(next) || "OUTER".equals(next)))
                    || (("LEFT".equals(modifier) || "RIGHT".equals(modifier)) && "INNER".equals(next)))) {
                String message = "Invalid JOIN modifiers: " + modifier + " " + next;
                throw new QueryParseException(sql, MESSAGE + message, message);
            }
        }
    }

    private static final Pattern pattern = Pattern.compile("at line (\\d+), column (\\d+)");
    private static final int MAX_LENGTH = 40;
    private static final String MESSAGE = "Unable to parse SQL: ";

    private static String getJSQLParserErrorMessage(String sql, Throwable e) {
        if (e instanceof ParseException)
            return getJSQLParseExceptionMessage(sql, (ParseException) e);
        return e.toString();
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


    public static RelationID getRelationId(QuotedIDFactory idfac, Table table) {
        // The single-name Table constructor in 5.4 splits even a quoted name on dots.
        if (table.getASTNode() != null) {
            Token first = table.getASTNode().jjtGetFirstToken();
            if (isQuoted(first.image)) {
                if (first == table.getASTNode().jjtGetLastToken()
                        || first.next == null || !".".equals(first.next.image))
                    return idfac.createRelationID(first.image);
            }
        }
        if (table.getSchemaName() == null)
            return idfac.createRelationID(table.getName());
        
        if (table.getDatabase().getDatabaseName() == null)
            return idfac.createRelationID(table.getSchemaName(), table.getName());

        return idfac.createRelationID(ImmutableList.copyOf(table.getNameParts()).reverse().toArray(new String[0]));
    }

    public static RelationID getRelationId(QuotedIDFactory idfac, Column column) {
        Table table = column.getTable();
        if (table.getASTNode() == null && column.getASTNode() != null) {
            Token first = column.getASTNode().jjtGetFirstToken();
            if (isQuoted(first.image) && first.next != null && ".".equals(first.next.image)
                    && first.next.next != null && first.next.next.image.equals(column.getColumnName()))
                return idfac.createRelationID(first.image);
        }
        return getRelationId(idfac, table);
    }

    public static String getTableName(Table table) {
        if (table.getASTNode() != null) {
            Token first = table.getASTNode().jjtGetFirstToken();
            if (isQuoted(first.image) && (first == table.getASTNode().jjtGetLastToken()
                    || first.next == null || !".".equals(first.next.image)))
                return first.image;
        }
        return table.getName();
    }

    private static boolean isQuoted(String name) {
        return name.startsWith("\"") || name.startsWith("`") || name.startsWith("[");
    }
}
