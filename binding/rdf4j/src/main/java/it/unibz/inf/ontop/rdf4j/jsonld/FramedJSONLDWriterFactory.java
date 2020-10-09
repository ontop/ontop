package it.unibz.inf.ontop.rdf4j.jsonld;

import com.github.jsonldjava.core.DocumentLoader;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterFactory;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Map;

public class FramedJSONLDWriterFactory implements RDFWriterFactory {
    private final Map<String, Object> jsonLDFrame;
    @Nullable
    private final DocumentLoader documentLoader;

    public FramedJSONLDWriterFactory(Map<String, Object> jsonLDFrame) {
        this.jsonLDFrame = jsonLDFrame;
        this.documentLoader = null;
    }

    public FramedJSONLDWriterFactory(Map<String, Object> jsonLdFrame, DocumentLoader documentLoader) {
        this.jsonLDFrame = jsonLdFrame;
        this.documentLoader = documentLoader;
    }

    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.JSONLD;
    }

    @Override
    public RDFWriter getWriter(OutputStream out) {
        return new FramedJSONLDWriter(jsonLDFrame, documentLoader, out);
    }
    @Override
    public RDFWriter getWriter(OutputStream out, String baseURI) throws URISyntaxException {
        return new FramedJSONLDWriter(jsonLDFrame, documentLoader, out, baseURI);
    }

    @Override
    public RDFWriter getWriter(Writer writer) {
        return new FramedJSONLDWriter(jsonLDFrame, documentLoader, writer);
    }

    @Override
    public RDFWriter getWriter(Writer writer, String baseURI) throws URISyntaxException {
        return new FramedJSONLDWriter(jsonLDFrame, documentLoader, writer, baseURI);
    }
}
