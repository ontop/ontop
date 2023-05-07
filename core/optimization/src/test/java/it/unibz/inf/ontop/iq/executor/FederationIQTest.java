package it.unibz.inf.ontop.iq.executor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static it.unibz.inf.ontop.OptimizationTestingTools.IQ_FACTORY;
import static org.junit.Assert.assertEquals;

public class FederationIQTest {

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

    private static void optimizeAndCompare(IQ initialIQ, IQ expectedIQ) {
        System.out.println("Initial query: "+ initialIQ);
        IQ optimizedIQ = FEDERATION_OPTIMIZER.optimize(initialIQ);
        System.out.println("Optimized query: "+ optimizedIQ);

        System.out.println("Expected query: "+ expectedIQ);
        assertEquals(expectedIQ, optimizedIQ);
    }
}
