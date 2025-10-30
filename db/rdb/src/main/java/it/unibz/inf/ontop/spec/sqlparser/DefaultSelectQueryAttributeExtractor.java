package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.spec.sqlparser.exception.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;


/**
 * Created by Roman Kontchakov on 25/01/2017.
 */

public class DefaultSelectQueryAttributeExtractor extends BasicSelectQueryParser<RAExpressionAttributes, RAExpressionAttributesOperations> {

    public DefaultSelectQueryAttributeExtractor(MetadataLookup metadata, CoreSingletons coreSingletons) {
        super(metadata, coreSingletons, new RAExpressionAttributesOperations());
    }

    public ImmutableList<QuotedID> getRAExpressionAttributes(Select select) throws InvalidQueryException, UnsupportedSelectQueryException {
        try {
            return ImmutableList.copyOf(translateSelect(select.getSelectBody(), select.getWithItemsList()).getUnqualifiedAttributes().keySet());
        }
        catch (InvalidSelectQueryRuntimeException e) {
            throw new InvalidQueryException(e.getMessage(), e.getObject());
        }
        catch (UnsupportedSelectQueryRuntimeException e) {
            throw new UnsupportedSelectQueryException(e.getMessage(), e.getObject());
        }
    }


    @Override
    protected RAExpressionAttributes translateSelect(SelectBody selectBody, List<WithItem> withItemsList) {
        PlainSelect plainSelect = getPlainSelect(selectBody);

        RAExpressionAttributes attributes;
        try {
            attributes = translateJoins(plainSelect.getFromItem(), plainSelect.getJoins());
        }
        catch (IllegalJoinException e) {
            throw new InvalidSelectQueryRuntimeException(e.toString(), plainSelect);
        }

        SelectItemParser sip = new SelectItemParser(attributes,
                (e, a) -> (e instanceof Column)
                        ? expressionParser.parseTerm(e, a)
                        : termFactory.getVariable("something"), idfac);

        return sip.parseSelectItems(plainSelect.getSelectItems());
    }

    @Override
    protected RAExpressionAttributes create(NamedRelationDefinition relation) {
        return operations.create(relation, createAttributeVariables(relation));
    }
}
