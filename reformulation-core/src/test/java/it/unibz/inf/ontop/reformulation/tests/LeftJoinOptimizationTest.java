package it.unibz.inf.ontop.reformulation.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Injector;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionImpl;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCoreConfiguration;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.pivotalrepr.impl.*;
import it.unibz.inf.ontop.pivotalrepr.impl.tree.DefaultIntermediateQueryBuilder;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.LeftJoinOptimizationProposalImpl;
import it.unibz.inf.ontop.sql.*;
import org.junit.Test;

import java.sql.Types;
import java.util.Optional;

import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition.RIGHT;
import static junit.framework.TestCase.assertTrue;

/**
 * TODO: explain
 */

public class LeftJoinOptimizationTest {

    private final static AtomPredicate TABLE1_PREDICATE = new AtomPredicateImpl("TABLE1", 3);
    private final static AtomPredicate TABLE1a_PREDICATE = new AtomPredicateImpl("TABLE1A", 4);
    private final static AtomPredicate TABLE2_PREDICATE = new AtomPredicateImpl("TABLE2", 3);
    private final static AtomPredicate TABLE2a_PREDICATE = new AtomPredicateImpl("TABLE2A", 3);
    private final static AtomPredicate TABLE3_PREDICATE = new AtomPredicateImpl("TABLE3", 3);
    private final static AtomPredicate ANS1_ARITY_2_PREDICATE = new AtomPredicateImpl("ans1", 2);
    private final static AtomPredicate ANS1_ARITY_3_PREDICATE = new AtomPredicateImpl("ans1", 3);
    private final static AtomPredicate ANS1_ARITY_4_PREDICATE = new AtomPredicateImpl("ans1", 4);
    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final static Variable X = DATA_FACTORY.getVariable("X");
    private final static Variable Y = DATA_FACTORY.getVariable("Y");
    private final static Variable Z = DATA_FACTORY.getVariable("z");
    private final static Constant ONE = DATA_FACTORY.getConstantLiteral("1");
    private final static Constant TWO = DATA_FACTORY.getConstantLiteral("2");

    private final static Variable M = DATA_FACTORY.getVariable("m");
    private final static Variable M1 = DATA_FACTORY.getVariable("m1");
    private final static Variable M2 = DATA_FACTORY.getVariable("m2");
    private final static Variable N = DATA_FACTORY.getVariable("n");
    private final static Variable N1 = DATA_FACTORY.getVariable("n1");
    private final static Variable N2 = DATA_FACTORY.getVariable("n2");
    private final static Variable O = DATA_FACTORY.getVariable("o");
    private final static Variable O1 = DATA_FACTORY.getVariable("o1");
    private final static Variable O2 = DATA_FACTORY.getVariable("o2");

    private final static ImmutableExpression EXPRESSION1 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, M, N);
    private final static ImmutableExpression EXPRESSION2 = DATA_FACTORY.getImmutableExpression(
            ExpressionOperation.EQ, N, M);
    
    private static final Injector INJECTOR = QuestCoreConfiguration.defaultBuilder().build().getInjector();

    private final MetadataForQueryOptimization metadata;

    public LeftJoinOptimizationTest() {
        metadata = initMetadata();
    }

    private static MetadataForQueryOptimization initMetadata() {
        ImmutableMultimap.Builder<AtomPredicate, ImmutableList<Integer>> uniqueKeyBuilder = ImmutableMultimap.builder();

        /**
         * Table 1: non-composite unique constraint and regular field
         */
        uniqueKeyBuilder.put(TABLE1_PREDICATE, ImmutableList.of(1));
        uniqueKeyBuilder.put(TABLE1a_PREDICATE, ImmutableList.of(1));

        /**
         * Table 2: non-composite unique constraint and regular field
         */
        uniqueKeyBuilder.put(TABLE2_PREDICATE, ImmutableList.of(1));
        uniqueKeyBuilder.put(TABLE2a_PREDICATE, ImmutableList.of(1));
        /**
         * Table 3: composite unique constraint over the first TWO columns
         */
        uniqueKeyBuilder.put(TABLE3_PREDICATE, ImmutableList.of(1, 2));

        RDBMetadata dbMetadata = RDBMetadataExtractionTools.createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                TABLE1_PREDICATE.getName()));
        Attribute table1Col1 = table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table1Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, true);
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table1Col1));

        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                TABLE2_PREDICATE.getName()));
        Attribute table2Col1 = table2Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute table2Col2 = table2Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table2Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table2Col1));
        table2Def.addForeignKeyConstraint(ForeignKeyConstraint.of("fk2-1", table2Col2, table1Col1));

        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                TABLE3_PREDICATE.getName()));
        Attribute table3Col1 = table3Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute table3Col2 = table3Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table3Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table3Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table3Col1, table3Col2));


        DatabaseRelationDefinition table1aDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                TABLE1a_PREDICATE.getName()));
        Attribute table1aCol1 = table1aDef.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute table1aCol2 = table1aDef.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        table1aDef.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table1aDef.addAttribute(idFactory.createAttributeID("col4"), Types.INTEGER, null, false);
        table1aDef.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table1aCol1));

        DatabaseRelationDefinition table2aDef = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,
                TABLE2a_PREDICATE.getName()));
        Attribute table2aCol1 = table2aDef.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, null, false);
        Attribute table2aCol2 = table2aDef.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, null, false);
        Attribute table2aCol3 = table2aDef.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, null, false);
        table2aDef.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table2aCol1));
        ForeignKeyConstraint.Builder fkBuilder = ForeignKeyConstraint.builder(table2aDef, table1aDef);
        fkBuilder.add(table2aCol2, table1aCol1);
        fkBuilder.add(table2aCol3, table1aCol2);
        table2aDef.addForeignKeyConstraint(fkBuilder.build("composite-fk"));


        return new MetadataForQueryOptimizationImpl(dbMetadata, uniqueKeyBuilder.build(), new UriTemplateMatcher());
    }

    /**
     *  TODO: explain
     */
    @Test
    public void testSelfJoinElimination1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(
                //Optional.of(DATA_FACTORY.getImmutableExpression(
                //ExpressionOperation.EQ, M, M)));
                Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        ExtensionalDataNode dataNode5 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O));
        expectedQueryBuilder.addChild(constructionNode1, dataNode5);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testSelfJoinElimination2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(
                //Optional.of(DATA_FACTORY.getImmutableExpression(
                //ExpressionOperation.EQ, M, M)));
                Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M1, N, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);
        LeftJoinNode leftJoinNode1 = new LeftJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(constructionNode1, leftJoinNode1);

        expectedQueryBuilder.addChild(leftJoinNode1, dataNode1, LEFT);
        expectedQueryBuilder.addChild(leftJoinNode1, dataNode2, RIGHT);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testSelfJoinWithCondition() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(
                Optional.of(DATA_FACTORY.getImmutableExpression(
                ExpressionOperation.EQ, O, TWO)));

        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, O1));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        System.out.println("\n Expected query: \n" +  expectedQuery);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testSelfLeftJoinNonUnification1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, ONE));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, TWO));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom.getVariables(),
                new ImmutableSubstitutionImpl<>(ImmutableMap.of(N, OBDAVocabulary.NULL)),Optional.empty());
        expectedQueryBuilder.init(projectionAtom, constructionNode1);

        expectedQueryBuilder.addChild(constructionNode1, dataNode1);

        IntermediateQuery expectedQuery = expectedQueryBuilder.build();

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testSelfLeftJoinNonUnificationEmptyResult() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);

        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        FilterNode filterNode = new FilterNodeImpl(DATA_FACTORY.getImmutableExpression(ExpressionOperation.IS_NOT_NULL, N));
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, ONE));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, TWO));

        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, filterNode);
        queryBuilder.addChild(filterNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        try {
            IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode)).getResultingQuery();
            System.out.println("\n After optimization: \n" +  optimizedQuery);
            System.out.println("\n Expected query: \n" +  "empty query");
            assertTrue(false);
        } catch (EmptyQueryException e) {
            System.out.println("\n After optimization: \n" +  "empty query");
            System.out.println("\n Expected query: \n" +  "empty query");
            assertTrue(true);
        }
    }


    @Test
    public void testSelfLeftJoinShouldNotUnify() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, TWO));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testSelfLeftJoinShouldNotUnify2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, O));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N, N));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();
        System.out.println("\n Expected query: \n" +  expectedQuery);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testNoSelfLeftJoin1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, ONE));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, N1, N, TWO));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testNoSelfLeftJoin2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M, N1, ONE));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, N1, M, M));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery expectedQuery = query.createSnapshot();

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);

        System.out.println("\n Expected query: \n" +  expectedQuery);

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, expectedQuery));
    }

    @Test
    public void testLeftJoinElimination1() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, M1, O));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M1, N1, O1));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(constructionNode1, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        expectedQueryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }


    @Test
    public void testLeftJoinElimination2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, M2, N1);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2a_PREDICATE, M, M1, M2));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1a_PREDICATE, M1, M2, N1, O1));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        expectedQueryBuilder.init(projectionAtom, constructionNode);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(constructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        expectedQueryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testLeftJoinElimination3() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, M2, N1);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2a_PREDICATE, M, M1, M2));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1a_PREDICATE, M1, M, N1, O1));

        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, leftJoinNode);
        expectedQueryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        expectedQueryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testLeftJoinElimination4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.empty());
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, M1, O));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M1, O1, N1));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom1 = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode1 = new ConstructionNodeImpl(projectionAtom1.getVariables());
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode = new InnerJoinNodeImpl(Optional.empty());
        expectedQueryBuilder.addChild(constructionNode1, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        expectedQueryBuilder.addChild(joinNode, dataNode2);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        System.out.println("\n Expected query: \n" +  query1);
        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testLeftJoinEliminationWithFilterCondition2() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.of(DATA_FACTORY.getImmutableExpression(ExpressionOperation.IS_NOT_NULL, N1)));
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, M1, O));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M1, O1, N1));

        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, leftJoinNode);
        expectedQueryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        expectedQueryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }

    @Test
    public void testLeftJoinEliminationWithFilterCondition4() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        DistinctVariableOnlyDataAtom projectionAtom = DATA_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = new ConstructionNodeImpl(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = new LeftJoinNodeImpl(Optional.of(
                        DATA_FACTORY.getImmutableExpression(ExpressionOperation.EQ, O1, TWO)));
        ExtensionalDataNode dataNode1 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE2_PREDICATE, M, M1, O));
        ExtensionalDataNode dataNode2 =  new ExtensionalDataNodeImpl(DATA_FACTORY.getDataAtom(TABLE1_PREDICATE, M1, N1, O1));

        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        IntermediateQuery optimizedQuery = query.applyProposal(new LeftJoinOptimizationProposalImpl(leftJoinNode))
                .getResultingQuery();

        System.out.println("\n After optimization: \n" +  optimizedQuery);


        IntermediateQueryBuilder expectedQueryBuilder = new DefaultIntermediateQueryBuilder(metadata, INJECTOR);
        expectedQueryBuilder.init(projectionAtom, constructionNode);
        expectedQueryBuilder.addChild(constructionNode, leftJoinNode);
        expectedQueryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        expectedQueryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query1 = expectedQueryBuilder.build();

        assertTrue(IQSyntacticEquivalenceChecker.areEquivalent(optimizedQuery, query1));
    }


}
