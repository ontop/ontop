package it.unibz.inf.ontop.iq;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.QueryTransformationException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class NoNullValuesEnforcerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoNullValuesEnforcerTest.class);

    private final static AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static AtomPredicate ANS3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
    private final static AtomPredicate ANS4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(4);
    private final static Variable A = TERM_FACTORY.getVariable("a");
    private final static Variable B = TERM_FACTORY.getVariable("b");
    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");
    private final static Variable W = TERM_FACTORY.getVariable("w");

    private final static ImmutableExpression EQ_X_Y = TERM_FACTORY.getStrictEquality(X, Y);
    private final static ImmutableExpression EQ_Y_Z = TERM_FACTORY.getStrictEquality(Y, Z);
    private final static ImmutableExpression NOT_NULL_Z = TERM_FACTORY.getDBIsNotNull(Z);
    private final static ImmutableExpression NOT_NULL_X = TERM_FACTORY.getDBIsNotNull(X);
    private final static ImmutableExpression NOT_NULL_W = TERM_FACTORY.getDBIsNotNull(W);
    private final static ImmutableExpression NOT_NULL_X_AND_NOT_NULL_W = TERM_FACTORY.getConjunction(NOT_NULL_X, NOT_NULL_W);

    private final static ExtensionalDataNode DATA_NODE_1;
    private final static ExtensionalDataNode DATA_NODE_2;
    private final static ExtensionalDataNode DATA_NODE_3;
    private static final NamedRelationDefinition TABLE2;

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        TABLE2 = builder.createDatabaseRelation("TABLE2",
            "A", integerDBType, true,
            "B", integerDBType, true);

        DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, X, 1, Z));
        DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, Y, 1, W));
        DATA_NODE_3 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, Y, 1, Z));
    }

    @Test
    public void testConstructionNodeAsRoot() throws QueryTransformationException {

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Y);
        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(DATA_NODE_1, DATA_NODE_2))));
        LOGGER.info("Initial IQ:\n" + query);

        IQTree transformedTree = NO_NULL_VALUE_ENFORCER.transform(query.getTree());
        LOGGER.info("Transformed IQ:\n" + transformedTree);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_Z);
        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode,
                                IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(DATA_NODE_1, DATA_NODE_2)))))
                .normalizeForOptimization();
        LOGGER.info("Expected IQ:\n" + expectedQuery);

        assertEquals(expectedQuery.getTree(), transformedTree);
    }

    @Test
    public void testConstructionNodeAsRoot_noNullableVariable() throws QueryTransformationException {

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Y);
        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(DATA_NODE_1, DATA_NODE_3)))).normalizeForOptimization();
        LOGGER.info("Initial IQ:\n" + query);

        IQTree transformedTree = NO_NULL_VALUE_ENFORCER.transform(query.getTree());
        LOGGER.info("Transformed IQ:\n" + transformedTree);

        assertEquals(query.getTree(), transformedTree);
    }

    @Test
    public void testNonConstructionNodeAsRoot() throws QueryTransformationException {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_Y_Z);
        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(DATA_NODE_1, DATA_NODE_3)));
        LOGGER.info("Initial IQ:\n" + query);

        IQTree transformedTree = NO_NULL_VALUE_ENFORCER.transform(query.getTree());
        LOGGER.info("Transformed IQ:\n" + transformedTree);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_X);
        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(DATA_NODE_1, DATA_NODE_3))))
                .normalizeForOptimization();
        LOGGER.info("Expected IQ:\n" + expectedQuery);

        assertEquals(expectedQuery.getTree(), transformedTree);
    }

    @Test
    public void test2NonNullableVariables() throws QueryTransformationException {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS4_PREDICATE, W, X, Y, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_Y_Z);
        IQ query = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(DATA_NODE_1, DATA_NODE_2)));
        LOGGER.info("Initial IQ:\n" + query);

        IQTree transformedTree = NO_NULL_VALUE_ENFORCER.transform(query.getTree());
        LOGGER.info("Transformed IQ:\n" + transformedTree);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_X_AND_NOT_NULL_W);
        IQ expectedQuery = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        IQ_FACTORY.createNaryIQTree(innerJoinNode, ImmutableList.of(DATA_NODE_1, DATA_NODE_2))))
            .normalizeForOptimization();
        LOGGER.info("Expected IQ:\n" + expectedQuery);

        assertEquals(expectedQuery.getTree(), transformedTree);
    }

    @Test
    public void testCoalesce() throws QueryTransformationException {
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1),
                ImmutableList.of(X));

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getIRIFunctionalTerm(Template.of("http://example.org/", 0),
                                ImmutableList.of(TERM_FACTORY.getDBCoalesce(A, B)))));


        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createFilterNode(TERM_FACTORY.getDisjunction(
                            TERM_FACTORY.getDBIsNotNull(A),
                            TERM_FACTORY.getDBIsNotNull(B))),
                dataNode));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        IQTree transformedTree = NO_NULL_VALUE_ENFORCER.transform(initialIQ.getTree())
                .normalizeForOptimization(expectedIQ.getVariableGenerator());
        LOGGER.info("Expected IQ:\n" + expectedIQ);
        LOGGER.info("Transformed IQ:\n" + transformedTree);
        assertEquals(expectedIQ.getTree(), transformedTree);
    }

    @Test
    public void testNullIf() throws QueryTransformationException {
        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A));

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(1),
                ImmutableList.of(X));

        DBConstant one = TERM_FACTORY.getDBIntegerConstant(1);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getIRIFunctionalTerm(Template.of("http://example.org/", 0),
                                ImmutableList.of(TERM_FACTORY.getImmutableFunctionalTerm(
                                        TERM_FACTORY.getDBFunctionSymbolFactory()
                                                .getRegularDBFunctionSymbol("NULLIF", 2),
                                        A, one
                                )))));


        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(constructionNode, dataNode));

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getIRIFunctionalTerm(Template.of("http://example.org/", 0), ImmutableList.of(A))));

        UnaryIQTree expectedTree = IQ_FACTORY.createUnaryIQTree(newConstructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createFilterNode(
                                TERM_FACTORY.getConjunction(
                                        TERM_FACTORY.getDBIsNotNull(A),
                                        TERM_FACTORY.getDBNot(TERM_FACTORY.getDBNonStrictDefaultEquality(A, one)))),
                        dataNode));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, expectedTree);

        IQTree transformedTree = NO_NULL_VALUE_ENFORCER.transform(initialIQ.getTree())
                .normalizeForOptimization(initialIQ.getVariableGenerator());
        LOGGER.info("Expected IQ:\n" + expectedIQ);
        LOGGER.info("Transformed IQ:\n" + transformedTree);
        assertEquals(expectedIQ.getTree(), transformedTree);
    }

}
