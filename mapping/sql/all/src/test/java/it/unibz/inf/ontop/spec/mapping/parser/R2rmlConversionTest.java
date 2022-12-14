package it.unibz.inf.ontop.spec.mapping.parser;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.ObjectMap;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.SQLPPTriplesMapToR2RMLConverter;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;


public class R2rmlConversionTest {

    private static final String ROOT = "src/test/resources/";
    private final SQLMappingParser mappingParser;
    private final RDF rdfFactory;

    R2rmlConversionTest() {
        OntopMappingSQLAllConfiguration configuration = OntopMappingSQLAllConfiguration.defaultBuilder()
                .jdbcDriver("dummy")
                .jdbcUrl("dummy")
                .build();

        Injector injector = configuration.getInjector();
        mappingParser = injector.getInstance(SQLMappingParser.class);
        rdfFactory = configuration.getRdfFactory();
    }
    private TriplesMap execute(String inputMappingFile) throws Exception {
        SQLPPTriplesMapToR2RMLConverter transformer = new SQLPPTriplesMapToR2RMLConverter(rdfFactory,
                RDF4JR2RMLMappingManager.getInstance().getMappingFactory());
        SQLPPMapping ppMapping = mappingParser.parse(new File(inputMappingFile));
        ImmutableList<TriplesMap> triplesMaps = ppMapping.getTripleMaps().stream()
                .flatMap(transformer::convert)
                .collect(ImmutableCollectors.toList());
        assertEquals(1, triplesMaps.size());
        return triplesMaps.get(0);
    }

    @Test
    public void testColumn() throws Exception {
        TriplesMap triplesMap = execute(ROOT + "npd-column-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertAll(() ->  assertEquals("dateSyncNPD", objectMap.getColumn()),
                () -> assertEquals("http://www.w3.org/2001/XMLSchema#dateTime", objectMap.getDatatype().getIRIString()),
                () -> assertNull(objectMap.getTemplate()));
    }

    @Test
    public void testConstant() throws Exception {
        TriplesMap triplesMap = execute(ROOT + "npd-constant-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertAll(() -> assertEquals("\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>", objectMap.getConstant().ntriplesString()),
                () -> assertNull(objectMap.getTemplate()));
    }

    @Test
    public void testUntypedLiteral() throws Exception {
        TriplesMap triplesMap = execute(ROOT + "npd-literal-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertAll(() -> assertEquals("someColName", objectMap.getColumn()),
                () -> assertNull(objectMap.getDatatype()),
                () -> assertNull(objectMap.getTemplate()));
    }

    @Test
    public void testLang() throws Exception {
        TriplesMap triplesMap = execute(ROOT + "langstring-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertAll(() -> assertEquals("title", objectMap.getColumn()),
                () -> assertNull(objectMap.getDatatype()),
                () -> assertEquals("en", objectMap.getLanguageTag()),
                () -> assertNull(objectMap.getTemplate()));
    }

    @Test
    public void testConstantIri() throws Exception {
        TriplesMap triplesMap = execute(ROOT + "npd-constant-iri-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertAll(() -> assertEquals("\"Fake point\"", objectMap.getConstant().ntriplesString()),
                () -> assertEquals("<http://sws.ifi.uio.no/data/npd-v2/wellbore/point>", triplesMap.getSubjectMap().getConstant().ntriplesString()),
                () -> assertNull(objectMap.getTemplate()));
    }
}

