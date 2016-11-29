package it.unibz.inf.ontop.r2rml;

import eu.optique.api.mapping.TriplesMap;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.io.ModelIOManager;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import static junit.framework.TestCase.assertTrue;

public class OBDAMappingTransformerDebugTest {

    @Test
    public void testMultipleSubjectsInMappingTarget() {
        File mapFile = new File("src/test/resources/obdaMappingTransformerTests/splitMappingAxiomBySubject.obda");
        URI obdaURI = mapFile.toURI();
        OBDAModel model = OBDADataFactoryImpl.getInstance()
                .getOBDAModel();
        ModelIOManager modelIO = new ModelIOManager(model);
        try {
            modelIO.load(new File(obdaURI));
        } catch (IOException | InvalidMappingException e) {
            e.printStackTrace();
        }
        URI srcURI = model.getSources().get(0).getSourceID();

        R2RMLWriter writer = new R2RMLWriter(model, srcURI);

        Collection<TriplesMap> tripleMaps = writer.getTripleMaps();
        for (TriplesMap tripleMap : tripleMaps) {
            System.out.println(tripleMap);
        }
        assertTrue(tripleMaps.size() > 1);
    }

    @Test
    public void testPredicateMapTranslation() {
        File mapFile = new File("src/test/resources/obdaMappingTransformerTests/predicateMap.obda");
        URI obdaURI = mapFile.toURI();
        OBDAModel model = OBDADataFactoryImpl.getInstance()
                .getOBDAModel();
        ModelIOManager modelIO = new ModelIOManager(model);
        try {
            modelIO.load(new File(obdaURI));
        } catch (IOException | InvalidMappingException e) {
            e.printStackTrace();
        }
        URI srcURI = model.getSources().get(0).getSourceID();

        R2RMLWriter writer = new R2RMLWriter(model, srcURI);

        assertTrue(writer.getTripleMaps().stream().findFirst()
                .filter(m -> m.getPredicateObjectMap(0).getPredicateMap(0).getTemplate() != null)
                .isPresent());
    }
}
