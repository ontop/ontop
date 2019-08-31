package it.unibz.inf.ontop.spec.mapping.parser;

import eu.optique.r2rml.api.model.ObjectMap;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllConfiguration;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.SQLPPMappingToR2RMLConverter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class R2rmlConversionTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(R2rmlConversionTest.class);


   private Collection<TriplesMap> execute(String inputMappingFile) throws Exception {
       OntopMappingSQLAllConfiguration.Builder configBuilder = OntopMappingSQLAllConfiguration.defaultBuilder()
               .nativeOntopMappingFile(inputMappingFile)
               .jdbcDriver("dummy")
               .jdbcUrl("dummy")
               .jdbcUser("")
               .jdbcPassword("");

       OntopMappingSQLAllConfiguration config = configBuilder.build();

       SQLPPMapping ppMapping;
        /*
         * load the mapping in native Ontop syntax
         */
       try {
           ppMapping = config.loadProvidedPPMapping();
       } catch (MappingException e) {
           e.printStackTrace();
           System.exit(1);
           return null;
       }


       SQLPPMappingToR2RMLConverter converter = new SQLPPMappingToR2RMLConverter(ppMapping,
               config.getRdfFactory(), config.getTermFactory());
       return converter.getTripleMaps();


   }

   @Test
   public void testColumn() throws Exception {
       Collection<TriplesMap> triplesMaps = execute("src/test/resources/npd-column-mapping.obda");
       for (TriplesMap triplesMap :triplesMaps){
           ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
           assertEquals("dateSyncNPD", objectMap.getColumn());
           assertEquals("http://www.w3.org/2001/XMLSchema#dateTime", objectMap.getDatatype().getIRIString());
           assertNull(objectMap.getTemplate());
       }

   }

    @Test
    public void testConstant() throws Exception {
        Collection<TriplesMap> triplesMaps = execute("src/test/resources/npd-constant-mapping.obda");
        for (TriplesMap triplesMap :triplesMaps){
            ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
            assertEquals("\"true\"^^<http://www.w3.org/2001/XMLSchema#boolean>", objectMap.getConstant().ntriplesString());
            assertNull(objectMap.getTemplate());
        }

    }

    @Test
    public void testLiteral() throws Exception {
        Collection<TriplesMap> triplesMaps = execute("src/test/resources/npd-literal-mapping.obda");
        for (TriplesMap triplesMap :triplesMaps){
            ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
            assertEquals("\"someUntypedLiteral\"", objectMap.getConstant().ntriplesString());
            assertNull(objectMap.getDatatype().getIRIString());
            assertNull(objectMap.getTemplate());
        }

    }

    @Test
    public void testLang() throws Exception {
        Collection<TriplesMap> triplesMaps = execute("src/test/resources/langstring-mapping.obda");
        for (TriplesMap triplesMap :triplesMaps){
            ObjectMap objectMap = triplesMap.getPredicateObjectMap(0).getObjectMap(0);
            assertEquals("title", objectMap.getColumn());
            assertNull(objectMap.getDatatype());
            assertEquals("en", objectMap.getLanguageTag());
            assertNull(objectMap.getTemplate());
        }

    }

}

