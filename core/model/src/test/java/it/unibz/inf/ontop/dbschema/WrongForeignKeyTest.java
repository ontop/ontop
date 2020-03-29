package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.TYPE_FACTORY;
import static it.unibz.inf.ontop.OntopModelTestingTools.createDummyMetadata;

/**
 * Test that we correctly output exceptions in case we try to insert an incorrect foreign key (missing values)
 */
public class WrongForeignKeyTest {
    private static final DBMetadata METADATA;
    private  static final QuotedIDFactory ID_FACTORY;

    static{
        BasicDBMetadata dbMetadata = createDummyMetadata();
        ID_FACTORY = dbMetadata.getQuotedIDFactory();

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();
        DBTermType integerDBType = dbTypeFactory.getDBLargeIntegerType();

        /*
         * Table 1:
         */
        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(ID_FACTORY.createRelationID("schema1","table1"));
        Attribute col1T1 = table1Def.addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType.getName(), integerDBType, false);
        Attribute col2T1 = table1Def.addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType.getName(), integerDBType, false);
        Attribute col3T1 = table1Def.addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType.getName(), integerDBType, false);
        Attribute col4T1 = table1Def.addAttribute(ID_FACTORY.createAttributeID("col4"), integerDBType.getName(), integerDBType, false);
        // Independent
        table1Def.addAttribute(ID_FACTORY.createAttributeID("col5"), integerDBType.getName(), integerDBType, false);
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T1));
        table1Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table1Def)
                .addDeterminant(col2T1)
                .addDependent(col3T1)
                .addDependent(col4T1)
                .build());


        /*
         * Table 2:
         */
        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(ID_FACTORY.createRelationID("schema1","table2"));
        table2Def.addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType.getName(), integerDBType, false);
        Attribute col2T2 = table2Def.addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType.getName(), integerDBType, false);
        table2Def.addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType.getName(), integerDBType, false);
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col2T2));


        /*
         * Table 3:
         */
        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(ID_FACTORY.createRelationID(null,"table3"));
        Attribute col1T3 = table3Def.addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType.getName(), integerDBType, false);
        Attribute col2T3 = table3Def.addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType.getName(), integerDBType, false);
        Attribute col3T3 = table3Def.addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType.getName(), integerDBType, false);
        Attribute col4T3 = table3Def.addAttribute(ID_FACTORY.createAttributeID("col4"), integerDBType.getName(), integerDBType, false);
        Attribute col5T3 = table3Def.addAttribute(ID_FACTORY.createAttributeID("col5"), integerDBType.getName(), integerDBType, false);
        table3Def.addAttribute(ID_FACTORY.createAttributeID("col6"), integerDBType.getName(), integerDBType, false);
        table3Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(col1T3));
        table3Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table3Def)
                .addDeterminant(col2T3)
                .addDependent(col3T3)
                .build());
        table3Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table3Def)
                .addDeterminant(col4T3)
                .addDependent(col5T3)
                .build());


        dbMetadata.freeze();
        METADATA = dbMetadata;
    }


    //Add a foreign key constraint where a column is missing in the referring table
    @Test(expected = IllegalArgumentException.class)
    public void testMissingColumnPK(){

        RelationID relationId = RelationID.createRelationIdFromDatabaseRecord(ID_FACTORY,
                "SCHEMA1", "TABLE1");
        RelationID refId = RelationID.createRelationIdFromDatabaseRecord(ID_FACTORY,
                "SCHEMA1", "TABLE2");
        DatabaseRelationDefinition relation = METADATA.getDatabaseRelation(relationId);
        DatabaseRelationDefinition ref = METADATA.getDatabaseRelation(refId);

        ForeignKeyConstraint.Builder builder;

        builder = new ForeignKeyConstraint.Builder(relation, ref);

        QuotedID attrId = QuotedID.createIdFromDatabaseRecord(ID_FACTORY, "COL4");
        QuotedID refAttrId = QuotedID.createIdFromDatabaseRecord(ID_FACTORY, "COL4");
        builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));

    }

    //Add two foreign key constraints referring to two different tables
    @Test(expected = IllegalArgumentException.class)
    public void testFKonDifferentTables(){

        RelationID relationId = RelationID.createRelationIdFromDatabaseRecord(ID_FACTORY,
                "SCHEMA1", "TABLE1");
        RelationID refId = RelationID.createRelationIdFromDatabaseRecord(ID_FACTORY,
                "SCHEMA1", "TABLE2");
        DatabaseRelationDefinition relation = METADATA.getDatabaseRelation(relationId);
        DatabaseRelationDefinition ref = METADATA.getDatabaseRelation(refId);

        ForeignKeyConstraint.Builder builder;

        builder = new ForeignKeyConstraint.Builder(relation, ref);

        QuotedID attrId = QuotedID.createIdFromDatabaseRecord(ID_FACTORY, "COL2");
        QuotedID refAttrId = QuotedID.createIdFromDatabaseRecord(ID_FACTORY, "COL1");
        builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));

        RelationID relationId2 = RelationID.createRelationIdFromDatabaseRecord(ID_FACTORY,
                "SCHEMA1", "TABLE3");

        DatabaseRelationDefinition relation2 = METADATA.getDatabaseRelation(relationId2);

        QuotedID attrId2 = QuotedID.createIdFromDatabaseRecord(ID_FACTORY, "COL2");
        QuotedID refAttrId2 = QuotedID.createIdFromDatabaseRecord(ID_FACTORY, "COL1");
        builder.add(relation2.getAttribute(attrId2), ref.getAttribute(refAttrId2));
    }
}
