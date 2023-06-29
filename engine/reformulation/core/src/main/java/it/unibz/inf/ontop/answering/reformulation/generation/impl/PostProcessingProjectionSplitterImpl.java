package it.unibz.inf.ontop.answering.reformulation.generation.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.PostProcessingProjectionSplitter;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.normalization.DistinctNormalizer;
import it.unibz.inf.ontop.iq.optimizer.splitter.impl.ProjectionSplitterImpl;
import it.unibz.inf.ontop.iq.tools.ProjectionDecomposer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

public class PostProcessingProjectionSplitterImpl extends ProjectionSplitterImpl implements PostProcessingProjectionSplitter {

    private final ProjectionDecomposer avoidPostProcessingDecomposer;
    private final ProjectionDecomposer proPostProcessingDecomposer;

    @Inject
    private PostProcessingProjectionSplitterImpl(IntermediateQueryFactory iqFactory,
                                                 SubstitutionFactory substitutionFactory,
                                                 CoreUtilsFactory coreUtilsFactory,
                                                 DistinctNormalizer distinctNormalizer) {
        super(iqFactory, substitutionFactory, distinctNormalizer);
        this.avoidPostProcessingDecomposer = coreUtilsFactory.createProjectionDecomposer(
                PostProcessingProjectionSplitterImpl::hasFunctionalToBePostProcessed,
                t -> !(t.isNull() || (t instanceof Variable) || (t instanceof DBConstant)));
        this.proPostProcessingDecomposer = coreUtilsFactory.createProjectionDecomposer(
                ImmutableFunctionalTerm::canBePostProcessed, t -> true);
    }

    private static boolean hasFunctionalToBePostProcessed(ImmutableFunctionalTerm functionalTerm) {
        if (!functionalTerm.canBePostProcessed())
            return false;

        if (!(functionalTerm.getFunctionSymbol() instanceof DBFunctionSymbol))
            return true;

        return functionalTerm.getTerms().stream()
                .anyMatch(PostProcessingProjectionSplitterImpl::hasToBePostProcessed);
    }

    private static boolean hasToBePostProcessed(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm)
            return hasFunctionalToBePostProcessed((ImmutableFunctionalTerm) term);

        return !term.isNull() && (!(term instanceof DBConstant)) && (!(term instanceof Variable));
    }

    @Override
    public ProjectionSplit split(IQ initialIQ, boolean avoidPostProcessing) {
        return split(initialIQ, avoidPostProcessing ? avoidPostProcessingDecomposer : proPostProcessingDecomposer);
    }


    @Override
    public ProjectionSplit split(IQTree tree, VariableGenerator variableGenerator) {
        return split(tree, variableGenerator, avoidPostProcessingDecomposer);
    }
}
