package it.unibz.inf.ontop.iq;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.QueryTransformationException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static junit.framework.TestCase.assertEquals;

public class NoNullValuesEnforcerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoNullValuesEnforcerTest.class);


    private final static RelationPredicate TABLE2_PREDICATE;
    private final static AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(1);
    private final static AtomPredicate ANS3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(3);
    private final static AtomPredicate ANS4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate(4);
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

    static {
        DBTermType integerDBType = DEFAULT_DUMMY_DB_METADATA.getDBTypeFactory().getDBLargeIntegerType();

        RelationDefinition table2Def = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation("TABLE2",
            "A", integerDBType, true,
            "B", integerDBType, true);
        TABLE2_PREDICATE = table2Def.getAtomPredicate();

        DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
        DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, W));
        DATA_NODE_3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));
    }

    @Test
    public void testConstructionNodeAsRoot() throws QueryTransformationException {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Y);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_2);
        IQ query = IQ_CONVERTER.convert(queryBuilder.build());
        LOGGER.info("Initial IQ:\n" + query);

        IQ transformedQuery = NO_NULL_VALUE_ENFORCER.transform(query);
        LOGGER.info("Transformed IQ:\n" + transformedQuery);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_Z);
        IntermediateQueryBuilder queryBuilder2 = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder2.init(projectionAtom, filterNode);
        queryBuilder2.addChild(filterNode, constructionNode);
        queryBuilder2.addChild(constructionNode, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_2);
        IQ expectedQuery = IQ_CONVERTER.convert(queryBuilder2.build()).normalizeForOptimization();
        LOGGER.info("Expected IQ:\n" + expectedQuery);

        assertEquals(expectedQuery, transformedQuery);
    }

    @Test
    public void testConstructionNodeAsRoot_noNullableVariable() throws QueryTransformationException {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Y);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_3);
        IQ query = IQ_CONVERTER.convert(queryBuilder.build()).normalizeForOptimization();
        LOGGER.info("Initial IQ:\n" + query);

        IQ transformedQuery = NO_NULL_VALUE_ENFORCER.transform(query);
        LOGGER.info("Transformed IQ:\n" + transformedQuery);

        assertEquals(query, transformedQuery);
    }

    @Test
    public void testNonConstructionNodeAsRoot() throws QueryTransformationException {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_Y_Z);
        queryBuilder.init(projectionAtom, innerJoinNode);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_3);
        IQ query = IQ_CONVERTER.convert(queryBuilder.build());
        LOGGER.info("Initial IQ:\n" + query);

        IQ transformedQuery = NO_NULL_VALUE_ENFORCER.transform(query);
        LOGGER.info("Transformed IQ:\n" + transformedQuery);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_X);
        IntermediateQueryBuilder queryBuilder2 = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder2.init(projectionAtom, filterNode);
        queryBuilder2.addChild(filterNode, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_3);
        IQ expectedQuery = IQ_CONVERTER.convert(queryBuilder2.build())
                .normalizeForOptimization();
        LOGGER.info("Expected IQ:\n" + expectedQuery);

        assertEquals(expectedQuery, transformedQuery);
    }

    @Test
    public void test2NonNullableVariables() throws QueryTransformationException {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS4_PREDICATE, W, X, Y, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_Y_Z);
        queryBuilder.init(projectionAtom, innerJoinNode);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_2);
        IQ query = IQ_CONVERTER.convert(queryBuilder.build());
        LOGGER.info("Initial IQ:\n" + query);

        IQ transformedQuery = NO_NULL_VALUE_ENFORCER.transform(query);
        LOGGER.info("Transformed IQ:\n" + transformedQuery);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_X_AND_NOT_NULL_W);
        IntermediateQueryBuilder queryBuilder2 = IQ_FACTORY.createIQBuilder(EXECUTOR_REGISTRY);
        queryBuilder2.init(projectionAtom, filterNode);
        queryBuilder2.addChild(filterNode, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_2);
        IQ expectedQuery = IQ_CONVERTER.convert(queryBuilder2.build()).normalizeForOptimization();
        LOGGER.info("Expected IQ:\n" + expectedQuery);

        assertEquals(expectedQuery, transformedQuery);
    }

}
