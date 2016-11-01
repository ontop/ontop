package it.unibz.inf.ontop.sql;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.parser.SelectQueryParser;
import org.junit.Test;

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

        System.out.println(parser.parse("SELECT * FROM P, Q"));
    }

}
