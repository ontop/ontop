package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmptyNodeRemovalTest {

    private static final AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(2);
    private static final Variable X = TERM_FACTORY.getVariable("x");
    private static final Variable Y = TERM_FACTORY.getVariable("y");
    private static final Variable A = TERM_FACTORY.getVariable("a");
    private static final Variable B = TERM_FACTORY.getVariable("b");
    private static final Variable B1 = TERM_FACTORY.getVariable("b1");
    private static final Variable C = TERM_FACTORY.getVariable("c");
    private static final DistinctVariableOnlyDataAtom PROJECTION_ATOM = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
            ANS1_PREDICATE, ImmutableList.of(X, Y));
    private static final ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);
    private static final ExtensionalDataNode DATA_NODE_1 = createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B));
    private static final ExtensionalDataNode DATA_NODE_2 = createExtensionalDataNode(TABLE2_AR1, ImmutableList.of(A));
    private static final EmptyNode DB_NODE_1 = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, C));

    /**
     * TODO: Put the UNION as the root instead of the construction node (when this will become legal)
     */
    @Test
    public void testUnionRemoval1NoTopSubstitution() {
        ImmutableSubstitution<ImmutableTerm> topBindings = SUBSTITUTION_FACTORY.getSubstitution();
        ImmutableSubstitution<ImmutableTerm> leftBindings = SUBSTITUTION_FACTORY.getSubstitution(
                X, generateURI1(A, false), Y, generateURI1(B, false));
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(PROJECTION_ATOM.getVariables());
        IQ query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                leftBindings);

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, DATA_NODE_1));

        optimizeAndCompare(query, expectedIQ);
    }

    @Test
    public void testUnionRemoval2() {
        ImmutableSubstitution<ImmutableTerm> topBindings = SUBSTITUTION_FACTORY.getSubstitution(
                X, generateURI1(A, false));
        ImmutableSubstitution<ImmutableTerm> leftBindings = SUBSTITUTION_FACTORY.getSubstitution(
                Y, generateURI1(B, false));
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(Y, A));
        IQ query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        //ImmutableSubstitution<ImmutableTerm> expectedTopBindings = topBindings.union(leftBindings)
        //        .orElseThrow(() -> new IllegalStateException("Wrong bindings (union cannot be computed)"));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(A, false),
                        Y, generateURI1(B, false)));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode, DATA_NODE_1));

        optimizeAndCompare(query, expectedIQ);
    }

    @Test
    public void testUnionRemoval3() {
        ImmutableSubstitution<ImmutableTerm> topBindings = SUBSTITUTION_FACTORY.getSubstitution(
                X, generateURI1(A, false), Y, generateURI1(B, false));
        ImmutableSubstitution<ImmutableTerm> leftBindings = SUBSTITUTION_FACTORY.getSubstitution();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B));
        IQ query = generateQueryWithUnion(topBindings, leftBindings, emptyNode);

        ImmutableSet<Variable> projectedVariables = PROJECTION_ATOM.getVariables();
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectedVariables, topBindings);

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode, DATA_NODE_1));

        optimizeAndCompare(query, expectedIQ);
    }

    @Test
    public void testUnionNoNullPropagation() {
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, false)));

        ImmutableSet<Variable> subQueryProjectedVariables = ImmutableSet.of(A, B);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(subQueryProjectedVariables);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(subQueryProjectedVariables);

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, DATA_NODE_2, emptyNode)))));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(subQueryProjectedVariables,
                SUBSTITUTION_FACTORY.getSubstitution(B, NULL));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, DATA_NODE_2)))));

        optimizeAndCompare(query, expectedIQ);
    }




    private static IQ generateQueryWithUnion(ImmutableSubstitution<ImmutableTerm> topBindings,
                                                            ImmutableSubstitution<ImmutableTerm> leftBindings,
                                                            EmptyNode emptyNode) {
        ImmutableSet<Variable> subQueryProjectedVariables = emptyNode.getVariables();

        ImmutableSet<Variable> projectedVariables = PROJECTION_ATOM.getVariables();
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectedVariables, topBindings);
        UnionNode unionNode = IQ_FACTORY.createUnionNode(subQueryProjectedVariables);

        return IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(unionNode, ImmutableList.of(
                                leftBindings.isEmpty()
                                        ? DATA_NODE_1
                                        : IQ_FACTORY.createUnaryIQTree(IQ_FACTORY.createConstructionNode(subQueryProjectedVariables, leftBindings),
                                                DATA_NODE_1),
                                emptyNode
                        ))));
    }

    @Test
    public void testLJ1() {
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, true)));

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, DATA_NODE_2, emptyNode)));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, TERM_FACTORY.getNullConstant()));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        DATA_NODE_2));

        optimizeAndCompare(query, expectedIQ);
    }

    @Test
    public void testLJ2() {

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, false)));
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, emptyNode, DATA_NODE_1)));

        optimizeUnsatisfiableQuery(query);
    }

    @Test
    public void testLJ3() {

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, false)));

        LeftJoinNode topLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode rightLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B, C));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(topLeftJoinNode,
                                DATA_NODE_2,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(rightLeftJoinNode, DATA_NODE_1, emptyNode))));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(topLeftJoinNode, DATA_NODE_2, DATA_NODE_1)));

        optimizeAndCompare(query, expectedIQ);
    }

    @Test
    public void testLJ4() {

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, true)));

        LeftJoinNode topLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode rightLeftJoinNode = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(topLeftJoinNode,
                                DATA_NODE_2,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(rightLeftJoinNode, emptyNode, DATA_NODE_1))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, TERM_FACTORY.getNullConstant()));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode, DATA_NODE_2));

        optimizeAndCompare(query, expectedIQ);
    }

    @Test
    public void testJoin1() {
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, false)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, C));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(DATA_NODE_1, emptyNode))));

        optimizeUnsatisfiableQuery(query);
    }

    @Test
    public void testJoin2() {
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, false)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        ConstructionNode constructionNode2 = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A));
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, C));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createUnaryIQTree(constructionNode2, emptyNode)))));

        optimizeUnsatisfiableQuery(query);
    }

    @Test
    public void testJoin3() {

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, false)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of());

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(DATA_NODE_1, emptyNode))));

        optimizeUnsatisfiableQuery(query);
    }

    private static void optimizeUnsatisfiableQuery(IQ query) {
        System.out.println("\n Unsatisfiable query: \n" +  query);
        IQ newQuery = query.normalizeForOptimization();
        System.out.println("Optimized query: " + newQuery);
        assertTrue(newQuery.getTree().isDeclaredAsEmpty());
    }

    @Test
    public void testJoinLJ1() {
        IQ query = generateJoinLJInitialQuery(
                Optional.of(TERM_FACTORY.getStrictEquality(B, C)), B, false);

        optimizeUnsatisfiableQuery(query);
    }

    @Test
    public void testJoinLJ2() {
        IQ query = generateJoinLJInitialQuery(Optional.of(TERM_FACTORY.getDBIsNotNull(C)), B, false);

        optimizeUnsatisfiableQuery(query);
    }

    @Test
    public void testJoinLJ3() {

        IQ query = generateJoinLJInitialQuery(Optional.of(TERM_FACTORY.getDBIsNull(C)), B, false);

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, false)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(DATA_NODE_1, DATA_NODE_2))));

        optimizeAndCompare(query, expectedIQ);
    }

    @Test
    public void testJoinLJ4() {

        IQ query = generateJoinLJInitialQuery(Optional.empty(), C, true);

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(
                PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, TERM_FACTORY.getNullConstant()));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                IQ_FACTORY.createExtensionalDataNode(TABLE1_AR2, ImmutableMap.of(0, A)),
                                DATA_NODE_2))));

        optimizeAndCompare(query, expectedIQ);
    }


    private static IQ generateJoinLJInitialQuery(Optional<ImmutableExpression> joiningCondition,
                                                                Variable variableForBuildingY,
                                                                boolean isVariableForBuildingYNullable) {

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false),
                        Y, generateURI1(variableForBuildingY, isVariableForBuildingYNullable)));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(joiningCondition);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();

        return IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createNaryIQTree(joinNode, ImmutableList.of(
                                DATA_NODE_1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, DATA_NODE_2, DB_NODE_1)))));
    }

    @Test
    public void testFilter1() {

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false),
                        Y, generateURI1(B, false)));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(C));
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B, C));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, DATA_NODE_2, emptyNode))));

        optimizeUnsatisfiableQuery(query);
    }

    @Test
    public void testFilter2() {
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false),
                        Y, generateURI1(B, true)));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNull(C));
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B, C));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(leftJoinNode, DATA_NODE_2, emptyNode))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false),
                        Y, TERM_FACTORY.getNullConstant()));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode, DATA_NODE_2));

        optimizeAndCompare(query, expectedIQ);
    }


    @Test
    public void testComplexTreeWithJoinCondition() {
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false),
                        Y, generateURI1(B, true)));

        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
        InnerJoinNode join = IQ_FACTORY.createInnerJoinNode(TERM_FACTORY.getDBIsNotNull(C));
        LeftJoinNode lj2 = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, C));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(lj1,
                                DATA_NODE_2,
                                IQ_FACTORY.createNaryIQTree(join, ImmutableList.of(
                                        DATA_NODE_1,
                                        IQ_FACTORY.createBinaryNonCommutativeIQTree(lj2, DATA_NODE_2, emptyNode))))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, TERM_FACTORY.getNullConstant()));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode, DATA_NODE_2));

        optimizeAndCompare(query, expectedIQ);
    }

    /**
     * Follows the SQL semantics
     */
    @Test
    public void testLJRemovalDueToNull1()  {
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, true)));

        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(B, B1));
        LeftJoinNode lj2 = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(lj1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(lj2, DATA_NODE_2, emptyNode),
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(A, B1)))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, TERM_FACTORY.getNullConstant()));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode, DATA_NODE_2));

        optimizeAndCompare(query, expectedIQ);
    }

    /**
     * Follows the SQL semantics. Equivalent to testLJRemovalDueToNull1.
     */
    @Test
    public void testLJRemovalDueToNull2() {
        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, generateURI1(B, true)));

        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
        LeftJoinNode lj2 = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(lj1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(lj2, DATA_NODE_2, emptyNode),
                                DATA_NODE_1)));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(A, false), Y, TERM_FACTORY.getNullConstant()));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode, DATA_NODE_2));

        optimizeAndCompare(query, expectedIQ);
    }

    @Test
    public void testLJRemovalDueToNull3() {

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(C, true), Y, generateURI1(B, true)));

        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(B, B1));
        LeftJoinNode lj2 = IQ_FACTORY.createLeftJoinNode();
        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(A, B));

        IQ query = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(rootNode,
                        IQ_FACTORY.createBinaryNonCommutativeIQTree(lj1,
                                IQ_FACTORY.createBinaryNonCommutativeIQTree(lj2, DATA_NODE_2, emptyNode),
                                createExtensionalDataNode(TABLE1_AR2, ImmutableList.of(B1, C)))));

        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getNullConstant(), Y, TERM_FACTORY.getNullConstant()));

        IQ expectedIQ = IQ_FACTORY.createIQ(PROJECTION_ATOM,
                IQ_FACTORY.createUnaryIQTree(newRootNode, IQ_FACTORY.createExtensionalDataNode(TABLE2_AR1, ImmutableMap.of())));

        optimizeAndCompare(query, expectedIQ);
    }

//    @Test
//    public void testGroup1() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
//
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, generateURI1(B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A, B));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
//        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, OBDAVocabulary.NULL)),
//                Optional.empty());
//        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
//
//        GroupNode newGroupNode = new GroupNodeImpl(ImmutableList.of(A));
//        expectedQueryBuilder.addChild(newRootNode, newGroupNode);
//        expectedQueryBuilder.addChild(newGroupNode, DATA_NODE_2);
//
//        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
//    }
//
//    @Test
//    public void testGroup2() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
//
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, TERM_FACTORY.getImmutableExpression(AVG, B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
//        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, OBDAVocabulary.NULL)),
//                Optional.empty());
//        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
//
//        GroupNode newGroupNode = new GroupNodeImpl(ImmutableList.of(A));
//        expectedQueryBuilder.addChild(newRootNode, newGroupNode);
//        expectedQueryBuilder.addChild(newGroupNode, DATA_NODE_2);
//
//        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
//    }
//
//    /**
//     * Not sure about grouping by "null".
//     */
//    @Test
//    public void testGroup3() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
//
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
//                        X, TERM_FACTORY.getImmutableExpression(AVG, A),
//                        Y, generateURI1(B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(B));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder(DB_METADATA);
//        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(
//                        X, TERM_FACTORY.getImmutableExpression(AVG, A),
//                        Y, OBDAVocabulary.NULL)),
//                Optional.empty());
//        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
//
//        expectedQueryBuilder.addChild(newRootNode, DATA_NODE_2);
//
//        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
//    }


    private static void optimizeAndCompare(IQ query, IQ expectedQuery) {

        System.out.println("\n Original query: \n" +  query);
        System.out.println("\n Expected query: \n" +  expectedQuery);

        IQ optimizedQuery = query.normalizeForOptimization();

        System.out.println("\n Optimized query: \n" +  optimizedQuery);

        assertEquals(expectedQuery, optimizedQuery);
    }

//    @Test
//    public void testGroupIsNotNullBinding() throws EmptyQueryException {
//        IntermediateQueryBuilder queryBuilder = createQueryBuilder(DB_METADATA);
//
//        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, TERM_FACTORY.getDBIsNotNull(B))),
//                Optional.empty());
//        queryBuilder.init(PROJECTION_ATOM, rootNode);
//
//        GroupNode groupNode = new GroupNodeImpl(ImmutableList.of(A));
//        queryBuilder.addChild(rootNode, groupNode);
//
//        LeftJoinNode lj1 = IQ_FACTORY.createLeftJoinNode();
//        queryBuilder.addChild(groupNode, lj1);
//        queryBuilder.addChild(lj1, DATA_NODE_2, LEFT);
//        EmptyNode emptyNode = IQ_FACTORY.createEmptyNode(ImmutableSet.of(B));
//        queryBuilder.addChild(lj1, emptyNode, RIGHT);
//
//        /**
//         * Expected query
//         */
//        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(METADATA);
//        ConstructionNode newRootNode = IQ_FACTORY.createConstructionNode(PROJECTION_ATOM.getVariables(),
//                SUBSTITUTION_FACTORY.getSubstitution(ImmutableMap.of(X, generateURI1(A),
//                        Y, TERM_FACTORY.getDBIsNotNull(OBDAVocabulary.NULL))),
//                Optional.empty());
//        expectedQueryBuilder.init(PROJECTION_ATOM, newRootNode);
//
//        GroupNode newGroupNode = new GroupNodeImpl(ImmutableList.of(A));
//        expectedQueryBuilder.addChild(newRootNode, newGroupNode);
//        expectedQueryBuilder.addChild(newGroupNode, DATA_NODE_2);
//
//        optimizeAndCompare(queryBuilder.build(), expectedQueryBuilder.build(), emptyNode);
//    }


    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument,
                                                        boolean isNullable) {
        if (isNullable) {
            ImmutableExpression condition = TERM_FACTORY.getDBIsNotNull(argument);
            return TERM_FACTORY.getRDFFunctionalTerm(
                    TERM_FACTORY.getIfElseNull(condition, argument),
                    TERM_FACTORY.getIfElseNull(condition, TERM_FACTORY.getRDFTermTypeConstant(
                            TYPE_FACTORY.getIRITermType())));
        }
        else
            return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }

}
