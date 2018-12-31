package it.unibz.inf.ontop.dbschema;

import org.junit.Test;

import java.sql.Types;

import static it.unibz.inf.ontop.OntopModelTestingTools.createDummyMetadata;

/**
 * Test that we correctly output exceptions in case we try to insert an incorrect foreign key (missing values)
 */
public class WrongForeignKeyTest {
    private static final DBMetadata METADATA;

    static{
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        /*
         * Table 1:
         */
        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID("schema1","table1"));
        Attribute col1T1 = table1Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, "INTEGER", false);
        Attribute col2T1 = table1Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, "INTEGER", false);
        Attribute col3T1 = table1Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, "INTEGER", false);
        Attribute col4T1 = table1Def.addAttribute(idFactory.createAttributeID("col4"), Types.INTEGER, "INTEGER", false);
        // Independent
        table1Def.addAttribute(idFactory.createAttributeID("col5"), Types.INTEGER, "INTEGER", false);
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T1));
        table1Def.addFunctionalDependency(FunctionalDependency.defaultBuilder()
                .addDeterminant(col2T1)
                .addDependent(col3T1)
                .addDependent(col4T1)
                .build());


        /*
         * Table 2:
         */
        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID("schema1","table2"));
        table2Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, "INTEGER", false);
        Attribute col2T2 = table2Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, "INTEGER", false);
        table2Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, "INTEGER", false);
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col2T2));


        /*
         * Table 3:
         */
        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(idFactory.createRelationID(null,"table3"));
        Attribute col1T3 = table3Def.addAttribute(idFactory.createAttributeID("col1"), Types.INTEGER, "INTEGER", false);
        Attribute col2T3 = table3Def.addAttribute(idFactory.createAttributeID("col2"), Types.INTEGER, "INTEGER", false);
        Attribute col3T3 = table3Def.addAttribute(idFactory.createAttributeID("col3"), Types.INTEGER, "INTEGER", false);
        Attribute col4T3 = table3Def.addAttribute(idFactory.createAttributeID("col4"), Types.INTEGER, "INTEGER", false);
        Attribute col5T3 = table3Def.addAttribute(idFactory.createAttributeID("col5"), Types.INTEGER, "INTEGER", false);
        table3Def.addAttribute(idFactory.createAttributeID("col6"), Types.INTEGER, "INTEGER", false);
        table3Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T3));
        table3Def.addFunctionalDependency(FunctionalDependency.defaultBuilder()
                .addDeterminant(col2T3)
                .addDependent(col3T3)
                .build());
        table3Def.addFunctionalDependency(FunctionalDependency.defaultBuilder()
                .addDeterminant(col4T3)
                .addDependent(col5T3)
                .build());


        dbMetadata.freeze();
        METADATA = dbMetadata;
    }


    //Add a foreign key constraint where a column is missing in the referring table
    @Test(expected = IllegalArgumentException.class)
    public void testMissingColumnPK(){

        RelationID relationId = RelationID.createRelationIdFromDatabaseRecord(METADATA.getQuotedIDFactory(),
                "SCHEMA1", "TABLE1");
        RelationID refId = RelationID.createRelationIdFromDatabaseRecord(METADATA.getQuotedIDFactory(),
                "SCHEMA1", "TABLE2");
        DatabaseRelationDefinition relation = METADATA.getDatabaseRelation(relationId);
        DatabaseRelationDefinition ref = METADATA.getDatabaseRelation(refId);

        ForeignKeyConstraint.Builder builder;

        builder = new ForeignKeyConstraint.Builder(relation, ref);

        QuotedID attrId = QuotedID.createIdFromDatabaseRecord(METADATA.getQuotedIDFactory(), "COL4");
        QuotedID refAttrId = QuotedID.createIdFromDatabaseRecord(METADATA.getQuotedIDFactory(), "COL4");
        builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));

    }

    //Add two foreign key constraints referring to two different tables
    @Test(expected = IllegalArgumentException.class)
    public void testFKonDifferentTables(){

        RelationID relationId = RelationID.createRelationIdFromDatabaseRecord(METADATA.getQuotedIDFactory(),
                "SCHEMA1", "TABLE1");
        RelationID refId = RelationID.createRelationIdFromDatabaseRecord(METADATA.getQuotedIDFactory(),
                "SCHEMA1", "TABLE2");
        DatabaseRelationDefinition relation = METADATA.getDatabaseRelation(relationId);
        DatabaseRelationDefinition ref = METADATA.getDatabaseRelation(refId);

        ForeignKeyConstraint.Builder builder;

        builder = new ForeignKeyConstraint.Builder(relation, ref);

        QuotedID attrId = QuotedID.createIdFromDatabaseRecord(METADATA.getQuotedIDFactory(), "COL2");
        QuotedID refAttrId = QuotedID.createIdFromDatabaseRecord(METADATA.getQuotedIDFactory(), "COL1");
        builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));

        RelationID relationId2 = RelationID.createRelationIdFromDatabaseRecord(METADATA.getQuotedIDFactory(),
                "SCHEMA1", "TABLE3");

        DatabaseRelationDefinition relation2 = METADATA.getDatabaseRelation(relationId2);

        QuotedID attrId2 = QuotedID.createIdFromDatabaseRecord(METADATA.getQuotedIDFactory(), "COL2");
        QuotedID refAttrId2 = QuotedID.createIdFromDatabaseRecord(METADATA.getQuotedIDFactory(), "COL1");
        builder.add(relation2.getAttribute(attrId2), ref.getAttribute(refAttrId2));

    }
}
