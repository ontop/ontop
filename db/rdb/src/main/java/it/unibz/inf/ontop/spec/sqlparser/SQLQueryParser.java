package it.unibz.inf.ontop.spec.sqlparser;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.tools.impl.IQ2CQ;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import net.sf.jsqlparser.JSQLParserException;

/**
 * High-level SQL query parser
 */
public class SQLQueryParser {

    private final CoreSingletons coreSingletons;
    private final TermFactory termFactory;

    @Inject
    public SQLQueryParser(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.termFactory = coreSingletons.getTermFactory();
    }

    public RAExpression getRAExpression(String sourceQuery, MetadataLookup metadataLookup) throws InvalidQueryException, MetadataExtractionException {
        SelectQueryParser sqp = new SelectQueryParser(metadataLookup, coreSingletons);
        try {
            return sqp.parse(sourceQuery);
        }
        catch (UnsupportedSelectQueryException | JSQLParserException e) {
            RelationDefinition view = metadataLookup.getBlackBoxView(sourceQuery);
            return sqp.translateParserView(view);
        }
    }

    public IQTree convert(RAExpression re) {
        return IQ2CQ.toIQTree(
                re.getDataAtoms().stream()
                        .collect(ImmutableCollectors.toList()),
                termFactory.getConjunction(re.getFilterAtoms().stream()),
                coreSingletons);
    }

}
