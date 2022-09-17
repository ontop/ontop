package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;

/**
 * Prevents a bug observed with MySQL 5.7.15, where the provenance special constant was misused
 * (causing a MINUS encoded with a LJ and filter is null not to work)
 */
@Singleton
public class ReplaceProvenanceConstantByNonGroundTermNormalizer extends DefaultRecursiveIQTreeVisitingTransformer
        implements DialectExtraNormalizer {

    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
    protected ReplaceProvenanceConstantByNonGroundTermNormalizer(IntermediateQueryFactory iqFactory,
                                                                 TermFactory termFactory,
                                                                 SubstitutionFactory substitutionFactory) {
        super(iqFactory);
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return transform(tree);
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree newLeftChild = leftChild.acceptTransformer(this);
        IQTree newRightChild = rightChild.acceptTransformer(this);

        return furtherTransformLJ(rootNode, leftChild, rightChild)
                .orElseGet(() -> newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                        ? tree
                        : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild));
    }

    private Optional<IQTree> furtherTransformLJ(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        if (rightChild.getRootNode() instanceof ConstructionNode) {
         ConstructionNode rightConstructionNode = (ConstructionNode) rightChild.getRootNode();
            IQTree rightGrandChild = ((UnaryIQTree) rightChild).getChild();
            Optional<Variable> grandChildVariable = rightGrandChild.getVariables().stream()
                    .findAny();

            DBConstant provenanceConstant = termFactory.getProvenanceSpecialConstant();

            return grandChildVariable
                    .map(v -> termFactory.getIfThenElse(termFactory.getDBIsNotNull(v),
                            termFactory.getDBStringConstant("placeholder1"),
                            termFactory.getDBStringConstant("placeholder2")))
                    .map(t -> rightConstructionNode.getSubstitution().transform(v -> v.equals(provenanceConstant) ? t : v))
                    .map(s -> iqFactory.createConstructionNode(rightConstructionNode.getVariables(), s))
                    .map(c -> iqFactory.createUnaryIQTree(c, rightGrandChild))
                    .map(r -> iqFactory.createBinaryNonCommutativeIQTree(rootNode, leftChild, r));
        }
        return Optional.empty();
    }
}
