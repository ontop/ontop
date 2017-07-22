package it.unibz.inf.ontop.mapping;


import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.unibz.inf.ontop.mapping.BasicNativeMappingMistakeTest.DB_METADATA;

public class BasicR2RMLMappingMistakeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicR2RMLMappingMistakeTest.class);

    @Test(expected = InvalidMappingSourceQueriesException.class)
    public void testUnboundTargetVariable() throws OBDASpecificationException {
        execute("/mistake/unbound-r2rml.ttl");
    }

    @Test(expected = InvalidMappingSourceQueriesException.class)
    public void testInvalidSQLQuery1() throws OBDASpecificationException {
        execute("/mistake/invalid-sql1-r2rml.ttl");
    }

    @Ignore("TODO: create an option for disabling black-box view creation "
            + "and create a specific exception for it")
    @Test
    public void testInvalidSQLQuery2() throws OBDASpecificationException {
        execute("/mistake/invalid-sql2-r2rml.ttl");
    }

    @Test(expected = InvalidMappingException.class)
    public void testInvalidPredicateObject1() throws OBDASpecificationException {
        execute("/mistake/invalid-predicate-object1-r2rml.ttl");
    }

    private void execute(String mappingFile) throws OBDASpecificationException {
        try {
            OntopMappingSQLAllConfiguration configuration = createConfiguration(mappingFile);
            configuration.loadSpecification();
        } catch (Exception e) {
            LOGGER.info(e.toString());
            throw e;
        }
    }

    private OntopMappingSQLAllConfiguration createConfiguration(String mappingFile) {
        return OntopMappingSQLAllConfiguration.defaultBuilder()
                .dbMetadata(DB_METADATA)
                .r2rmlMappingFile(getClass().getResource(mappingFile).getPath())
                .jdbcUrl("jdbc:h2://localhost/fake")
                .jdbcUser("fake_user")
                .jdbcPassword("fake_password")
                .enableProvidedDBMetadataCompletion(false)
                .build();
    }

}
