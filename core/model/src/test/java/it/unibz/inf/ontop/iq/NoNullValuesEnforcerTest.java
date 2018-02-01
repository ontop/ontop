package it.unibz.inf.ontop.iq;


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.QueryTransformationException;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.FilterNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.*;
import static it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation.*;
import static org.junit.Assert.assertTrue;

public class NoNullValuesEnforcerTest {
    private final static AtomPredicate TABLE2_PREDICATE = ATOM_FACTORY.getAtomPredicate("P", 2);
    private final static AtomPredicate ANS1_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 1);
    private final static AtomPredicate ANS3_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans3", 3);
    private final static AtomPredicate ANS4_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans4", 4);
    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");
    private final static Variable W = TERM_FACTORY.getVariable("w");

    private final static ImmutableExpression EQ_X_Y = TERM_FACTORY.getImmutableExpression(EQ, X, Y);
    private final static ImmutableExpression EQ_Y_Z = TERM_FACTORY.getImmutableExpression(EQ, Y, Z);
    private final static ImmutableExpression NOT_NULL_Z = TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, Z);
    private final static ImmutableExpression NOT_NULL_X = TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, X);
    private final static ImmutableExpression NOT_NULL_W = TERM_FACTORY.getImmutableExpression(IS_NOT_NULL, W);
    private final static ImmutableExpression NOT_NULL_X_AND_NOT_NULL_W = TERM_FACTORY.getImmutableExpression(AND, NOT_NULL_X, NOT_NULL_W);

    private final static ExtensionalDataNode DATA_NODE_1 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, X, Z));
    private final static ExtensionalDataNode DATA_NODE_2 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, W));
    private final static ExtensionalDataNode DATA_NODE_3 = IQ_FACTORY.createExtensionalDataNode(ATOM_FACTORY.getDataAtom(TABLE2_PREDICATE, Y, Z));

    private final DBMetadata dbMetadata;

    public NoNullValuesEnforcerTest() {
        dbMetadata = createMetadata();
    }

    @Test
    public void testConstructionNodeAsRoot() throws QueryTransformationException {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Y);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_2);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQuery transformedQuery = NO_NULL_VALUE_ENFORCER.transform(query);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_Z);
        IntermediateQueryBuilder queryBuilder2 = query.newBuilder();
        queryBuilder2.init(projectionAtom, constructionNode);
        queryBuilder2.addChild(constructionNode, filterNode);
        queryBuilder2.addChild(filterNode, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_2);
        IntermediateQuery expectedQuery = queryBuilder2.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(transformedQuery, expectedQuery));
    }

    @Test
    public void testConstructionNodeAsRoot_noNullableVariable() throws QueryTransformationException {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(Z));
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_PREDICATE, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_X_Y);
        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, innerJoinNode);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_3);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQuery transformedQuery = NO_NULL_VALUE_ENFORCER.transform(query);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(transformedQuery, query));
    }

    @Test
    public void testNonConstructionNodeAsRoot() throws QueryTransformationException {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS3_PREDICATE, X, Y, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_Y_Z);
        queryBuilder.init(projectionAtom, innerJoinNode);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_3);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQuery transformedQuery = NO_NULL_VALUE_ENFORCER.transform(query);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_X);
        IntermediateQueryBuilder queryBuilder2 = query.newBuilder();
        queryBuilder2.init(projectionAtom, filterNode);
        queryBuilder2.addChild(filterNode, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_3);
        IntermediateQuery expectedQuery = queryBuilder2.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(transformedQuery, expectedQuery));
    }

    @Test
    public void test2NonNullableVariables() throws QueryTransformationException {

        IntermediateQueryBuilder queryBuilder = IQ_FACTORY.createIQBuilder(dbMetadata, EXECUTOR_REGISTRY);
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS4_PREDICATE, W, X, Y, Z);
        InnerJoinNode innerJoinNode = IQ_FACTORY.createInnerJoinNode(EQ_Y_Z);
        queryBuilder.init(projectionAtom, innerJoinNode);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder.addChild(innerJoinNode, DATA_NODE_2);
        IntermediateQuery query = queryBuilder.build();

        IntermediateQuery transformedQuery = NO_NULL_VALUE_ENFORCER.transform(query);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(NOT_NULL_X_AND_NOT_NULL_W);
        IntermediateQueryBuilder queryBuilder2 = query.newBuilder();
        queryBuilder2.init(projectionAtom, filterNode);
        queryBuilder2.addChild(filterNode, innerJoinNode);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_1);
        queryBuilder2.addChild(innerJoinNode, DATA_NODE_2);
        IntermediateQuery expectedQuery = queryBuilder2.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(transformedQuery, expectedQuery));
    }

    private DBMetadata createMetadata() {
        BasicDBMetadata metadata = createDummyMetadata();
        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        DatabaseRelationDefinition relation1 =
                metadata.createDatabaseRelation(idfac.createRelationID(null, "P"));
        relation1.addAttribute(idfac.createAttributeID("A"), 0, "INT", true);
        relation1.addAttribute(idfac.createAttributeID("B"), 0, "INT", true);

        return metadata;
    }

}
