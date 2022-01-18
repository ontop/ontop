package it.unibz.inf.ontop.spec.sqlparser;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.spec.sqlparser.exception.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;


/**
 * Created by Roman Kontchakov on 25/01/2017.
 */

public class DefaultSelectQueryAttributeExtractor extends FromItemParser<RAExpressionAttributes> {

    public DefaultSelectQueryAttributeExtractor(MetadataLookup metadata, CoreSingletons coreSingletons) {
        super(metadata, coreSingletons, new RAExpressionAttributesOperations());
    }

    public RAExpressionAttributes getRAExpressionAttributes(SelectBody selectBody) throws InvalidQueryException, UnsupportedSelectQueryException {
        try {
            return translateSelectBody(selectBody);
        }
        catch (InvalidSelectQueryRuntimeException e) {
            throw new InvalidQueryException(e.getMessage(), e.getObject());
        }
        catch (UnsupportedSelectQueryRuntimeException e) {
            throw new UnsupportedSelectQueryException(e.getMessage(), e.getObject());
        }
    }


    @Override
    protected RAExpressionAttributes translateSelectBody(SelectBody selectBody) {
        PlainSelect plainSelect = JSqlParserTools.getPlainSelect(selectBody);

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
