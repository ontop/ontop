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
        UniqueConstraint.primaryKeyOf(table1Def.getAttribute(1));
        FunctionalDependency.defaultBuilder(table1Def)
                .addDeterminant(2)
                .addDependent(3)
                .addDependent(4)
                .build();

        table2Def = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation("table2",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(table2Def.getAttribute(2));

        table3Def = DEFAULT_DUMMY_DB_METADATA.createDatabaseRelation("table3",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false,
            "col4", integerDBType, false,
            "col5", integerDBType, false,
            "col6", integerDBType, false);
        UniqueConstraint.primaryKeyOf(table3Def.getAttribute(1));
        FunctionalDependency.defaultBuilder(table3Def)
                .addDeterminant(2)
                .addDependent(3)
                .build();
        FunctionalDependency.defaultBuilder(table3Def)
                .addDeterminant(4)
                .addDependent(5)
                .build();
    }


    //Add a foreign key constraint where a column is missing in the referring table
    @Test(expected = RelationDefinition.AttributeNotFoundException.class)
    public void testMissingColumnPK() throws RelationDefinition.AttributeNotFoundException {
        ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder("", table1Def, table2Def);

        QuotedID attrId = ID_FACTORY.createAttributeID("COL4");
        QuotedID refAttrId = ID_FACTORY.createAttributeID( "COL4");
        builder.add(attrId, refAttrId);
    }

}
