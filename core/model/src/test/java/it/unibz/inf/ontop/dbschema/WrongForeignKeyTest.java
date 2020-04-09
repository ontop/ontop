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

    static {
        ID_FACTORY = DEFAULT_DUMMY_DB_METADATA.getQuotedIDFactory();
        DBTermType integerDBType = DEFAULT_DUMMY_DB_METADATA.getDBTypeFactory().getDBLargeIntegerType();

        table1Def = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation("table1",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false);
        table1Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table1Def.getAttribute(1)));
        table1Def.addFunctionalDependency(FunctionalDependency.defaultBuilder(table1Def)
                .addDeterminant(table1Def.getAttribute(2))
                .addDependent(table1Def.getAttribute(3))
                .addDependent(table1Def.getAttribute(4))
                .build());

        table2Def = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation("table2",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        table2Def.addUniqueConstraint(UniqueConstraint.primaryKeyOf(table2Def.getAttribute(2)));

        table3Def = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation("table3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false,
            "col6", integerDBType, false);
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

        ForeignKeyConstraint.Builder builder = new ForeignKeyConstraint.Builder("", relation, ref);

        QuotedID attrId = ID_FACTORY.createAttributeID("COL4");
        QuotedID refAttrId = ID_FACTORY.createAttributeID( "COL4");
        builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));
    }

    //Add two foreign key constraints referring to two different tables
    @Test(expected = IllegalArgumentException.class)
    public void testFKonDifferentTables(){
        DatabaseRelationDefinition relation = table1Def;
        DatabaseRelationDefinition ref = table2Def;

        ForeignKeyConstraint.Builder builder = new ForeignKeyConstraint.Builder("", relation, ref);

        QuotedID attrId = ID_FACTORY.createAttributeID("COL2");
        QuotedID refAttrId = ID_FACTORY.createAttributeID("COL1");
        builder.add(relation.getAttribute(attrId), ref.getAttribute(refAttrId));

        DatabaseRelationDefinition relation2 = table3Def;

        QuotedID attrId2 = ID_FACTORY.createAttributeID("COL2");
        QuotedID refAttrId2 = ID_FACTORY.createAttributeID( "COL1");
        builder.add(relation2.getAttribute(attrId2), ref.getAttribute(refAttrId2));
    }
}
