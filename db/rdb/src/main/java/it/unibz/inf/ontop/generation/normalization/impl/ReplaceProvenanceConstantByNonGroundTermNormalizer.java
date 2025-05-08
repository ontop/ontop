package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

/**
 * Prevents a bug observed with MySQL 5.7.15, where the provenance special constant was misused
 * (causing a MINUS encoded with a LJ and filter is null not to work)
 */
@Singleton
public class ReplaceProvenanceConstantByNonGroundTermNormalizer extends DefaultRecursiveIQTreeVisitingTransformer
        implements DialectExtraNormalizer {

    private final TermFactory termFactory;

    @Inject
    protected ReplaceProvenanceConstantByNonGroundTermNormalizer(IntermediateQueryFactory iqFactory,
                                                                 TermFactory termFactory) {
        super(iqFactory);
        this.termFactory = termFactory;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree);
    }

    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = transformChild(leftChild);
        IQTree newRightChild = transformChild(rightChild);

        return furtherTransformLJ(rootNode, leftChild, rightChild)
                .orElseGet(() -> newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                        ? tree
                        : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild));
    }

    private Optional<IQTree> furtherTransformLJ(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        var construction = UnaryIQTreeDecomposition.of(rightChild, ConstructionNode.class);
        if (construction.isPresent()) {
            IQTree rightGrandChild = construction.getChild();
            Optional<Variable> grandChildVariable = rightGrandChild.getVariables().stream()
                    .findAny();

            DBConstant provenanceConstant = termFactory.getProvenanceSpecialConstant();

            ConstructionNode rightConstructionNode = construction.getNode();
            return grandChildVariable.isPresent()
                    ? Optional.of(iqFactory.createBinaryNonCommutativeIQTree(
                            rootNode,
                            leftChild,
                            iqFactory.createUnaryIQTree(
                                    iqFactory.createConstructionNode(
                                            rightConstructionNode.getVariables(),
                                            rightConstructionNode.getSubstitution()
                                                    .transform(t -> t.equals(provenanceConstant)
                                                            ? termFactory.getIfThenElse(
                                                                    termFactory.getDBIsNotNull(grandChildVariable.get()),
                                                                    termFactory.getDBStringConstant("placeholder1"),
                                                                    termFactory.getDBStringConstant("placeholder2"))
                                                            : t)),
                                    rightGrandChild)))
                    : Optional.empty();
        }
        return Optional.empty();
    }
}
