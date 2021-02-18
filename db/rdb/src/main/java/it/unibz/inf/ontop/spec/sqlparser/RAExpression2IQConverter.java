package it.unibz.inf.ontop.spec.sqlparser;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.tools.impl.IQ2CQ;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class RAExpression2IQConverter {

    private final TermFactory termFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    public RAExpression2IQConverter(TermFactory termFactory, IntermediateQueryFactory iqFactory) {
        this.termFactory = termFactory;

        this.iqFactory = iqFactory;
    }

    public IQTree convert(RAExpression re) {
        return IQ2CQ.toIQTree(
                re.getDataAtoms().stream()
                        .collect(ImmutableCollectors.toList()),
                termFactory.getConjunction(re.getFilterAtoms().stream()),
                iqFactory);
    }


}
