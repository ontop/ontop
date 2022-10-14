package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.lang.reflect.Field;
import java.util.List;


public class JSqlParserTools {



    public static Select parse(String sql) throws JSQLParserException, InvalidQueryException {
        Statement statement = CCJSqlParserUtil.parse(sql, parser -> parser.withSquareBracketQuotation(true));
        if (!(statement instanceof Select))
            throw new InvalidQueryException("The query is not a SELECT statement", statement);

        return (Select) statement;
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
