package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.utils.SQLAllMappingTestingTools.*;

public abstract class AbstractBasicMappingMistakeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBasicMappingMistakeTest.class);
    private final DBMetadata dbMetadata;

    AbstractBasicMappingMistakeTest() {
        RDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DatabaseRelationDefinition personTable = dbMetadata.createDatabaseRelation(
                idFactory.createRelationID(null, "PERSON"));
        Attribute personId = personTable.addAttribute(idFactory.createAttributeID("ID"),
                dbTypeFactory.getDBIntegerType().getName(), dbTypeFactory.getDBIntegerType(), false);
        personTable.addAttribute(idFactory.createAttributeID("FNAME"),
                dbTypeFactory.getDBIntegerType().getName(), dbTypeFactory.getDBStringType(), false);
        personTable.addUniqueConstraint(UniqueConstraint.primaryKeyOf(personId));

        dbMetadata.freeze();
        this.dbMetadata = dbMetadata;
    }

    protected void execute(String mappingFile) throws OBDASpecificationException {
        try {
            OntopMappingSQLAllConfiguration configuration = createConfiguration(mappingFile);
            configuration.loadSpecification();
        } catch (Exception e) {
            LOGGER.info(e.toString());
            throw e;
        }
    }

    protected abstract OntopMappingSQLAllConfiguration createConfiguration(String mappingFile);

    protected DBMetadata getDBMetadata() {
        return dbMetadata;
    }
}
