package it.unibz.inf.ontop.spec.sqlparser;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.SelectBody;

/**
 * High-level SQL query parser
 */
public class SQLQueryParser {

    private final CoreSingletons coreSingletons;

    @Inject
    public SQLQueryParser(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    public RAExpression getRAExpression(String sourceQuery, MetadataLookup metadataLookup) throws InvalidQueryException, MetadataExtractionException {
        SelectQueryParser sqp = new SelectQueryParser(metadataLookup, coreSingletons);
        try {
            SelectBody selectBody = JSqlParserTools.parse(sourceQuery);
            return sqp.parse(selectBody);
        }
        catch (UnsupportedSelectQueryException | JSQLParserException e) {
            RelationDefinition view = metadataLookup.getBlackBoxView(sourceQuery);
            return sqp.translateParserView(view);
        }
    }
}
