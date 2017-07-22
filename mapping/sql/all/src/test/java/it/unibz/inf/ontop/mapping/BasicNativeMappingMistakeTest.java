package it.unibz.inf.ontop.mapping;


import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class BasicNativeMappingMistakeTest {

    private static final RDBMetadata DB_METADATA;
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicNativeMappingMistakeTest.class);

    static {
            RDBMetadata dbMetadata = RDBMetadataExtractionTools.createDummyMetadata();
            QuotedIDFactory idFactory = dbMetadata.getQuotedIDFactory();

            DatabaseRelationDefinition personTable = dbMetadata.createDatabaseRelation(
                    idFactory.createRelationID(null, "PERSON"));
            Attribute personId = personTable.addAttribute(idFactory.createAttributeID("ID"),
                    Types.INTEGER, null, false);
            personTable.addAttribute(idFactory.createAttributeID("FNAME"),
                    Types.VARCHAR, null, false);
            personTable.addUniqueConstraint(UniqueConstraint.primaryKeyOf(personId));

            dbMetadata.freeze();
            DB_METADATA = dbMetadata;
        }

    @Test(expected = InvalidMappingSourceQueriesException.class)
    public void testUnboundTargetVariable() throws OBDASpecificationException {
        execute("/mistake/unbound.obda");
    }

    @Test(expected = InvalidMappingSourceQueriesException.class)
    public void testInvalidSQLQuery1() throws OBDASpecificationException {
        execute("/mistake/invalid-sql1.obda");
    }

    @Ignore("TODO: create an option for disabling black-box view creation "
            + "and create a specific exception for it")
    @Test
    public void testInvalidSQLQuery2() throws OBDASpecificationException {
        execute("/mistake/invalid-sql2.obda");
    }

    @Test(expected = InvalidMappingException.class)
    public void testMissingTargetTerm() throws OBDASpecificationException {
        execute("/mistake/missing-target-term.obda");
    }

    private void execute(String obdaFile) throws OBDASpecificationException {
        try {
            OntopMappingSQLAllConfiguration configuration = createConfiguration(obdaFile);
            configuration.loadSpecification();
        } catch (Exception e) {
            LOGGER.info(e.toString());
            throw e;
        }
    }

    private OntopMappingSQLAllConfiguration createConfiguration(String obdaFile) {
        return OntopMappingSQLAllConfiguration.defaultBuilder()
                .dbMetadata(DB_METADATA)
                .nativeOntopMappingFile(getClass().getResource(obdaFile).getPath())
                .jdbcUrl("jdbc:h2://localhost/fake")
                .jdbcUser("fake_user")
                .jdbcPassword("fake_password")
                .enableProvidedDBMetadataCompletion(false)
                .build();
    }

}
