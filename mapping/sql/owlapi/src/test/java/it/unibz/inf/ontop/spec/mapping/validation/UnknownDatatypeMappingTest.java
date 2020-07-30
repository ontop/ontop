package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.InvalidMappingExceptionWithIndicator;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import org.apache.commons.rdf.api.IRI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class UnknownDatatypeMappingTest {

    private static final String JDBC_URL = "jdbc:h2:mem:unknown-datatype-inference";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";
    private static final String DIR = "/unknown-datatype/";
    private static final String CREATE_SCRIPT = DIR + "create-db.sql";
    private static final String DROP_SCRIPT = DIR + "drop-db.sql";
    private static final String DEFAULT_OWL_FILE = DIR + "marriage.ttl";
    private static TestConnectionManager TEST_MANAGER;

    @BeforeClass
    public static void setUp() throws Exception {
        TEST_MANAGER = new TestConnectionManager(JDBC_URL, DB_USER, DB_PASSWORD, CREATE_SCRIPT, DROP_SCRIPT);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TEST_MANAGER.close();
    }

    @Test
    public void testUpperFunction() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_unknown_function.obda");
        checkDatatype(spec.getSaturatedMapping(), XSD.STRING);

    }

    @Test(expected = UnknownDatatypeException.class)
    public void testMappingUnknownStringFunction() throws OBDASpecificationException {
        TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_view_function.obda");
    }

    @Test
    public void testMappingFunction() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_function.obda");
        checkDatatype(spec.getSaturatedMapping(), XSD.STRING);
    }

    @Test
    public void testMappingTargetFunction() throws OBDASpecificationException {
        OBDASpecification spec = TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_target_function.obda");
        checkDatatype(spec.getSaturatedMapping(), XSD.STRING);
    }

    @Ignore("TODO: reactivate it once the SQL and SPARQL CONCAT functions will be distinguished")
    @Test
    public void testMappingIntFunction() throws OBDASpecificationException {
        OBDASpecification spec =TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_int_function.obda");
        checkDatatype(spec.getSaturatedMapping(), XSD.STRING);
    }

    @Test(expected = UnknownDatatypeException.class)
    public void testMappingToCharFunction() throws OBDASpecificationException {
        TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_tochar_function.obda");
    }

    @Test(expected = UnknownDatatypeException.class)
    public void testMappingRDFSLiteralFunction() throws OBDASpecificationException {
        TEST_MANAGER.extractSpecification(DEFAULT_OWL_FILE, DIR + "marriage_rdfsliteral.obda");
    }

    private void checkDatatype(Mapping mapping, IRI expectedType) {
        RDFAtomPredicate triplePredicate = mapping.getRDFAtomPredicates().stream()
                .findFirst().get();

        Optional<IRI> optionalDatatype = mapping.getRDFProperties(triplePredicate).stream()
                .map(i -> mapping.getRDFPropertyDefinition(triplePredicate, i))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(query -> Optional.of(query.getTree().getRootNode())
                        .filter(r -> r instanceof ConstructionNode)
                        .map(r -> (ConstructionNode)r)
                        .map(r -> r.getSubstitution().getImmutableMap().values().stream())
                        .orElseGet(Stream::empty))
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .flatMap(t-> t.inferType()
                        .flatMap(TermTypeInference::getTermType)
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype)t)
                .map(RDFDatatype::getIRI)
                .findFirst();

        assertTrue("A datatype was expected", optionalDatatype.isPresent());
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        IRI datatype = optionalDatatype.get();

        assertEquals(expectedType, datatype);
    }
}
