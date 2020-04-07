package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.DEFAULT_DUMMY_DB_METADATA;

/**
 * Test that we correctly output exceptions in case we try to insert an incorrect foreign key (missing values)
 */
public class WrongForeignKeyTest {
    private static final QuotedIDFactory ID_FACTORY;
    private static final DatabaseRelationDefinition table1Def, table2Def, table3Def;

    static{
        DummyDBMetadataBuilder dbMetadata = DEFAULT_DUMMY_DB_METADATA;
        ID_FACTORY = dbMetadata.getQuotedIDFactory();
        DBTypeFactory dbTypeFactory = dbMetadata.getDBTypeFactory();
        DBTermType integerDBType = dbTypeFactory.getDBLargeIntegerType();

        table1Def = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(ID_FACTORY.createRelationID("schema1","table1"))
            .addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col4"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col5"), integerDBType, false));
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table1Def.getAttribute(1)));
        table1Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table1Def)
                .addDeterminant(table1Def.getAttribute(2))
                .addDependent(table1Def.getAttribute(3))
                .addDependent(table1Def.getAttribute(4))
                .build());

        table2Def = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(ID_FACTORY.createRelationID("schema1","table2"))
            .addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType, false));
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table2Def.getAttribute(2)));

        table3Def = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(ID_FACTORY.createRelationID(null,"table3"))
            .addAttribute(ID_FACTORY.createAttributeID("col1"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col2"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col3"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col4"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col5"), integerDBType, false)
            .addAttribute(ID_FACTORY.createAttributeID("col6"), integerDBType, false));
        table3Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table3Def.getAttribute(1)));
        table3Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table3Def)
                .addDeterminant(table3Def.getAttribute(2))
                .addDependent(table3Def.getAttribute(3))
                .build());
        table3Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table3Def)
                .addDeterminant(table3Def.getAttribute(4))
                .addDependent(table3Def.getAttribute(5))
                .build());
    }


    //Add a foreign key constraint where a column is missing in the referring table
    @Test(expected = IllegalArgumentException.class)
    public void testMissingColumnPK(){
        DatabaseRelationDefinition relation = table1Def;
        DatabaseRelationDefinition ref = table2Def;

        ForeignKeyConstraint.Builder builder = new ForeignKeyConstraint.Builder(relation, ref);

        QuotedID attrId = ID_FACTORY.createAttributeID("COL4");
        QuotedID refAttrId = ID_FACTORY.createAttributeID( "COL4");
        builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));

    }

    //Add two foreign key constraints referring to two different tables
    @Test(expected = IllegalArgumentException.class)
    public void testFKonDifferentTables(){
        DatabaseRelationDefinition relation = table1Def;
        DatabaseRelationDefinition ref = table2Def;

        ForeignKeyConstraint.Builder builder = new ForeignKeyConstraint.Builder(relation, ref);

        QuotedID attrId = ID_FACTORY.createAttributeID("COL2");
        QuotedID refAttrId = ID_FACTORY.createAttributeID("COL1");
        builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));

        DatabaseRelationDefinition relation2 = table3Def;

        QuotedID attrId2 = ID_FACTORY.createAttributeID("COL2");
        QuotedID refAttrId2 = ID_FACTORY.createAttributeID( "COL1");
        builder.add(relation2.getAttribute(attrId2), ref.getAttribute(refAttrId2));
    }
}
