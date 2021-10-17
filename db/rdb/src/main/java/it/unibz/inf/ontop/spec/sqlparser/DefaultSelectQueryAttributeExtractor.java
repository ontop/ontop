package it.unibz.inf.ontop.spec.sqlparser;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.sqlparser.exception.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;


/**
 * Created by Roman Kontchakov on 25/01/2017.
 */

public class DefaultSelectQueryAttributeExtractor extends FromItemParser<RAExpressionAttributes> {
    private final QuotedIDFactory idfac;
    private final TermFactory termFactory;
    private final CoreSingletons coreSingletons;

    public DefaultSelectQueryAttributeExtractor(MetadataLookup metadata, CoreSingletons coreSingletons) {
        super(new ExpressionParser(metadata.getQuotedIDFactory(), coreSingletons), metadata.getQuotedIDFactory(), metadata, coreSingletons.getTermFactory(), new RAExpressionAttributesOperations());
        this.idfac = metadata.getQuotedIDFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.coreSingletons = coreSingletons;
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

        ExpressionParser ep = new ExpressionParser(idfac, coreSingletons);
        SelectItemParser sip = new SelectItemParser(attributes,
                (e, a) -> (e instanceof Column)
                        ? ep.parseTerm(e, a)
                        : termFactory.getVariable("something"), idfac);

        return sip.parseSelectItems(plainSelect.getSelectItems());
    }

    @Override
    protected RAExpressionAttributes create(NamedRelationDefinition relation) {
        return ((RAExpressionAttributesOperations)operations).create(relation, createAttributeVariables(relation));
    }
}
