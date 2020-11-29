package it.unibz.inf.ontop.spec.mapping.serializer.impl;


import com.google.common.collect.ImmutableList;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.serializer.MappingSerializer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4JGraph;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

import java.io.*;


public class R2RMLMappingSerializer implements MappingSerializer {

    private final RDF rdfFactory;
    private final RDF4JR2RMLMappingManager manager;

    public R2RMLMappingSerializer(RDF rdfFactory) {
        this.manager = RDF4JR2RMLMappingManager.getInstance();
        this.rdfFactory = rdfFactory;
    }

    /**
     * the method to write the R2RML mappings to a file
     *
     * @param file the ttl file to write to
     * @param ppMapping mapping
     */
    @Override
    public void write(File file, SQLPPMapping ppMapping) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            SQLPPTriplesMapToR2RMLConverter transformer = new SQLPPTriplesMapToR2RMLConverter(rdfFactory, manager.getMappingFactory());
            ImmutableList<TriplesMap> tripleMaps = ppMapping.getTripleMaps().stream()
                    .flatMap(transformer::convert)
                    .collect(ImmutableCollectors.toList());
            RDF4JGraph rdf4JGraph = manager.exportMappings(tripleMaps);
            Model model = rdf4JGraph.asModel().get();

            model.setNamespace("rr", "http://www.w3.org/ns/r2rml#");
            ppMapping.getPrefixManager().getPrefixMap()
                    .forEach((key, value) ->
                            model.setNamespace(new SimpleNamespace(
                                    key.substring(0, key.length() - 1), // remove the last ":" from the prefix,
                                    value)));

            WriterConfig settings = new WriterConfig();
            settings.set(BasicWriterSettings.PRETTY_PRINT, true);
            settings.set(BasicWriterSettings.INLINE_BLANK_NODES, true);
            Rio.write(model, fos, RDFFormat.TURTLE, settings);
        }
    }
}
