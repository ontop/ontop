package it.unibz.inf.ontop.cli;

import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriter;
import org.eclipse.rdf4j.rio.nquads.NQuadsWriter;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.trig.TriGWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

import java.io.BufferedWriter;
import java.util.function.Function;

public enum RDFFormatTypes {
    rdfxml(".rdf", RDFXMLWriter::new),
    turtle(".ttl", w ->  new TurtleWriter(w).set(BasicWriterSettings.PRETTY_PRINT, false)),
    ntriples(".nt", w ->  new NTriplesWriter(w).set(BasicWriterSettings.PRETTY_PRINT, false)),
    nquads(".nq", w ->  new NQuadsWriter(w).set(BasicWriterSettings.PRETTY_PRINT, false)),
    trig(".trig", TriGWriter::new),
    jsonld(".jsonld", JSONLDWriter::new);

    private final String extension;
    private final Function<BufferedWriter, RDFHandler> rdfHandlerProvider;

    RDFFormatTypes(String extension, Function<BufferedWriter, RDFHandler> rdfHandlerProvider) {
        this.extension = extension;
        this.rdfHandlerProvider = rdfHandlerProvider;
    }

    public String getExtension() {
        return extension;
    }

    public RDFHandler createRDFHandler(BufferedWriter writer) {
        return rdfHandlerProvider.apply(writer);
    }
}
