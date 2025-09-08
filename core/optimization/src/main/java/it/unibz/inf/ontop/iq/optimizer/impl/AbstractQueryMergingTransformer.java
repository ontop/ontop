package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
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
public abstract class AbstractQueryMergingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    protected final VariableGenerator variableGenerator;
    protected final SubstitutionFactory substitutionFactory;
    protected final QueryTransformerFactory transformerFactory;

    protected AbstractQueryMergingTransformer(VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        this(variableGenerator, coreSingletons.getIQFactory(), coreSingletons.getSubstitutionFactory(),
                coreSingletons.getQueryTransformerFactory());
    }

    protected AbstractQueryMergingTransformer(VariableGenerator variableGenerator,
                                              IntermediateQueryFactory iqFactory,
                                              SubstitutionFactory substitutionFactory,
                                              QueryTransformerFactory transformerFactory) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
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

        IQ renamedDefinition = transformerFactory.createRenamer(renamingSubstitution).transform(definition);

        Substitution<? extends VariableOrGroundTerm> descendingSubstitution = substitutionFactory.getSubstitution(
                renamedDefinition.getProjectionAtom().getArguments(),
                dataNode.getProjectionAtom().getArguments());

        return renamedDefinition.getTree()
                .applyDescendingSubstitution(descendingSubstitution, Optional.empty(), variableGenerator)
                .normalizeForOptimization(variableGenerator);
    }
}
