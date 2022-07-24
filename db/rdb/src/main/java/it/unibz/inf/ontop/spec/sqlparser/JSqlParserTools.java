package it.unibz.inf.ontop.spec.sqlparser;

import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;


public class JSqlParserTools {



    public static Select parse(String sql) throws JSQLParserException, InvalidQueryException {
        Statement statement = CCJSqlParserUtil.parse(sql, parser -> parser.withSquareBracketQuotation(true));
        if (!(statement instanceof Select))
            throw new InvalidQueryException("The query is not a SELECT statement", statement);

        return (Select) statement;
    }

    public static RelationID getRelationId(QuotedIDFactory idfac, Table table) {
        // a massive workaround for JSQLParser, which supports long names
        // but does NOT give direct access to the components
        if (table.getSchemaName() == null)
            return idfac.createRelationID(table.getName());
        
        if (table.getDatabase().getDatabaseName() == null)
            return idfac.createRelationID(table.getSchemaName(), table.getName());
        
        String s = table.getFullyQualifiedName();
        return idfac.createRelationID(s.split("\\."));
    }
}
