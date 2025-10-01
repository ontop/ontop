package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.DownPropagation;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

/**
 * Replaces intensional data nodes by an IQTree
 * <p>
 * Does NOT look for intensional data nodes inside the definitions
 */
public abstract class AbstractQueryMergingTransformer extends DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator {

    protected final SubstitutionFactory substitutionFactory;
    protected final IQTreeTools iqTreeTools;

    protected AbstractQueryMergingTransformer(VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        this(variableGenerator, coreSingletons.getIQFactory(), coreSingletons.getSubstitutionFactory(),
                coreSingletons.getIQTreeTools());
    }

    protected AbstractQueryMergingTransformer(VariableGenerator variableGenerator,
                                              IntermediateQueryFactory iqFactory,
                                              SubstitutionFactory substitutionFactory,
                                              IQTreeTools iqTreeTools) {
        super(iqFactory, variableGenerator);
        this.substitutionFactory = substitutionFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public final IQTree transformIntensionalData(IntensionalDataNode dataNode) {
        Optional<IQ> definition = getDefinition(dataNode);
        return definition
                .map(d -> replaceIntensionalData(dataNode, d))
                .orElseGet(() -> handleIntensionalWithoutDefinition(dataNode));
    }

    protected abstract Optional<IQ> getDefinition(IntensionalDataNode dataNode);

    protected abstract IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode);

    /**
     * Does NOT look for intensional data nodes inside the definitions
     */
    private IQTree replaceIntensionalData(IntensionalDataNode dataNode, IQ definition) {
        if (!definition.getProjectionAtom().getPredicate().equals(dataNode.getProjectionAtom().getPredicate()))
            throw new IllegalStateException("Incompatible predicates");

        IQ renamedDefinition = iqTreeTools.getFreshInstance(definition, variableGenerator);

        Substitution<? extends VariableOrGroundTerm> descendingSubstitution = substitutionFactory.getSubstitution(
                renamedDefinition.getProjectionAtom().getArguments(),
                dataNode.getProjectionAtom().getArguments());

        try {
            DownPropagation dp = iqTreeTools.createDownPropagation(descendingSubstitution, Optional.empty(), renamedDefinition.getTree().getVariables(), variableGenerator);
            return dp.propagate(renamedDefinition.getTree())
                    .normalizeForOptimization(variableGenerator);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            throw new MinorOntopInternalBugException("IntensionalDataNode cannot contains NULLs" + dataNode, e);
        }
    }
}
