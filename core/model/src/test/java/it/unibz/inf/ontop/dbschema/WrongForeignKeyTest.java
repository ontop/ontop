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
        DatabaseRelationDefinition table1Def = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(ID_FACTORY.createRelationID("schema1","table1"))
            .addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col4"), integerDBType.getName(), integerDBType, false)
            // Independent
            .addAttribute(ID_FACTORY.createAttributeID("col5"), integerDBType.getName(), integerDBType, false));
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table1Def.getAttribute(1)));
        table1Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table1Def)
                .addDeterminant(table1Def.getAttribute(2))
                .addDependent(table1Def.getAttribute(3))
                .addDependent(table1Def.getAttribute(4))
                .build());


        /*
         * Table 2:
         */
        DatabaseRelationDefinition table2Def = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(ID_FACTORY.createRelationID("schema1","table2"))
            .addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType.getName(), integerDBType, false));
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table2Def.getAttribute(2)));


        /*
         * Table 3:
         */
        DatabaseRelationDefinition table3Def = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(ID_FACTORY.createRelationID(null,"table3"))
            .addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col4"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col5"), integerDBType.getName(), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col6"), integerDBType.getName(), integerDBType, false));
        table3Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table3Def.getAttribute(1)));
        table3Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table3Def)
                .addDeterminant(table3Def.getAttribute(2))
                .addDependent(table3Def.getAttribute(3))
                .build());
        table3Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table3Def)
                .addDeterminant(table3Def.getAttribute(4))
                .addDependent(table3Def.getAttribute(5))
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
