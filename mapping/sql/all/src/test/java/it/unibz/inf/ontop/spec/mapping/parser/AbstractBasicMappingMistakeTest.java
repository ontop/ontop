package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import static it.unibz.inf.ontop.utils.SQLAllMappingTestingTools.*;

public abstract class AbstractBasicMappingMistakeTest {

    protected static final String ROOT = "src/test/resources/mistake/";

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTypeFactory dbTypeFactory = builder.getDBTypeFactory();

        NamedRelationDefinition personTable = builder.createDatabaseRelation("PERSON",
            "ID", dbTypeFactory.getDBLargeIntegerType(), false,
            "FNAME", dbTypeFactory.getDBStringType(), false);
        UniqueConstraint.primaryKeyOf(personTable.getAttribute(1));
    }

    protected void execute(String mappingFile) throws OBDASpecificationException {
        OntopMappingSQLAllConfiguration.Builder<? extends OntopMappingSQLAllConfiguration.Builder<?>> builder =
                OntopMappingSQLAllConfiguration.defaultBuilder()
                        .jdbcUrl("jdbc:h2:mem:dummy"); // need a database connection
        builder = createConfiguration(builder, mappingFile);
        OntopMappingSQLAllConfiguration configuration = builder.build();

        configuration.loadSpecification();
    }

    protected abstract <T extends OntopMappingSQLAllConfiguration.Builder<T>> OntopMappingSQLAllConfiguration.Builder<T>
                createConfiguration(OntopMappingSQLAllConfiguration.Builder<T> builder, String mappingFile);
}
