package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * See {@link QueryUnfolder.Factory} for creating a new instance.
 */
public class TwoPhaseQueryUnfolder extends AbstractIQOptimizer implements QueryUnfolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwoPhaseQueryUnfolder.class);

    private final Mapping mapping;

    private final CoreSingletons coreSingletons;

    /**
     * See {@link QueryUnfolder.Factory#create(Mapping)}
     */
    @AssistedInject
    private TwoPhaseQueryUnfolder(@Assisted Mapping mapping, CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory());
        this.mapping = mapping;
        this.coreSingletons = coreSingletons;
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return this::transformTree;
    }

    private IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        long before = System.currentTimeMillis();
        FirstPhaseQueryMergingTransformer firstPhaseTransformer = new FirstPhaseQueryMergingTransformer(mapping, variableGenerator, coreSingletons);

        // NB: no normalization at that point, because of limitations of getPossibleVariableDefinitions implementation
        // (Problem with join strict equality and condition)
        IQTree partiallyUnfoldedIQ = tree.acceptVisitor(firstPhaseTransformer);
        LOGGER.debug("First phase query unfolding time: {}", System.currentTimeMillis() - before);

        if (!firstPhaseTransformer.areSomeIntensionalNodesRemaining())
            return partiallyUnfoldedIQ;

        return executeSecondPhaseUnfolding(partiallyUnfoldedIQ, variableGenerator);
    }

    private IQTree executeSecondPhaseUnfolding(IQTree partiallyUnfoldedIQ, VariableGenerator variableGenerator){
        long before = System.currentTimeMillis();
        IQTree unfoldedIQ = partiallyUnfoldedIQ.acceptVisitor(new SecondPhaseQueryMergingTransformer(
                partiallyUnfoldedIQ.getPossibleVariableDefinitions(), mapping, variableGenerator, coreSingletons));
        LOGGER.debug("Second phase query unfolding time: {}", System.currentTimeMillis() - before);
        return unfoldedIQ;
    }
}
