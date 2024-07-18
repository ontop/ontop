package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.*;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public abstract class AbstractIntensionalQueryMerger implements IQOptimizer {

    protected final IntermediateQueryFactory iqFactory;

    protected AbstractIntensionalQueryMerger(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree newTree = optimize(query.getTree());
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected IQTree optimize(IQTree tree) {
        QueryMergingTransformer transformer = createTransformer(tree.getKnownVariables());
        return tree.acceptTransformer(transformer);
    }

    protected abstract QueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables);

    /**
     * Replaces intensional data nodes by an IQTree
     *
     * Does NOT look for intensional data nodes inside the definitions
     *
     */
    protected static abstract class QueryMergingTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;
        private final SubstitutionFactory substitutionFactory;
        private final QueryTransformerFactory transformerFactory;
        private final AtomFactory atomFactory;

        protected QueryMergingTransformer(VariableGenerator variableGenerator,
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
         *
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
}
