package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.model.type.DBTermType;
import org.junit.Test;

import static it.unibz.inf.ontop.OntopModelTestingTools.createMetadataProviderBuilder;


/**
 * Test that we correctly output exceptions in case we try to insert an incorrect foreign key (missing values)
 */

public class WrongForeignKeyTest {
    private static final QuotedIDFactory ID_FACTORY;
    private static final NamedRelationDefinition table1Def, table2Def, table3Def;

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        ID_FACTORY = builder.getQuotedIDFactory();
        DBTermType integerDBType = builder.getDBTypeFactory().getDBLargeIntegerType();

        table1Def = builder.createDatabaseRelation("table1",
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

        table2Def = builder.createDatabaseRelation("table2",
            "col1", integerDBType, false,
            "col2", integerDBType, false,
            "col3", integerDBType, false);
        UniqueConstraint.primaryKeyOf(table2Def.getAttribute(2));

        table3Def = builder.createDatabaseRelation("table3",
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
    @Test(expected = AttributeNotFoundException.class)
    public void testMissingColumnPK() throws AttributeNotFoundException {
        ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder("", table1Def, table2Def);

        QuotedID attrId = ID_FACTORY.createAttributeID("COL4");
        QuotedID refAttrId = ID_FACTORY.createAttributeID( "COL4");
        builder.add(attrId, refAttrId);
    }

}
