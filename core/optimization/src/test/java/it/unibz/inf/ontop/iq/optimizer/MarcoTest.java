package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import org.junit.Test;

import java.util.Optional;

import static it.unibz.inf.ontop.NoDependencyTestDBMetadata.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.IQ_FACTORY;
import static junit.framework.TestCase.assertEquals;

public class MarcoTest {

    private final static NamedRelationDefinition TABLE1;
    private final static NamedRelationDefinition TABLE2;
    private final static NamedRelationDefinition TABLE3;
    private final static NamedRelationDefinition TABLE4;
    private final static NamedRelationDefinition TABLE5;


    private final static Variable A = TERM_FACTORY.getVariable("a");
    private final static Variable B = TERM_FACTORY.getVariable("b");
    private final static Variable C = TERM_FACTORY.getVariable("c");
    private final static Variable D = TERM_FACTORY.getVariable("d");
    private final static Variable N1 = TERM_FACTORY.getVariable("n1");
    private final static Variable N2 = TERM_FACTORY.getVariable("n2");
    private final static Variable N4 = TERM_FACTORY.getVariable("n4");
    private final static Variable N5 = TERM_FACTORY.getVariable("n5");
    private final static Variable O1 = TERM_FACTORY.getVariable("o1");
    private final static Variable O2 = TERM_FACTORY.getVariable("o2");
    private final static Variable O3 = TERM_FACTORY.getVariable("o3");
    private final static Variable O4 = TERM_FACTORY.getVariable("o4");
    private final static Variable O5 = TERM_FACTORY.getVariable("o5");
    private final static Variable X1 = TERM_FACTORY.getVariable("x1");
    private final static Variable X2 = TERM_FACTORY.getVariable("x2");

    private final static DBConstant ONE = TERM_FACTORY.getDBConstant("1", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static DBConstant TWO = TERM_FACTORY.getDBConstant("2", TYPE_FACTORY.getDBTypeFactory().getDBLargeIntegerType());
    private final static ImmutableExpression X1_EQ_X2 = TERM_FACTORY.getStrictEquality(X1, X2);
    private final static ImmutableExpression C_EQ_ONE = TERM_FACTORY.getStrictEquality(C, ONE);
    private final static ImmutableExpression X1_EQ_X2_AND_C_EQ_ONE = TERM_FACTORY.getConjunction(
            X1_EQ_X2, C_EQ_ONE
    );



    static {


        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();
        DBTermType arrayDBType = builder.getDBTypeFactory().getDBArrayType();

        TABLE1 = builder.createDatabaseRelation( "TABLE1",
                "pk", integerDBType, false,
                "col2", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE1.getAttribute(1));

        TABLE2 = builder.createDatabaseRelation( "TABLE2",
                "pk", integerDBType, false,
                "arr", arrayDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE2.getAttribute(1));

        TABLE3 = builder.createDatabaseRelation( "TABLE3",
                "pk", integerDBType, false,
                "arr", arrayDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE3.getAttribute(1));

        TABLE4 = builder.createDatabaseRelation( "TABLE4",
                "pk", integerDBType, false,
                "arr", arrayDBType, true,
                "col3", integerDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE4.getAttribute(1));

        TABLE5 = builder.createDatabaseRelation( "TABLE5",
                "pk", integerDBType, false,
                "arr1", arrayDBType, true,
                "arr2", arrayDBType, true,
                "arr3", arrayDBType, true,
                "arr4", arrayDBType, true);
        UniqueConstraint.primaryKeyOf(TABLE5.getAttribute(1));

    }


    @Test
    public void testLiftFlatten1() {

        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(
                ATOM_FACTORY.getRDFAnswerPredicate(3), X1, O, B);

        ConstructionNode rootNode = IQ_FACTORY.createConstructionNode(projectionAtom.getVariables());
        InnerJoinNode joinNode = IQ_FACTORY.createInnerJoinNode(X1_EQ_X2);
        ExtensionalDataNode leftDataNode = createExtensionalDataNode(TABLE1, ImmutableList.of(X1, B));
        FlattenNode flattenNode = IQ_FACTORY.createFlattenNode(O, N, Optional.empty(), JSON_TYPE);
        ExtensionalDataNode rightDataNode = createExtensionalDataNode(TABLE2, ImmutableList.of(X2, N));

        IQ initialIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createNaryIQTree(
                                joinNode,
                                ImmutableList.of(
                                        leftDataNode,
                                        IQ_FACTORY.createUnaryIQTree(
                                                flattenNode,
                                                rightDataNode
                                        )))));

        IQ expectedIQ = IQ_FACTORY.createIQ(projectionAtom,
                IQ_FACTORY.createUnaryIQTree(
                        rootNode,
                        IQ_FACTORY.createUnaryIQTree(
                                flattenNode,
                                IQ_FACTORY.createNaryIQTree(
                                        joinNode,
                                        ImmutableList.of(
                                                leftDataNode,
                                                rightDataNode
                                        )))));

        optimizeAndCompare(initialIQ, expectedIQ);
    }

    private static void optimizeAndCompare(IQ query, IQ expectedQuery) {
        System.out.println("\nBefore optimization: \n" +  query);
        System.out.println("\nExpected: \n" +  expectedQuery);

        IQ optimizedIQ = MARCO_OPTIMIZER.optimize(query);
        System.out.println("\nAfter optimization: \n" +  optimizedIQ);

        assertEquals(expectedQuery, optimizedIQ);
    }

}
