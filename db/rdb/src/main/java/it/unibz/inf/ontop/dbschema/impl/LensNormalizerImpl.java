package it.unibz.inf.ontop.dbschema.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.Lens;
import it.unibz.inf.ontop.dbschema.LensNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.NotYetTypedBinaryMathOperationTransformer;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;

public class LensNormalizerImpl implements LensNormalizer {

    private final NotYetTypedEqualityTransformer equalityTransformer;
    private final NotYetTypedBinaryMathOperationTransformer binaryMathOperationTransformer;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    protected LensNormalizerImpl(NotYetTypedEqualityTransformer equalityTransformer, NotYetTypedBinaryMathOperationTransformer binaryMathOperationTransformer, IntermediateQueryFactory iqFactory) {
        this.equalityTransformer = equalityTransformer;
        this.binaryMathOperationTransformer = binaryMathOperationTransformer;
        this.iqFactory = iqFactory;
    }

    @Override
    public void normalize(Lens lens) {
        IQ initialIQ = lens.getIQ();
        IQ newIQ = normalizeIQ(lens.getIQ());

        if (initialIQ != newIQ)
            lens.updateIQ(newIQ);
    }

    protected IQ normalizeIQ(IQ iq) {
        IQ normalizedIQ = iq.normalizeForOptimization();
        IQTree newTree = equalityTransformer.transform(normalizedIQ.getTree());
        IQTree finalTree = binaryMathOperationTransformer.transform(newTree);

        // TODO: add new optimization

        return (finalTree == normalizedIQ.getTree())
                ? normalizedIQ
                : iqFactory.createIQ(iq.getProjectionAtom(), finalTree);
    }
}
