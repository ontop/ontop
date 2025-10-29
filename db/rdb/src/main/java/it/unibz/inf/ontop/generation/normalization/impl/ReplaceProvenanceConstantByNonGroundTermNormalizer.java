package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.UnaryIQTreeTools.UnaryIQTreeDecomposition;

/**
 * Prevents a bug observed with MySQL 5.7.15, where the provenance special constant was misused
 * (causing a MINUS encoded with a LJ and filter is null not to work)
 */
@Singleton
public class ReplaceProvenanceConstantByNonGroundTermNormalizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    @Inject
    protected ReplaceProvenanceConstantByNonGroundTermNormalizer(CoreSingletons coreSingletons) {
        super(new Transformer(coreSingletons)::transform);
    }

    private static class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        private final TermFactory termFactory;
        private final IQTreeTools iqTreeTools;

        Transformer(CoreSingletons coreSingletons) {
            super(coreSingletons.getIQFactory());
            this.termFactory = coreSingletons.getTermFactory();
            this.iqTreeTools = coreSingletons.getIQTreeTools();
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = transform(leftChild);
            IQTree newRightChild = transform(rightChild);

            var construction = UnaryIQTreeDecomposition.of(rightChild, ConstructionNode.class);
            if (!construction.isPresent())
                return withTransformedChildren(tree, newLeftChild, newRightChild);

            IQTree rightGrandChild = construction.getChild();
            Optional<Variable> grandChildVariable = rightGrandChild.getVariables().stream()
                    .findAny();

            if (grandChildVariable.isEmpty())
                return withTransformedChildren(tree, newLeftChild, newRightChild);

            DBConstant provenanceConstant = termFactory.getProvenanceSpecialConstant();
            ConstructionNode constructionNode = iqTreeTools.replaceSubstitution(
                    construction.getNode(),
                    s -> s.transform(t -> t.equals(provenanceConstant)
                            ? getIfThenElse(grandChildVariable.get())
                            : t));

            return iqFactory.createBinaryNonCommutativeIQTree(
                    rootNode, leftChild,
                    iqFactory.createUnaryIQTree(constructionNode, rightGrandChild));
        }

        private ImmutableFunctionalTerm getIfThenElse(Variable grandChildVariable) {
            return termFactory.getIfThenElse(
                    termFactory.getDBIsNotNull(grandChildVariable),
                    termFactory.getDBStringConstant("placeholder1"),
                    termFactory.getDBStringConstant("placeholder2"));
        }
    }
}
