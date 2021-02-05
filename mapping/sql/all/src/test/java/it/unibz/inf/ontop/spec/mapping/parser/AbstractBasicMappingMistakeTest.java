package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.OfflineMetadataProviderBuilder;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.utils.SQLAllMappingTestingTools.*;

public abstract class AbstractBasicMappingMistakeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBasicMappingMistakeTest.class);

    static {
        OfflineMetadataProviderBuilder builder = createMetadataProviderBuilder();
        DBTypeFactory dbTypeFactory = builder.getDBTypeFactory();

        NamedRelationDefinition personTable = builder.createDatabaseRelation("PERSON",
            "ID", dbTypeFactory.getDBLargeIntegerType(), false,
            "FNAME", dbTypeFactory.getDBStringType(), false);
        UniqueConstraint.primaryKeyOf(personTable.getAttribute(1));
    }

    protected void execute(String mappingFile) throws OBDASpecificationException {
        try {
            OntopMappingSQLAllConfiguration configuration = createConfiguration(mappingFile);
            configuration.loadSpecification();
        }
        catch (Exception e) {
            LOGGER.info(e.toString());
            throw e;
        }
    }

    protected abstract OntopMappingSQLAllConfiguration createConfiguration(String mappingFile);
}
