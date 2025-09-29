package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.DownPropagation;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.iq.visit.impl.DefaultRecursiveIQTreeVisitingTransformerWithVariableGenerator;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
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
    protected final QueryRenamer queryRenamer;
    protected final TermFactory termFactory;

    protected AbstractQueryMergingTransformer(VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        this(variableGenerator, coreSingletons.getIQFactory(), coreSingletons.getSubstitutionFactory(),
                coreSingletons.getQueryRenamer(), coreSingletons.getTermFactory());
    }

    protected AbstractQueryMergingTransformer(VariableGenerator variableGenerator,
                                              IntermediateQueryFactory iqFactory,
                                              SubstitutionFactory substitutionFactory,
                                              QueryRenamer queryRenamer,
                                              TermFactory termFactory) {
        super(iqFactory, variableGenerator);
        this.substitutionFactory = substitutionFactory;
        this.queryRenamer = queryRenamer;
        this.termFactory = termFactory;
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

        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                variableGenerator, definition.getTree().getKnownVariables());

        IQ renamedDefinition = queryRenamer.applyInDepthRenaming(renamingSubstitution, definition);

        Substitution<? extends VariableOrGroundTerm> descendingSubstitution = substitutionFactory.getSubstitution(
                renamedDefinition.getProjectionAtom().getArguments(),
                dataNode.getProjectionAtom().getArguments());

        try {
            DownPropagation dp = DownPropagation.of(descendingSubstitution, Optional.empty(), renamedDefinition.getTree().getVariables(), variableGenerator, termFactory);
            return dp.propagate(renamedDefinition.getTree())
                    .normalizeForOptimization(variableGenerator);
        }
        catch (DownPropagation.InconsistentDownPropagationException e) {
            throw new MinorOntopInternalBugException("IntensionalDataNode cannot contains NULLs" + dataNode, e);
        }
    }
}
