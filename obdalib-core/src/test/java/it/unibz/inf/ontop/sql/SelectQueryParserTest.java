package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.SelectQueryParser;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedQueryException;
import org.junit.Before;
import org.junit.Ignore;
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



    @Test // TODO: does relationAliasesConsistent check is wrong?
    public void innerJoinOn_Same_Table_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);
        final CQIE parse = parser.parse("SELECT p1.A, p2.B FROM P p1 INNER JOIN  P p2 on p1.A = p2.A ");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );

        Variable a1 = FACTORY.getVariable("A1");
        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive


        Function atomQ = FACTORY.getFunction(ExpressionOperation.EQ, ImmutableList.of(a1, a2));

        assertTrue( parse.getBody().contains(atomQ));
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

    @Test(expected =UnsupportedOperationException.class)
    public void innerJoinOn_naturalJoin_ambiguity_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P INNER JOIN  Q on P.A =  Q.A NATURAL JOIN  R ");
    }


    //TODO: In this case column reference "a" is ambiguous. Does this should be check during the "selection" operation handler?
    @Test(expected =UnsupportedOperationException.class)
    public void innerJoinOn_naturalJoin_ambiguity2_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // column reference "a" is ambiguous
        String sql = "SELECT A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;";
        final CQIE parse = parser.parse(sql);
        System.out.println("\n" + sql + "\n" + "column reference \"a\" is ambiguous --- this is wrongly parsed:\n" + parse);
    }

    @Test
    public void innerJoinOn_naturalJoin_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        final CQIE parse = parser.parse("SELECT P.A, P.B, R.C, D FROM P NATURAL JOIN Q INNER JOIN  R on Q.C =  R.C;");
        System.out.println(parse);
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
    public void join_using_inner_Test() {
        DBMetadata metadata = createMetadata();


        SelectQueryParser parser = new SelectQueryParser(metadata);

        final CQIE parse = parser.parse("SELECT A, C FROM P INNER JOIN Q USING (A)");
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



    @Test(expected =UnsupportedQueryException.class)
    public void left_outer_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P LEFT  OUTER JOIN  Q on P.A =  Q.A ");
    }

    @Test(expected =UnsupportedQueryException.class)
    public void right_outer_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P RIGHT  OUTER JOIN  Q on P.A =  Q.A ");
    }

    @Test(expected =UnsupportedQueryException.class)
    public void right_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P RIGHT JOIN  Q on P.A =  Q.A ");
    }

    @Test(expected =UnsupportedQueryException.class)
    public void left_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P LEFT JOIN  Q on P.A =  Q.A ");
    }

    @Test(expected =UnsupportedQueryException.class)
    public void full_outer_Join_Test() {
        DBMetadata metadata = createMetadata();
        SelectQueryParser parser = new SelectQueryParser(metadata);
        // common column name "A" appears more than once in left table
        parser.parse("SELECT A, C FROM P FULL OUTER JOIN  Q on P.A =  Q.A ");
    }

    private DBMetadata createMetadata(){
        DBMetadata metadata = DBMetadataExtractor.createDummyMetadata();

        RelationID table1 = MDFAC.createRelationID(null, "P");
        QuotedID attx0 = MDFAC.createAttributeID("A");
        QuotedID atty0 = MDFAC.createAttributeID("B");

        DatabaseRelationDefinition relation1 = metadata.createDatabaseRelation(table1);
        relation1.addAttribute(attx0, 0, "INT", false);
        relation1.addAttribute(atty0, 0, "INT", false);

        RelationID table2 = MDFAC.createRelationID(null, "Q");
        QuotedID attx1 = MDFAC.createAttributeID("A");
        QuotedID atty1 = MDFAC.createAttributeID("C");

        DatabaseRelationDefinition relation2 = metadata.createDatabaseRelation(table2);
        relation2.addAttribute(attx1, 0, "INT", false);
        relation2.addAttribute(atty1, 0, "INT", false);


        RelationID table3 = MDFAC.createRelationID(null, "R");
        QuotedID attx3 = MDFAC.createAttributeID("A");
        QuotedID atty3 = MDFAC.createAttributeID("B");
        QuotedID attu3 = MDFAC.createAttributeID("C");
        QuotedID attv3 = MDFAC.createAttributeID("D");

        DatabaseRelationDefinition relation3= metadata.createDatabaseRelation(table3);
        relation3.addAttribute(attx3, 0, "INT", false);
        relation3.addAttribute(atty3, 0, "INT", false);
        relation3.addAttribute(attu3, 0, "INT", false);
        relation3.addAttribute(attv3, 0, "INT", false);
        return metadata;
    }




}
