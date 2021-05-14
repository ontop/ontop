package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;

import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Ignore;
import org.junit.Test;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;
import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.RIGHT;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

/**
 * TODO: explain
 */

public class LeftJoinOptimizationTest {

    private final static NamedRelationDefinition TABLE1;
    private final static NamedRelationDefinition TABLE1a;
    private final static NamedRelationDefinition TABLE2;
    private final static NamedRelationDefinition TABLE2a;
    private final static NamedRelationDefinition TABLE3;
    private final static NamedRelationDefinition TABLE4;
    private final static NamedRelationDefinition TABLE5;
    private final static NamedRelationDefinition TABLE6;
    private final static NamedRelationDefinition TABLE21;
    private final static NamedRelationDefinition TABLE22;
    private final static AtomPredicate ANS1_ARITY_2_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 2);
    private final static AtomPredicate ANS1_ARITY_3_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 3);
    private final static AtomPredicate ANS1_ARITY_4_PREDICATE = ATOM_FACTORY.getRDFAnswerPredicate( 4);

    private final static ImmutableList<Template.Component> URI_TEMPLATE_STR_1 = Template.of("http://example.org/ds1/", 0);

    private final static Variable X = TERM_FACTORY.getVariable("x");
    private final static Variable Y = TERM_FACTORY.getVariable("y");
    private final static Variable Z = TERM_FACTORY.getVariable("z");
    private final static DBConstant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());

    private final static Variable M = TERM_FACTORY.getVariable("m");
    private final static Variable M1 = TERM_FACTORY.getVariable("m1");
    private final static Variable M1F0 = TERM_FACTORY.getVariable("m1f0");
    private final static Variable M2 = TERM_FACTORY.getVariable("m2");
    private final static Variable MF1 = TERM_FACTORY.getVariable("mf1");
    private final static Variable N = TERM_FACTORY.getVariable("n");
    private final static Variable NF1 = TERM_FACTORY.getVariable("nf1");
    private final static Variable N1 = TERM_FACTORY.getVariable("n1");
    private final static Variable N1F0 = TERM_FACTORY.getVariable("n1f0");
    private final static Variable N1F1 = TERM_FACTORY.getVariable("n1f1");
    private final static Variable N2 = TERM_FACTORY.getVariable("n2");
    private final static Variable O = TERM_FACTORY.getVariable("o");
    private final static Variable OF0 = TERM_FACTORY.getVariable("of0");
    private final static Variable OF1 = TERM_FACTORY.getVariable("of1");
    private final static Variable O1 = TERM_FACTORY.getVariable("o1");
    private final static Variable O2 = TERM_FACTORY.getVariable("o2");
    private final static Variable O1F1 = TERM_FACTORY.getVariable("o1f1");
    private final static Variable F0 = TERM_FACTORY.getVariable("f0");

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        /*
         * Table 1: non-composite unique constraint and regular field
         */
        TABLE1 = builder.createDatabaseRelation( "TABLE1",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));

        /*
         * Table 2: non-composite unique constraint and regular field
         */
        TABLE2 = builder.createDatabaseRelation("TABLE2",
           "col1", integerDBType, false,
           "col2", integerDBType, false,
           "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(1));
        ForeignKeyConstraint.of("fk2-1", TABLE2.getAttribute(2), TABLE1.getAttribute(1));

        /*
         * Table 3: composite unique constraint over the first TWO columns
         */
        TABLE3 = builder.createDatabaseRelation("TABLE3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1), TABLE3.getAttribute(2));

        /*
         * Table 1a: non-composite unique constraint and regular field
         */
        TABLE1a = builder.createDatabaseRelation("TABLE1A",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE1a.getAttribute(1));

        /*
         * Table 2a: non-composite unique constraint and regular field
         */
        TABLE2a = builder.createDatabaseRelation("TABLE2A",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(TABLE2a.getAttribute(1));
        ForeignKeyConstraint.builder("composite-fk", TABLE2a, TABLE1a)
            .add(2, 1)
            .add(3, 2)
            .build();

        /*
         * Table 4: non-composite unique constraint and nullable fk
         */
        TABLE4 = builder.createDatabaseRelation("TABLE4",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE4.getAttribute(1));
        ForeignKeyConstraint.of("fk4-1", TABLE4.getAttribute(3), TABLE1.getAttribute(1));

        /*
         * Table 5: nullable unique constraint
         */
        TABLE5 = builder.createDatabaseRelation("TABLE5",
            "col1", integerDBType, true,
            "col2", integerDBType, false);
        UniqueConstraint.builder(TABLE5, "uc5")
                    .addDeterminant(1)
                    .build();

        /*
         * Table 6: PK + nullable column
         */
        TABLE6 = builder.createDatabaseRelation("TABLE6",
                "col1", integerDBType, false,
                "col2", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE6.getAttribute(1));

        TABLE21 = builder.createDatabaseRelation("table21",
                "col1", integerDBType, false,
                "col2", integerDBType, false,
                "col3", integerDBType, false,
                "col4", integerDBType, false,
                "col5", integerDBType, false,
                "col6", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE21.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE21)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();

        TABLE22 = builder.createDatabaseRelation("table22",
                "col1", integerDBType, false,
                "col2", integerDBType, true,
                "col3", integerDBType, true,
                "col4", integerDBType, true,
                "col5", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE22.getAttribute(1));
        FunctionalDependency.defaultBuilder(TABLE22)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();
    }

    @Test
    public void testSelfJoinElimination1()  {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 = createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 = createExtensionalDataNode(TABLE1,  ImmutableList.of(M, N1, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);

        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE1,  ImmutableList.of(M, N, O));
        expectedQueryBuilder.init(projectionAtom1, dataNode5);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfJoinElimination2()  {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(O));
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);

        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        expectedQueryBuilder.init(projectionAtom1, dataNode5);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testNoSelfLeftJoin3() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.init(projectionAtom, leftJoinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 1, N));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, N, 2, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        LeftJoinNode leftJoinNode1 = IQ_FACTORY.createLeftJoinNode();
        expectedQueryBuilder.init(projectionAtom, leftJoinNode1);

        expectedQueryBuilder.addChild(leftJoinNode1, dataNode1, LEFT);
        expectedQueryBuilder.addChild(leftJoinNode1, dataNode2, RIGHT);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfJoinWithCondition() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(O, TWO));

        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(OF1, TWO), OF1)));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        expectedQueryBuilder.addChild(newConstructionNode,
                createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, OF1)));

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfLeftJoinNonUnification1() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, TWO));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, NULL));
        expectedQueryBuilder.init(projectionAtom, constructionNode1);

        ExtensionalDataNode newDataNode =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 2, ONE));
        expectedQueryBuilder.addChild(constructionNode1, newDataNode);
        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfLeftJoinNonUnification1NotSimplifiedExpression() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, TWO));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(
                projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N,
                        TERM_FACTORY.getNullConstant()));
        expectedQueryBuilder.init(projectionAtom, constructionNode1);

        ExtensionalDataNode newDataNode =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M, 2, ONE));
        expectedQueryBuilder.addChild(constructionNode1, newDataNode);
        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test(expected = EmptyQueryException.class)
    public void testSelfLeftJoinNonUnificationEmptyResult() throws EmptyQueryException {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(N));
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, TWO));

        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, filterNode);
        queryBuilder.addChild(filterNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        System.out.println("\nBefore optimization: \n" +  query);

        optimize(query);
    }


    @Test
    public void testSelfLeftJoinIfElseNull1() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, TWO));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, TERM_FACTORY.getIfElseNull(
                        TERM_FACTORY.getStrictEquality(F1, TWO), NF0)));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        expectedQueryBuilder.addChild(newConstructionNode,
                createExtensionalDataNode(TABLE1, ImmutableList.of(M, NF0, F1)));

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfLeftJoinIfElseNull2() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, N));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = query.newBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N, TERM_FACTORY.getIfElseNull( 
                        TERM_FACTORY.getConjunction(
                                TERM_FACTORY.getStrictEquality(NF0, NF1),
                                TERM_FACTORY.getDBIsNotNull(NF1)),
                        NF0)));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        expectedQueryBuilder.addChild(newConstructionNode,
                createExtensionalDataNode(TABLE1, ImmutableList.of(M, NF0, NF1)));

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testNoSelfLeftJoin1() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(N1, N, TWO));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        optimizeAndCheck(query, query.createSnapshot());
    }

    @Test
    public void testNoSelfLeftJoin2() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, ONE));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(N1, M, N));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        optimizeAndCheck(query, query.createSnapshot());
    }

    @Test
    public void testLeftJoinElimination1()  {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1, O1));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.init(projectionAtom, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode newDataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1, 1, N1));
        expectedQueryBuilder.addChild(joinNode, newDataNode2);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testLeftJoinElimination2() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, M2, N1);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.init(projectionAtom, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2a, ImmutableList.of(M, M1, M2));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1a, ImmutableMap.of(0, M1, 1, M2, 2, N1));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.init(projectionAtom, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        expectedQueryBuilder.addChild(joinNode, dataNode2);

        optimizeAndCheck(queryBuilder.build(), expectedQueryBuilder.build());
    }

    @Test
    public void testLeftJoinElimination3() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, M2, N1);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2a, ImmutableList.of(M, M1, M2));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1a, ImmutableMap.of(0, M1, 1, M, 2, N1));

        queryBuilder.init(projectionAtom, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        expectedQueryBuilder.init(projectionAtom, leftJoinNode);
        expectedQueryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        expectedQueryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testLeftJoinElimination4() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.init(projectionAtom, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1,  2, N1));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.init(projectionAtom, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        expectedQueryBuilder.addChild(joinNode, dataNode2);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testLeftJoinElimination5() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, O1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, M1, O1));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                    SUBSTITUTION_FACTORY.getSubstitution(O1, TERM_FACTORY.getIfElseNull(
                            TERM_FACTORY.getStrictEquality(M1, M1F0),
                            O1F1)));
        expectedQueryBuilder.init(projectionAtom, constructionNode1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(constructionNode1, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode dataNode3 = createExtensionalDataNode(TABLE1, ImmutableList.of(M1, M1F0, O1F1));
        expectedQueryBuilder.addChild(joinNode, dataNode3);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testLeftJoinNonElimination1() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.init(projectionAtom, leftJoinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, M, 2, O));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, O, 1, N));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();
        optimizeAndCheck(query, query.createSnapshot());
    }

    @Test
    public void testLeftJoinEliminationWithFilterCondition2() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(N1));
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1, 2, N1));

        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.init(projectionAtom, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        expectedQueryBuilder.addChild(joinNode, dataNode2);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testLeftJoinEliminationWithFilterCondition4() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        ImmutableExpression expression = TERM_FACTORY.getStrictEquality(O1, TWO);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode(expression);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1, O1));

        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N1,
                        TERM_FACTORY.getIfElseNull(TERM_FACTORY.getStrictEquality(O1F1, TWO), N1F0)));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(newConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode newDataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1F0, O1F1));
        expectedQueryBuilder.addChild(joinNode, newDataNode2);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testLeftJoinEliminationWithImplicitFilterCondition() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1, TWO));

        queryBuilder.init(projectionAtom, constructionNode);
        queryBuilder.addChild(constructionNode, leftJoinNode);
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        ImmutableExpression expression = TERM_FACTORY.getStrictEquality(F1, TWO);
        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(N1,
                        TERM_FACTORY.getIfElseNull( expression, N1F0)));
        expectedQueryBuilder.init(projectionAtom, newConstructionNode);
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(newConstructionNode, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode newDataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1F0, F1));
        expectedQueryBuilder.addChild(joinNode, newDataNode2);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfLeftJoinWithJoinOnLeft1() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of());

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftJoinNode, joinNode, LEFT);
        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();

        expectedQueryBuilder.init(projectionAtom, joinNode);
        ExtensionalDataNode dataNode5 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O));
        expectedQueryBuilder.addChild(joinNode, dataNode5);
        expectedQueryBuilder.addChild(joinNode, dataNode3);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfLeftJoinWithJoinOnLeft2() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, O1));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, N1, O));
        ExtensionalDataNode dataNode3 =  IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, N1));

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        queryBuilder.addChild(leftJoinNode, joinNode, LEFT);
        queryBuilder.addChild(joinNode, dataNode1);
        queryBuilder.addChild(joinNode, dataNode3);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, M, N, O);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(O,
                        TERM_FACTORY.getIfElseNull(
                            TERM_FACTORY.getStrictEquality(N1, N),
                            OF1)));
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        expectedQueryBuilder.addChild(constructionNode1, joinNode);
        expectedQueryBuilder.addChild(joinNode, createExtensionalDataNode(TABLE1, ImmutableList.of(M, N, OF1)));
        expectedQueryBuilder.addChild(joinNode, dataNode3);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testSelfJoinNullableUniqueConstraint() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, M, N);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.init(projectionAtom, leftJoinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, M));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE5, ImmutableList.of(M, N));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, dataNode2, RIGHT);

        IntermediateQuery query = queryBuilder.build();

        optimizeAndCheck(query, query.createSnapshot());
    }


    @Test
    public void testLeftJoinEliminationUnnecessaryConstructionNode1() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_4_PREDICATE, M, M1, O, N1);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        queryBuilder.init(projectionAtom, constructionNode);
        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, M1, O));
        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M1, N1, O1));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(dataNode2.getVariables());

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, rightConstructionNode, RIGHT);
        queryBuilder.addChild(rightConstructionNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.init(projectionAtom, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        ExtensionalDataNode newDataNode2 =  IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1, 1, N1));
        expectedQueryBuilder.addChild(joinNode, newDataNode2);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Test
    public void testLeftJoinEliminationConstructionNode1() {

        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(X, generateURI1(M1)));
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(1, M1));

        ImmutableExpression o1IsNotNull = TERM_FACTORY.getDBIsNotNull(O1);
        FilterNode rightFilterNode = IQ_FACTORY.createFilterNode(o1IsNotNull);

        ImmutableFunctionalTerm uri1O1Term = generateURI1(O1);

        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, M1, 2, O1));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M1, Y),
                SUBSTITUTION_FACTORY.getSubstitution(Y, uri1O1Term));

        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);
        queryBuilder.addChild(leftJoinNode, rightConstructionNode, RIGHT);
        queryBuilder.addChild(rightConstructionNode, rightFilterNode);
        queryBuilder.addChild(rightFilterNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom1 = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_2_PREDICATE, X, Y);
        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom1.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M1),
                        Y, TERM_FACTORY.getRDFFunctionalTerm(
                                uri1O1Term.getTerm(0),
                                TERM_FACTORY.getIfElseNull(
                                        o1IsNotNull,
                                        uri1O1Term.getTerm(1)))));
        expectedQueryBuilder.init(projectionAtom1, constructionNode1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(constructionNode1, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);
        expectedQueryBuilder.addChild(joinNode, dataNode2);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Ignore("TODO: let the LJ optimizer consider equalities in the LJ condition for detecting constraint matching")
    @Test
    public void testLeftJoinEliminationConstructionNode2_1() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M),
                        Y, generateURI1(N)));
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  createExtensionalDataNode(TABLE2, ImmutableList.of(M, N, O));
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);

        ImmutableExpression o1IsNotNull = TERM_FACTORY.getDBIsNotNull(O1);
        FilterNode rightFilterNode = IQ_FACTORY.createFilterNode(o1IsNotNull);

        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(M, M, O1));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M, N, Z),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Z, generateURI1(O1),
                        N, M));

        queryBuilder.addChild(leftJoinNode, rightConstructionNode, RIGHT);
        queryBuilder.addChild(rightConstructionNode, rightFilterNode);
        queryBuilder.addChild(rightFilterNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();

        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();

        ImmutableExpression zCondition = TERM_FACTORY.getConjunction(
                TERM_FACTORY.getStrictEquality(M, N),
                TERM_FACTORY.getStrictEquality(M, MF1),
                o1IsNotNull);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M),
                        Y, generateURI1(N),
                        Z, TERM_FACTORY.getIfElseNull(
                                zCondition,
                                generateURI1(O1))));
        expectedQueryBuilder.init(projectionAtom, constructionNode1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(constructionNode1, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode newRightDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(M, MF1, O1));

        expectedQueryBuilder.addChild(joinNode, newRightDataNode);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }

    @Ignore("TODO: clean the expression")
    @Test
    public void testLeftJoinEliminationConstructionNode2_2() {
        IntermediateQueryBuilder queryBuilder = createQueryBuilder();
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_ARITY_3_PREDICATE, X, Y, Z);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M),
                        Y, generateURI1(N)));
        queryBuilder.init(projectionAtom, constructionNode);

        LeftJoinNode leftJoinNode = IQ_FACTORY.createLeftJoinNode();
        queryBuilder.addChild(constructionNode, leftJoinNode);
        ExtensionalDataNode dataNode1 =  IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, M, 1, N));
        queryBuilder.addChild(leftJoinNode, dataNode1, LEFT);

        ImmutableExpression o1IsNotNull = TERM_FACTORY.getDBIsNotNull(O1);
        FilterNode rightFilterNode = IQ_FACTORY.createFilterNode(o1IsNotNull);

        ImmutableFunctionalTerm uri1O1Term = generateURI1(O1);

        ExtensionalDataNode dataNode2 =  createExtensionalDataNode(TABLE1, ImmutableList.of(N, N, O1));
        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(M, N, Z),
                SUBSTITUTION_FACTORY.getSubstitution(
                        Z, uri1O1Term,
                        M, N));

        queryBuilder.addChild(leftJoinNode, rightConstructionNode, RIGHT);
        queryBuilder.addChild(rightConstructionNode, rightFilterNode);
        queryBuilder.addChild(rightFilterNode, dataNode2);

        IntermediateQuery query = queryBuilder.build();


        IntermediateQueryBuilder expectedQueryBuilder = createQueryBuilder();

        ImmutableExpression zCondition = TERM_FACTORY.getConjunction(
                TERM_FACTORY.getStrictEquality(F0, N),
                TERM_FACTORY.getDBIsNotNull(O1F1),
                TERM_FACTORY.getStrictEquality(M, N));

        ImmutableFunctionalTerm uri1O1F1Term = generateURI1(O1F1);

        ConstructionNode constructionNode1 = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(
                        X, generateURI1(M),
                        Y, generateURI1(N),
                        Z, TERM_FACTORY.getRDFFunctionalTerm(
                            TERM_FACTORY.getIfElseNull(
                                    zCondition,
                                    uri1O1F1Term.getTerm(0)),
                            TERM_FACTORY.getIfElseNull(
                                    zCondition,
                                    uri1O1F1Term.getTerm(1)))));
        expectedQueryBuilder.init(projectionAtom, constructionNode1);

        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode();
        expectedQueryBuilder.addChild(constructionNode1, joinNode);
        expectedQueryBuilder.addChild(joinNode, dataNode1);

        ExtensionalDataNode newRightDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(N, F0, O1F1));

        expectedQueryBuilder.addChild(joinNode, newRightDataNode);

        optimizeAndCheck(query, expectedQueryBuilder.build());
    }
    
    @Test
    public void testLeftJoinOrder1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, B));

        BinaryNonCommutativeIQTree leftJoinTree1 = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        BinaryNonCommutativeIQTree leftJoinTree2 = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftJoinTree1,
                dataNode3);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree2);

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode3,
                dataNode2);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newLeftJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testLeftJoinDenormalized1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, B));

        NaryIQTree joinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        ImmutableExpression expression = TERM_FACTORY.getDBIsNotNull(B);

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(expression),
                dataNode1,
                joinTree);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, dataNode2);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Ignore("Not clear how to reach that state")
    @Test
    public void testLeftJoinDenormalized2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, C));

        ImmutableExpression expression = TERM_FACTORY.getDBIsNotNull(C);

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(C, D),
                SUBSTITUTION_FACTORY.getSubstitution(D, TERM_FACTORY.getProvenanceSpecialConstant()));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(expression),
                dataNode1,
                IQ_FACTORY.createUnaryIQTree(rightConstructionNode, dataNode2));

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(D), C)));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(topConstructionNode, leftJoinTree)));

        ExtensionalDataNode newDataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 2, B));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newDataNode1);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode4, dataNode5));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, B, TWO)),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,2, CF0));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode4, dataNode5));

        ImmutableExpression cond = TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, BF0, TWO);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        projectionAtom.getVariables(),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                B, TERM_FACTORY.getIfElseNull(cond, BF0),
                                C, TERM_FACTORY.getIfElseNull(cond, CF0))),
                newJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer4() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, C, TWO)),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, CF0));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, CF0, 1, PROV));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode4, dataNode5));


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, CF0, TWO)),
                newJoinTree,
                dataNode6);

        ImmutableExpression cond = TERM_FACTORY.getDBIsNotNull(PROV);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        projectionAtom.getVariables(),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                C, TERM_FACTORY.getIfElseNull(cond, CF0))),
                newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer5() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE2, ImmutableMap.of(0, A, 1, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, C, TWO)),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, CF0));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, CF0));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode5));

        UnaryIQTree newRight = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(CF0, F1),
                        SUBSTITUTION_FACTORY.getSubstitution(F1, TERM_FACTORY.getProvenanceSpecialConstant())),
                dataNode6);


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, CF0, TWO)),
                newJoinTree,
                newRight);

        ImmutableExpression cond = TERM_FACTORY.getDBIsNotNull(F1);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        projectionAtom.getVariables(),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                C, TERM_FACTORY.getIfElseNull(cond, CF0))),
                newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer6() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, C, TWO)),
                dataNode1,
                rightJoin);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(B));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree));

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, CF0));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, CF0));

        NaryIQTree newJoinTree = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode1, dataNode5));

        UnaryIQTree newRight = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(CF0, F1),
                        SUBSTITUTION_FACTORY.getSubstitution(F1, TERM_FACTORY.getProvenanceSpecialConstant())),
                dataNode6);


        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, CF0, TWO)),
                newJoinTree,
                newRight);

        ImmutableExpression cond = TERM_FACTORY.getDBIsNotNull(F1);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(
                        projectionAtom.getVariables(),
                        SUBSTITUTION_FACTORY.getSubstitution(
                                C, TERM_FACTORY.getIfElseNull(cond, CF0))),
                newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer7() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A, 1, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer8() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, D));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A,1, B, 2, D));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(D));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 1, BF0, 2, D));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBIsNotNull(D)),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer9() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A,1, B, 2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(C));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 1, BF0, 2, C));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0, 1, PROV, 2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(PROV), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode,
                IQ_FACTORY.createUnaryIQTree(filterNode,
                        newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer10() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, C));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A,1, B, 2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 1, BF0, 2, C));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, BF0, 1, PROV, 2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(PROV), BF0)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransfer11() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A,1, ONE));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, ONE,2, C));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(B, ONE));

        IQTree rightJoin = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode2, dataNode3)));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, A, 1, F0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, ONE,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(F0, ONE)),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), ONE)));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testNonJoinTransfer6() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE4, ImmutableMap.of(0, A, 2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,2, C));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE6, ImmutableMap.of(0, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getDBNumericInequality(InequalityLabel.LT, C, TWO)),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testNonJoinTransfer7() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE5, ImmutableMap.of(0, A,1, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        optimizeAndCompare(initialIQ, initialIQ);
    }

    /**
     * TODO: remove the top distinct in the expected query
     */
    @Test
    public void testJoinTransferFD1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 2, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,1, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        // TODO: get rid of the distinct here
        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransferFD2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, C, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        ExtensionalDataNode newDataNode = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, C, 1, A, 2, B));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newDataNode);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * TODO: remove the top distinct in the expected query
     */
    @Test
    public void testJoinTransferFD3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 4, E));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B, 4, E));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree)));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 2, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,1, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode newConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        // TODO: get rid of the distinct here
        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(newConstructionNode, newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * TODO: remove the top distinct in the expected query
     */
    @Test
    public void testJoinTransferFD4() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        FilterNode filterNode = IQ_FACTORY.createFilterNode(TERM_FACTORY.getDBIsNotNull(A));

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, leftJoinTree)));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(0, D, 1, A, 2, BF0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,1, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode4, dataNode5);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        // TODO: get rid of the distinct here
        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode,
                        IQ_FACTORY.createUnaryIQTree(filterNode, newLeftJoinTree)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransferFD5() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, ONE));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, ONE,2, C));

        ConstructionNode rightConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, B, C),
                SUBSTITUTION_FACTORY.getSubstitution(B, ONE));

        IQTree rightJoin = IQ_FACTORY.createUnaryIQTree(
                rightConstructionNode,
                IQ_FACTORY.createNaryIQTree(
                        IQ_FACTORY.createInnerJoinNode(), ImmutableList.of(dataNode2, dataNode3)));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A, 2, F0));
        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, ONE,2, C));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(TERM_FACTORY.getStrictEquality(F0, ONE)),
                dataNode4, dataNode5);

        ConstructionNode topConstructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), ONE)));

        ConstructionNode subConstructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, C));

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(topConstructionNode,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(subConstructionNode, newLeftJoinTree)));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }


    /**
     * TODO: remove the top distinct in the expected query
     */
    @Test
    public void testJoinTransferFD6() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(5), ImmutableList.of(A, B, C, D, E));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));
        ExtensionalDataNode dataNode4 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, D,1, E));

        IQTree leftChild = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode1, dataNode4);

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                leftChild,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        ExtensionalDataNode dataNode5 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 2, BF0));
        ExtensionalDataNode dataNode6 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, BF0,1, C));

        IQTree newLeftChild = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(), dataNode5, dataNode4);

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                newLeftChild, dataNode6);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables(),
                SUBSTITUTION_FACTORY.getSubstitution(B, TERM_FACTORY.getIfElseNull(TERM_FACTORY.getDBIsNotNull(C), BF0)));

        // TODO: get rid of the distinct here
        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode,
                IQ_FACTORY.createUnaryIQTree(constructionNode, newLeftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }



    /**
     * Missing DISTINCT
     */
    @Test
    public void testNonJoinTransferFD1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(0, B,2, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testNonJoinTransferFD2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, C, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A,2, B));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));
        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, leftJoinTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * E is nullable
     */
    @Test
    public void testNonJoinTransferFD3() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, D, 1, A, 5, E));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(1, A,2, B, 5, E));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode,
                        IQ_FACTORY.createUnaryIQTree(constructionNode, leftJoinTree)));

        optimizeAndCompare(initialIQ, initialIQ);
    }

    /**
     * A is nullable
     */
    @Test
    public void testNonJoinTransferFD4() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(4), ImmutableList.of(A, B, C, D));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(0, D, 1, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE22, ImmutableMap.of(1, A,2, B));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Test
    public void testJoinTransferSameTerms1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A, 2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        BinaryNonCommutativeIQTree newLeftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1, dataNode3);

        UnaryIQTree newTree = IQ_FACTORY.createUnaryIQTree(distinctNode, newLeftJoinTree);

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom, newTree);

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    @Test
    public void testJoinTransferSameTerms2() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A, 2, B));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE3, ImmutableMap.of(1, A));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, dataNode1));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    /**
     * Nullable column (A)
     */
    @Test
    public void testNonJoinTransferSameTerms1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), ImmutableList.of(A, B, C));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, B, 2, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, A));
        ExtensionalDataNode dataNode3 = IQ_FACTORY.createExtensionalDataNode(TABLE21, ImmutableMap.of(0, B,1, C));

        NaryIQTree rightJoin = IQ_FACTORY.createNaryIQTree(
                IQ_FACTORY.createInnerJoinNode(),
                ImmutableList.of(dataNode2, dataNode3));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                rightJoin);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        optimizeAndCompare(initialIQ, initialIQ);
    }

    @Ignore("TODO: support (no new variable coming from the right)")
    @Test
    public void testLJSameTerms1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ATOM_FACTORY.getRDFAnswerPredicate(2), ImmutableList.of(A, B));

        ExtensionalDataNode dataNode1 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(1, B, 2, A));
        ExtensionalDataNode dataNode2 = IQ_FACTORY.createExtensionalDataNode(TABLE1, ImmutableMap.of(2, A));

        BinaryNonCommutativeIQTree leftJoinTree = IQ_FACTORY.createBinaryNonCommutativeIQTree(
                IQ_FACTORY.createLeftJoinNode(),
                dataNode1,
                dataNode2);

        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom, IQ_FACTORY.createUnaryIQTree(distinctNode, leftJoinTree));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(distinctNode, dataNode1));

        optimizeAndCompare(initialIQ, expectedIQ);
    }


        private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("Initial query: "+ initialIQ);
        System.out.println("Expected query: "+ expectedIQ);
        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ, EXECUTOR_REGISTRY);
        System.out.println("Optimized query: "+ optimizedIQ);
        assertEquals(expectedIQ, optimizedIQ);
    }

    private static void optimizeAndCheck(IntermediateQuery initialQuery, IntermediateQuery expectedQuery) {
        optimizeAndCompare(IQ_CONVERTER.convert(initialQuery), IQ_CONVERTER.convert(expectedQuery));
    }

    private IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        IQ initialIQ =  IQ_CONVERTER.convert(query);

        IQ optimizedIQ = JOIN_LIKE_OPTIMIZER.optimize(initialIQ, EXECUTOR_REGISTRY);
        if (optimizedIQ.getTree().isDeclaredAsEmpty())
            throw new EmptyQueryException();

        return IQ_CONVERTER.convert(optimizedIQ, EXECUTOR_REGISTRY);
    }

    private static ImmutableFunctionalTerm generateURI1(VariableOrGroundTerm argument) {
        return TERM_FACTORY.getIRIFunctionalTerm(URI_TEMPLATE_STR_1, ImmutableList.of(argument));
    }
}
