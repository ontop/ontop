package it.unibz.inf.ontop.spec.mapping.parser;

import eu.optique.r2rml.api.model.ObjectMap;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.SQLPPMappingToR2RMLConverter;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class R2rmlConversionTest {

   private TriplesMap execute(String inputMappingFile) throws Exception {
       OntopMappingSQLAllConfiguration config = OntopMappingSQLAllConfiguration.defaultBuilder()
               .nativeOntopMappingFile(inputMappingFile)
               .jdbcDriver("dummy")
               .jdbcUrl("dummy")
               .jdbcUser("")
               .jdbcPassword("")
               .build();

       SQLPPMapping ppMapping = config.loadProvidedPPMapping();
       SQLPPMappingToR2RMLConverter converter = new SQLPPMappingToR2RMLConverter(ppMapping,
               config.getRdfFactory(), config.getTermFactory());
       Collection<TriplesMap> triplesMaps = converter.getTripleMaps();
       assertEquals(1, triplesMaps.size());
       return triplesMaps.iterator().next();
   }

   @Test
   public void testColumn() throws Exception {
       TriplesMap triplesMap = execute("src/test/resources/npd-column-mapping.obda");
       ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
       assertEquals("dateSyncNPD", objectMap.getColumn());
       assertEquals("http://www.w3.org/2001/XMLSchema#dateTime", objectMap.getDatatype().getIRIString());
       assertNull(objectMap.getTemplate());
   }

    @Test
    public void testConstant() throws Exception {
        TriplesMap triplesMap = execute("src/test/resources/npd-constant-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertEquals("\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>", objectMap.getConstant().ntriplesString());
        assertNull(objectMap.getTemplate());
    }

    @Test
    public void testUntypedLiteral() throws Exception {
        TriplesMap triplesMap = execute("src/test/resources/npd-literal-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertEquals("someColName", objectMap.getColumn());
        assertNull(objectMap.getDatatype());
        assertNull(objectMap.getTemplate());
    }

    @Test
    public void testLang() throws Exception {
        TriplesMap triplesMap = execute("src/test/resources/langstring-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertEquals("title", objectMap.getColumn());
        assertNull(objectMap.getDatatype());
        assertEquals("en", objectMap.getLanguageTag());
        assertNull(objectMap.getTemplate());
    }

    @Test
    public void testConstantIri() throws Exception {
        TriplesMap triplesMap = execute("src/test/resources/npd-constant-iri-mapping.obda");
        ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
        assertEquals("\"Fake point\"", objectMap.getConstant().ntriplesString());
        assertEquals("<http://sws.ifi.uio.no/data/npd-v2/wellbore/point>", triplesMap.getSubjectMap().getConstant().ntriplesString());
        assertNull(objectMap.getTemplate());
    }
}

