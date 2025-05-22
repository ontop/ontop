package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
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

    private final VariableGenerator variableGenerator;
    private final SubstitutionFactory substitutionFactory;
    private final QueryTransformerFactory transformerFactory;
    private final AtomFactory atomFactory;

    protected AbstractQueryMergingTransformer(VariableGenerator variableGenerator, CoreSingletons coreSingletons) {
        this(variableGenerator, coreSingletons.getIQFactory(), coreSingletons.getSubstitutionFactory(),
                coreSingletons.getAtomFactory(), coreSingletons.getQueryTransformerFactory());
    }

    protected AbstractQueryMergingTransformer(VariableGenerator variableGenerator,
                                              IntermediateQueryFactory iqFactory,
                                              SubstitutionFactory substitutionFactory,
                                              AtomFactory atomFactory,
                                              QueryTransformerFactory transformerFactory) {
        super(iqFactory);
        this.variableGenerator = variableGenerator;
        this.substitutionFactory = substitutionFactory;
        this.transformerFactory = transformerFactory;
        this.atomFactory = atomFactory;
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
        InjectiveSubstitution<Variable> renamingSubstitution = substitutionFactory.generateNotConflictingRenaming(
                variableGenerator, definition.getTree().getKnownVariables());

        IQ renamedIQ = transformerFactory.createRenamer(renamingSubstitution).transform(definition);

        Substitution<? extends VariableOrGroundTerm> descendingSubstitution = extractSubstitution(
                atomFactory.getDistinctVariableOnlyDataAtom(renamedIQ.getProjectionAtom().getPredicate(),
                        substitutionFactory.apply(renamingSubstitution, renamedIQ.getProjectionAtom().getArguments())),
                dataNode.getProjectionAtom());

        return renamedIQ.getTree()
                .applyDescendingSubstitution(descendingSubstitution, Optional.empty(), variableGenerator)
                .normalizeForOptimization(variableGenerator);
    }

    private Substitution<? extends VariableOrGroundTerm> extractSubstitution(DistinctVariableOnlyDataAtom sourceAtom,
                                                                             DataAtom<AtomPredicate> targetAtom) {
        if (!sourceAtom.getPredicate().equals(targetAtom.getPredicate())) {
            throw new IllegalStateException("Incompatible predicates");
        }

        return substitutionFactory.getSubstitution(sourceAtom.getArguments(), targetAtom.getArguments());
    }
}
