package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.RelationalExpression;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 *
 */
public class RelationalExpressionTest {

    private static OBDADataFactory FACTORY = OBDADataFactoryImpl.getInstance();
    private static DBMetadata METADATA = DBMetadataExtractor.createDummyMetadata();
    private static QuotedIDFactory MDFAC = METADATA.getQuotedIDFactory();

    @Test
    public void f() {

        Variable x = FACTORY.getVariable("x");
        Variable y = FACTORY.getVariable("y");

        Function f1 = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[] { null, null }),
                ImmutableList.<Term>of(x, y));

        RelationID table1 = MDFAC.createRelationID(null, "P");
        QuotedID attx = MDFAC.createAttributeID("A");
        QuotedID atty = MDFAC.createAttributeID("B");

        RelationalExpression re1 = new RelationalExpression(ImmutableList.of(f1),
                ImmutableMap.of(new QualifiedAttributeID(table1, attx), x,
                        new QualifiedAttributeID(table1, atty), y,
                        new QualifiedAttributeID(null, attx), x,
                        new QualifiedAttributeID(null, atty), y),
                ImmutableMap.of(attx, ImmutableSet.of(table1), atty, ImmutableSet.of(table1)));

        Variable u = FACTORY.getVariable("u");
        Variable v = FACTORY.getVariable("v");

        Function f2 = FACTORY.getFunction(
                FACTORY.getPredicate("Q", new Predicate.COL_TYPE[] { null, null }),
                ImmutableList.<Term>of(u, v));

        RelationID table2 = MDFAC.createRelationID(null, "Q");
        QuotedID attu = MDFAC.createAttributeID("A");
        QuotedID attv = MDFAC.createAttributeID("C");

        RelationalExpression re2 = new RelationalExpression(ImmutableList.of(f2),
                ImmutableMap.of(new QualifiedAttributeID(table2, attu), u,
                        new QualifiedAttributeID(table2, attv), v,
                        new QualifiedAttributeID(null, attu), u,
                        new QualifiedAttributeID(null, attv), v),
                ImmutableMap.of(attu, ImmutableSet.of(table2), attv, ImmutableSet.of(table2)));

        System.out.println(re1);
        System.out.println(re2);
        final RelationalExpression relationalExpression = RelationalExpression.crossJoin(re1, re2);
        System.out.println(relationalExpression);

        assertTrue(relationalExpression.getAtoms().indexOf(f1)>=0);
        assertTrue(relationalExpression.getAtoms().indexOf(f2)>0);

    }
}
