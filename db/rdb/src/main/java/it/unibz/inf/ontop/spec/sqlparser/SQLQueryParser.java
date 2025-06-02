package it.unibz.inf.ontop.spec.sqlparser;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.MetadataLookup;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryException;
import net.sf.jsqlparser.JSQLParserException;

import java.util.Optional;

/**
 * High-level SQL query parser
 */
public class SQLQueryParser {

    private final CoreSingletons coreSingletons;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    public SQLQueryParser(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
        this.termFactory = coreSingletons.getTermFactory();
        this.iqTreeTools = coreSingletons.getIQTreeTools();
        this.iqFactory = coreSingletons.getIQFactory();
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
        Optional<ImmutableExpression> joiningConditions = termFactory.getConjunction(re.getFilterAtoms().stream());
        return iqTreeTools.createOptionalInnerJoinTree(joiningConditions, re.getDataAtoms())
                .orElseGet(iqFactory::createTrueNode);
    }
}
