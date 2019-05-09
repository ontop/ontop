package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.NestedView;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.iq.optimizer.LevelUpOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 */
public class LevelUpOptimizerImpl implements LevelUpOptimizer {

    private final IntermediateQueryFactory iqFactory;
    private final AtomFactory atomFactory;

    @Inject
    private LevelUpOptimizerImpl(IntermediateQueryFactory iqFactory,
                                 AtomFactory atomFactory) {
        this.iqFactory = iqFactory;
        this.atomFactory = atomFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        // Select the relation to expand
        Optional<DatabaseRelationDefinition> dR = selectLeafRelation(query.getTree());
        if (dR.isPresent()){
            return optimize(
                    iqFactory.createIQ(
                            query.getProjectionAtom(),
                            query.getTree().acceptTransformer(
                                    new TreeTransformer(
                                            iqFactory,
                                            dR.get(),
                                            query.getVariableGenerator()
                                    ))));
        }
        return query;
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {


        private final DatabaseRelationDefinition dR;
        private final VariableGenerator variableGenerator;

        TreeTransformer(IntermediateQueryFactory iqFactory, DatabaseRelationDefinition dR, VariableGenerator variableGenerator) {
            super(iqFactory);
            this.dR = dR;
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            DatabaseRelationDefinition rDef = (DatabaseRelationDefinition) dataNode.getDataAtom().getPredicate().getRelationDefinition();
            if (rDef.equals(dR)) {
                Variable var = variableGenerator.generateNewVariable();
                return iqFactory.createUnaryIQTree(
                        generateFlattenNode(var, rDef, dataNode.getDataAtom().getArguments()),
                        nestDataNode(dataNode.getDataAtom(), var)
                );
            }
            return dataNode;
        }

        private IQTree nestDataNode(DataAtom<RelationPredicate> dataAtom, Variable var) {
            RelationDefinition rDef = dataAtom.getPredicate().getRelationDefinition();
            if (rDef instanceof NestedView) {
                NestedView nv = (NestedView) rDef;
                ImmutableList<Variable> vars = IntStream.range(0, nv.getParentRelation().getAttributes().size()).boxed()
                        .map(i -> i == nv.getIndexInParentRelation()?
                                var:
                                variableGenerator.generateNewVariable())
                        .collect(ImmutableCollectors.toList());
                return iqFactory.createExtensionalDataNode(
                        atomFactory.getDataAtom(
                                ((NestedView)rDef).getParentRelation().getAtomPredicate(),
                                vars
                ));
            }
            throw new LevelUpException("The database relation definition is expected to be a nested view");
        }


        private UnaryOperatorNode generateFlattenNode(Variable variable, DatabaseRelationDefinition rDef, ImmutableList<? extends VariableOrGroundTerm> arguments) {
            if (rDef instanceof NestedView) {
                return iqFactory.createStrictFlattenNode(
                        variable,
                        0,
                        atomFactory.getFlattenNodeDataAtom(
                                ((NestedView)rDef).getNestedRelation().getAtomPredicate(),
                                arguments
                        ));
            }
            throw new LevelUpException("The database relation definition is expected to be a nested view");
        }
    }

    private Optional<DatabaseRelationDefinition> selectLeafRelation(IQTree tree) {
        ImmutableSet<DatabaseRelationDefinition> rDefs = retrieveAllRelations(Stream.of(), tree)
                .collect(ImmutableCollectors.toSet());
        return rDefs.stream()
                .filter(r -> !hasChildIn(r, rDefs) && r instanceof NestedView)
                .findFirst();
    }

    private Stream<DatabaseRelationDefinition> retrieveAllRelations(Stream<DatabaseRelationDefinition> rDefs, IQTree tree) {
        if (tree instanceof ExtensionalDataNode) {
            DatabaseRelationDefinition rDef = ((DatabaseRelationDefinition) ((ExtensionalDataNode) tree).getDataAtom().getPredicate().getRelationDefinition());
            return Stream.concat(
                    rDefs,
                    getAncestorRelations(rDef)
            );
        }
        if (tree instanceof LeafIQTree) {
            return Stream.of();
        }
        for (IQTree child : tree.getChildren()) {
            rDefs = retrieveAllRelations(rDefs, child);
        }
        return rDefs;
    }

    // includes the input relation
    private Stream<DatabaseRelationDefinition> getAncestorRelations(DatabaseRelationDefinition rDef) {
        if (rDef instanceof NestedView) {
            return Stream.concat(
                    Stream.of(rDef),
                    getAncestorRelations(((NestedView)rDef).getParentRelation()));
        }
        return Stream.of(rDef);
    }

    private boolean hasChildIn(DatabaseRelationDefinition rDef, ImmutableSet<DatabaseRelationDefinition> rDefs) {
        return rDefs.stream()
                .filter(r -> r instanceof NestedView)
                .map(r -> (NestedView)r)
                .map(NestedView::getParentRelation)
                .anyMatch(r -> r.equals(rDef));
    }

    private static class LevelUpException extends OntopInternalBugException {
        LevelUpException(String message) {
            super(message);
        }
    }
}
