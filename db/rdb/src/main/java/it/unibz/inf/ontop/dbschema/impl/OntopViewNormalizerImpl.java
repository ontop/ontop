package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.OntopViewDefinition;
import it.unibz.inf.ontop.dbschema.OntopViewNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;

public class OntopViewNormalizerImpl implements OntopViewNormalizer {

    private final NotYetTypedEqualityTransformer equalityTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected OntopViewNormalizerImpl(NotYetTypedEqualityTransformer equalityTransformer, IntermediateQueryFactory iqFactory) {
        this.equalityTransformer = equalityTransformer;
        this.iqFactory = iqFactory;
    }

    @Override
    public void normalize(OntopViewDefinition viewDefinition) {
        IQ initialIQ = viewDefinition.getIQ();
        IQ newIQ = normalizeIQ(viewDefinition.getIQ());

        if (initialIQ != newIQ)
            viewDefinition.updateIQ(newIQ);
    }

    protected IQ normalizeIQ(IQ iq) {
        IQ normalizedIQ = iq.normalizeForOptimization();
        IQTree newTree = equalityTransformer.transform(normalizedIQ.getTree());

        // TODO: add new optimization

        return (newTree == normalizedIQ.getTree())
                ? normalizedIQ
                : iqFactory.createIQ(iq.getProjectionAtom(), newTree);
    }
}
