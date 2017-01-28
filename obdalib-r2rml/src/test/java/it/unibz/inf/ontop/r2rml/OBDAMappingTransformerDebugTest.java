package it.unibz.inf.ontop.r2rml;


import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.OBDACoreConfiguration;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.model.OBDAModel;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import static junit.framework.TestCase.assertTrue;

public class OBDAMappingTransformerDebugTest {

    @Test
    public void testMultipleSubjectsInMappingTarget() throws InvalidDataSourceException, IOException, InvalidMappingException {
        File mapFile = new File("src/test/resources/obdaMappingTransformerTests/splitMappingAxiomBySubject.obda");

        OBDACoreConfiguration config = OBDACoreConfiguration.defaultBuilder()
                .nativeOntopMappingFile(mapFile)
                .build();

        OBDAModel model = config.loadProvidedMapping();
        URI srcURI = model.getSources().iterator().next().getSourceID();

        R2RMLWriter writer = new R2RMLWriter(model, srcURI, null, config.getInjector());

        Collection<TriplesMap> tripleMaps = writer.getTripleMaps();
        for (TriplesMap tripleMap : tripleMaps) {
            System.out.println(tripleMap);
        }
        assertTrue(tripleMaps.size() > 1);
    }

    @Test
    public void testPredicateMapTranslation() throws InvalidDataSourceException, IOException, InvalidMappingException {
        File mapFile = new File("src/test/resources/obdaMappingTransformerTests/predicateMap.obda");
        OBDACoreConfiguration config = OBDACoreConfiguration.defaultBuilder()
                .nativeOntopMappingFile(mapFile)
                .build();

        OBDAModel model = config.loadProvidedMapping();
        URI srcURI = model.getSources().iterator().next().getSourceID();

        R2RMLWriter writer = new R2RMLWriter(model, srcURI, null, config.getInjector());

        assertTrue(writer.getTripleMaps().stream().findFirst()
                .filter(m -> m.getPredicateObjectMap(0).getPredicateMap(0).getTemplate() != null)
                .isPresent());
    }
}
