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
        BasicDBMetadata dbMetadata = createDummyMetadata();
        QuotedIDFactory idFactory = dbMetadata.getDBParameters().getQuotedIDFactory();

        DBTypeFactory dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        DatabaseRelationDefinition personTable = dbMetadata.createDatabaseRelation(new RelationDefinition.AttributeListBuilder(
                idFactory.createRelationID(null, "PERSON"))
            .addAttribute(idFactory.createAttributeID("ID"), dbTypeFactory.getDBLargeIntegerType(), false)
            .addAttribute(idFactory.createAttributeID("FNAME"), dbTypeFactory.getDBStringType(), false));
        personTable.addUniqueConstraint(UniqueConstraint.primaryKeyOf(personTable.getAttribute(1)));

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
