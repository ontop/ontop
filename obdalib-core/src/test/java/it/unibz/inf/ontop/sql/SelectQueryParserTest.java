package it.unibz.inf.ontop.sql;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.SelectQueryParser;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Roman Kontchakov on 01/11/2016.
 */
public class SelectQueryParserTest {

    private static OBDADataFactory FACTORY = OBDADataFactoryImpl.getInstance();
    private static DBMetadata METADATA = DBMetadataExtractor.createDummyMetadata();
    private static QuotedIDFactory MDFAC = METADATA.getQuotedIDFactory();

    @Test
    public void f() {
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

        SelectQueryParser parser = new SelectQueryParser(metadata);
        final CQIE parse = parser.parse("SELECT * FROM P, Q");
        System.out.println(parse);

        assertTrue( parse.getHead().getTerms().size()==0);
        assertTrue( parse.getReferencedVariables().size()==4 );


        Variable a1 = FACTORY.getVariable("A1");
        Variable b1 = FACTORY.getVariable("B1");

        Function atomP = FACTORY.getFunction(
                FACTORY.getPredicate("P", new Predicate.COL_TYPE[] { null, null }),
                ImmutableList.of(a1, b1));

        assertTrue( parse.getBody().contains(atomP));

        Variable a2 = FACTORY.getVariable("A2"); // variable are key sensitive
        Variable c2 = FACTORY.getVariable("C2");

        Function atomQ = FACTORY.getFunction(
                FACTORY.getPredicate("Q", new Predicate.COL_TYPE[] { null, null }),
                ImmutableList.of(a2, c2));

        assertTrue( parse.getBody().contains(atomQ));
    }

}
