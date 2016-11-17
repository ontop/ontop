package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.SelectQueryParser;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class SelectQueryParserTest {

    private OBDADataFactory FACTORY;
    private DBMetadata METADATA;
    private QuotedIDFactory MDFAC;


    @Before
    public void beforeEachTest(){
        FACTORY = OBDADataFactoryImpl.getInstance();
        METADATA = DBMetadataExtractor.createDummyMetadata();
        MDFAC = METADATA.getQuotedIDFactory();
    }

    @Test
    public void simpleJoin_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);
        final CQIE parse = parser.parse("SELECT A, B FROM P, Q"); //todo: fix this when the column reference A is ambiguous should throw an exception!!
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable b1 = FACTORY.getVariable("B1");

        Function atomP = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[] { null, null }),
                ImmutableList.of(a1, b1));

        assertTrue(parse.getBody().contains(atomP));

        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive
        Variable c2 = FACTORY.getVariable("C2");

        Function atomQ = FACTORY.getFunction(
                FACTORY.getPredicate("Q", new Predicate.COL_TYPE[] { null, null }),
                ImmutableList.of(a2, c2));

        assertTrue(parse.getBody().contains(atomQ));
    }




    @Test
    public void innerJoinOn_EQ_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);
        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A = Q.A ");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive


        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }


    @Test
    public void innerJoinOn_GTE_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);
        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A >= Q.A ");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive


        Function atomQ = FACTORY.getFunction(ExpressionOperation.GTE, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_GT_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A >Q.A");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive


        Function atomQ = FACTORY.getFunction(ExpressionOperation.GT, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_LT_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A < Q.A");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive


        Function atomQ = FACTORY.getFunction(ExpressionOperation.LT, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_LTE_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A <= Q.A");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive


        Function atomQ = FACTORY.getFunction(ExpressionOperation.LTE, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_NEQ_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A <> Q.A");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive


        Function atomQ = FACTORY.getFunction(ExpressionOperation.NEQ, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }

    @Test
    public void innerJoinOn_AND_Test() {
        DBMetadata metadata = createMetadata();

        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A <> Q.A AND P.A = 2 AND Q.A = 3");
        System.out.println(parse);

        assertEquals(0, parse.getHead().getTerms().size());
        assertEquals(4, parse.getReferencedVariables().size());

        assertEquals(5, parse.getBody().size());

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive
        ValueConstant v2 = FACTORY.getConstantLiteral("2", Predicate.COL_TYPE.LONG);
        ValueConstant v3 = FACTORY.getConstantLiteral("3", Predicate.COL_TYPE.LONG);

        Function atomQ = FACTORY.getFunction(ExpressionOperation.NEQ, ImmutableList.of(a1, a2));
        assertEquals(atomQ, parse.getBody().get(2));

        Function atomQ2 = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, v2));
        assertEquals(atomQ2, parse.getBody().get(3));

        Function atomQ3 = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a2, v3));
        assertEquals(atomQ3, parse.getBody().get(4));
    }

    @Test
    public void innerJoinOn_EQ_CONSTANT_INT_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A = 1");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        ValueConstant a2 = FACTORY. getConstantLiteral("1", Predicate.COL_TYPE.LONG); // variable are key sensitive


        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }

    @Test
    public void natural_join_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P NATURAL JOIN  Q");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 =FACTORY.getVariable("A2");


        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }


    @Test
    public void join_using_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P JOIN Q USING (A)");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 =FACTORY.getVariable("A2");


        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
    }

    @Test
    public void in_condition_Test() {
        final DBMetadata metadata = createMetadata();
        final SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, B FROM P where P.A IN ( 1, 2 )");
        System.out.println(parse);
        assertTrue( parse.getHead().getTerms().size()==0);

        final Variable a1 = FACTORY.getVariable("A1");
        final ValueConstant v1 = FACTORY. getConstantLiteral("1", Predicate.COL_TYPE.LONG); // variable are key sensitive
        final ValueConstant v2 = FACTORY. getConstantLiteral("2", Predicate.COL_TYPE.LONG);


        final Function atom = FACTORY.getFunction(ExpressionOperation.OR, ImmutableList.of (
                FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, v2)),
                FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, v1))));

        assertTrue( parse.getBody().contains(atom));
    }



    private DBMetadata createMetadata(){
        DBMetadata metadata = DBMetadataExtractor.createDummyMetadata();

        RelationID table1 = MDFAC.createRelationID(null, "P");
        QuotedID attx = MDFAC.createAttributeID("A");
        QuotedID atty = MDFAC.createAttributeID("B");

        DatabaseRelationDefinition relation1 = metadata.createDatabaseRelation(table1);
        relation1.addAttribute(attx, 0, "INT", false);
        relation1.addAttribute(atty, 0, "INT", false);

        RelationID table2 = MDFAC.createRelationID(null, "Q");
        QuotedID attu = MDFAC.createAttributeID("A");
        QuotedID attv = MDFAC.createAttributeID("C");

        DatabaseRelationDefinition relation2 = metadata.createDatabaseRelation(table2);
        relation2.addAttribute(attu, 0, "INT", false);
        relation2.addAttribute(attv, 0, "INT", false);
        return metadata;
    }




}
