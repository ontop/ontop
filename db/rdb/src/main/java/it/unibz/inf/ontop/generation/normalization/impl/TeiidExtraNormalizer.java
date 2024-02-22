package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

/**
 * Introduces a sub-query to work around an issue with Teiid not enforcing the <code>LIMIT</code> clause in expressions
 * of the form <code>SELECT DISTINCT ... ORDER BY ... LIMIT n</code>.
 * <p>
 * Concretely, this normalizer matches IQ patterns of the form:
 * <pre>
 * SLICE limit=N
 *    DISTINCT
 *       CONSTRUCT [vars_C] [subst_C]
 *          ORDER BY [spec on vars_O]
 *            ...body...
 * </pre>
 * with the restriction that vars_O are contained in vars_C, and transforms them into the following form:
 * <pre>
 * SLICE limit=N
 *    CONSTRUCT [vars_C] [subst_C]
 *       ORDER BY [spec on vars_O]
 *          DISTINCT
 *             CONSTRUCT [vars_C, __teiid_dummy__] [subst_C + __teiid_dummy__/...some dummy constant...]
 *                ...body...
 * </pre>
 * </p>
 * <p>
 * While the two IQ trees are equivalent, the latter leads to generating an SQL native query that includes a nested
 * subquery (the innermost CONSTRUCT with the dummy var) where the LIMIT is correctly handled by Teiid.
 * </p>
 * <p>Note that the problem in Teiid (up to latest version v16 included) seems to arise only if all the components of
 * the matched pattern do appear: in particular, removing one or more of DISTINCT, ORDER BY, or the projection
 * (CONSTRUCT) to a subset of columns appears to prevent the issue from being triggered (i.e., it's the interplay of all
 * of them + LIMIT/SLICE to cause the issue).
 * </p>
 */
public final class TeiidExtraNormalizer implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;

    private final SubstitutionFactory substitutionFactory;

    private final TermFactory termFactory;

    @Inject
    public TeiidExtraNormalizer(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return new Transformer(variableGenerator).transform(tree);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final VariableGenerator variableGenerator;

        public Transformer(VariableGenerator variableGenerator) {
            super(TeiidExtraNormalizer.this.iqFactory);
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {

            // Process the child node and then consider the resulting child tree
            IQTree sliceChild = child.acceptTransformer(this);

            // Process SLICE( DISTINCT( CONSTRUCT( ORDER BY( ... ) ) ) ) with order by vars subset construct vars
            if (sliceChild.getRootNode() instanceof DistinctNode) {

                DistinctNode distinctNode = (DistinctNode) sliceChild.getRootNode();
                IQTree distinctChild = ((UnaryIQTree) sliceChild).getChild(); // got DISTINCT node & child tree

                if (distinctChild.getRootNode() instanceof ConstructionNode) {

                    ConstructionNode constructNode = (ConstructionNode) distinctChild.getRootNode();
                    IQTree constructChild = ((UnaryIQTree) distinctChild).getChild(); // got CONSTRUCT node & child tree

                    if (constructChild.getRootNode() instanceof OrderByNode) {

                        OrderByNode orderByNode = (OrderByNode) constructChild.getRootNode();
                        IQTree orderByChild = ((UnaryIQTree) constructChild).getChild(); // got ORDER BY node & child tree

                        if (constructNode.getLocalVariables().containsAll(orderByNode.getLocalVariables())) {
                            return introduceSubQuery(sliceNode, distinctNode, constructNode, orderByNode, orderByChild);
                        }
                    }
                }
            }

            // If pattern is not matched, proceed as per super implementation
            return (!sliceChild.equals(child) || !sliceNode.equals(tree.getRootNode()))
                    ? iqFactory.createUnaryIQTree(sliceNode, sliceChild)
                    : tree;
        }

        private IQTree introduceSubQuery(SliceNode sliceNode, DistinctNode distinctNode, ConstructionNode constructNode,
                                         OrderByNode orderByNode, IQTree body) {

            // Create the 'inner' CONSTRUCT for the sub-query to introduce, projecting all variables of the original
            // construct node + an additional constant-valued dummy variable that will be projected away later. This
            // appears to be enough for Teiid to correctly implement the LIMIT clause generated by the SLICE node
            Variable dummyVar = variableGenerator.generateNewVariable("__teiid_dummy__");
            ImmutableSet<Variable> innerConstructVars = Stream
                    .concat(constructNode.getVariables().stream(), Stream.of(dummyVar))
                    .collect(ImmutableSet.toImmutableSet());
            Substitution<DBConstant> innerConstructSubstitution = substitutionFactory.getSubstitution(dummyVar,
                    termFactory.getDBConstant("", termFactory.getTypeFactory().getDBTypeFactory().getDBStringType()));
            ConstructionNode innerConstructNode = iqFactory.createConstructionNode(innerConstructVars, innerConstructSubstitution);

            // Create tree SLICE( CONSTRUCT( ORDER BY( DISTINCT( 'inner' CONSTRUCT( ... ) ) ) ) )
            return iqFactory.createUnaryIQTree(sliceNode,
                    iqFactory.createUnaryIQTree(constructNode,
                            iqFactory.createUnaryIQTree(orderByNode,
                                    iqFactory.createUnaryIQTree(distinctNode,
                                            iqFactory.createUnaryIQTree(innerConstructNode, body)))));
        }

    }

}
