package it.unibz.inf.ontop.iq.type.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

public abstract class AbstractTypedTermTransformer extends AbstractTermTransformer {
    protected final SingleTermTypeExtractor typeExtractor;

    protected AbstractTypedTermTransformer(IntermediateQueryFactory iqFactory, TermFactory termFactory, SingleTermTypeExtractor typeExtractor) {
        super(iqFactory, termFactory);
        this.typeExtractor = typeExtractor;
    }

    protected final Optional<DBTermType> getDBTermType(ImmutableTerm term, IQTree tree) {
        return typeExtractor.extractSingleTermType(term, tree)
                .filter(t -> t instanceof DBTermType)
                .map(t -> (DBTermType) t);
    }
}
