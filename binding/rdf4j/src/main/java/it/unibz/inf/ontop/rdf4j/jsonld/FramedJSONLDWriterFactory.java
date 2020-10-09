package it.unibz.inf.ontop.rdf4j.jsonld;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterFactory;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Map;

public class FramedJSONLDWriterFactory implements RDFWriterFactory {
    private final Map<String, Object> jsonLDFrame;

    public FramedJSONLDWriterFactory(Map<String, Object> jsonLDFrame) {
        this.jsonLDFrame = jsonLDFrame;
    }

    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.JSONLD;
    }

    @Override
    public RDFWriter getWriter(OutputStream out) {
        return new FramedJSONLDWriter(jsonLDFrame, out);
    }
    @Override
    public RDFWriter getWriter(OutputStream out, String baseURI) throws URISyntaxException {
        return new FramedJSONLDWriter(jsonLDFrame, out, baseURI);
    }

    @Override
    public RDFWriter getWriter(Writer writer) {
        return new FramedJSONLDWriter(jsonLDFrame, writer);
    }

    @Override
    public RDFWriter getWriter(Writer writer, String baseURI) throws URISyntaxException {
        return new FramedJSONLDWriter(jsonLDFrame, writer, baseURI);
    }
}
