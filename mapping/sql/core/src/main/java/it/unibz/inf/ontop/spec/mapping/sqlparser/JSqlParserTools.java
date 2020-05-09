package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JSqlParserTools {

    public static SelectBody parse(String sql) throws InvalidSelectQueryException, JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql, parser -> parser.withSquareBracketQuotation(true));
        if (!(statement instanceof Select))
            throw new InvalidSelectQueryException("The query is not a SELECT statement", statement);

        return ((Select) statement).getSelectBody();
    }

    /**
     *
     * @param selectBody
     * @return
     * @throws UnsupportedSelectQueryRuntimeException
     * @throws InvalidSelectQueryRuntimeException
     */

    public static PlainSelect getPlainSelect(SelectBody selectBody) {
        // other subclasses of SelectBody are
        //      SelectOperationList (INTERSECT, EXCEPT, MINUS, UNION),
        //      ValuesStatement (VALUES)
        //      WithItem ([RECURSIVE]...)

        if (!(selectBody instanceof PlainSelect))
            throw new UnsupportedSelectQueryRuntimeException("Complex SELECT statements are not supported", selectBody);

        PlainSelect plainSelect = (PlainSelect) selectBody;

        if (plainSelect.getIntoTables() != null)
            throw new InvalidSelectQueryRuntimeException("SELECT INTO is not allowed in mappings", selectBody);

        return plainSelect;
    }

    public static RAExpressionAttributes parseSelectItems(SelectItemParser sip, List<SelectItem> selectItems) {

        try {
            ImmutableMap<QualifiedAttributeID, ImmutableTerm> map = selectItems.stream()
                    .map(si -> sip.getAttributes(si).entrySet())
                    .flatMap(Collection::stream)
                    .collect(ImmutableCollectors.toMap());

            return new RAExpressionAttributes(map);
        }
        catch (IllegalArgumentException e) {
            Map<QualifiedAttributeID, Integer> duplicates = new HashMap<>();
            selectItems.stream()
                    .map(si -> sip.getAttributes(si).entrySet())
                    .flatMap(Collection::stream)
                    .forEach(a -> duplicates.put(a.getKey(), duplicates.getOrDefault(a.getKey(), 0) + 1));

            throw new InvalidSelectQueryRuntimeException(duplicates.entrySet().stream()
                    .filter(d -> d.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .map(QualifiedAttributeID::getSQLRendering)
                    .collect(Collectors.joining(", ",
                            "Duplicate column names ",
                            " in the SELECT clause: ")),  selectItems);
        }
    }
}
