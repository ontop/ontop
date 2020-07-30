package it.unibz.inf.ontop.spec.mapping.parser;


import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import org.junit.Ignore;
import org.junit.Test;

public class BasicR2RMLMappingMistakeTest extends AbstractBasicMappingMistakeTest {

    @Test(expected = InvalidMappingSourceQueriesException.class)
    public void testUnboundTargetVariable() throws OBDASpecificationException {
        execute("/mistake/unbound-r2rml.ttl");
    }

    @Ignore
    @Test(expected = InvalidMappingSourceQueriesException.class)
    public void testInvalidSQLQuery1() throws OBDASpecificationException {
        execute("/mistake/invalid-sql1-r2rml.ttl");
    }

    @Test(expected = InvalidMappingSourceQueriesException.class)
    public void testInvalidSQLQuery2() throws OBDASpecificationException {
        execute("/mistake/invalid-sql2-r2rml.ttl");
    }

    @Test(expected = InvalidMappingException.class)
    public void testInvalidPredicateObject1() throws OBDASpecificationException {
        execute("/mistake/invalid-predicate-object1-r2rml.ttl");
    }

    @Override
    protected OntopMappingSQLAllConfiguration createConfiguration(String mappingFile) {
        return OntopMappingSQLAllConfiguration.defaultBuilder()
                .r2rmlMappingFile(getClass().getResource(mappingFile).getPath())
                .jdbcUrl("jdbc:h2:mem:questrepository")
                .jdbcUser("fake_user")
                .jdbcPassword("fake_password")
                .build();
    }

}
