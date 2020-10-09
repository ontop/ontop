package it.unibz.inf.ontop.rdf4j.jsonld;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.nquads.NQuadsWriter;


import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FramedJSONLDWriter implements RDFWriter {

    private final NQuadsWriter nQuadsWriter;
    private final ByteArrayOutputStream nQuadsOutputStream;
    private final Writer writer;
    private final Map<String, Object> jsonLdFrame;

    @Nullable
    private final String baseIRI;

    public FramedJSONLDWriter(Map<String, Object> jsonLdFrame, OutputStream outputStream) {
        this(jsonLdFrame, outputStream, null);
    }

    /**
     * TODO: consider base IRI
     */
    public FramedJSONLDWriter(Map<String, Object> jsonLdFrame, OutputStream outputStream, @Nullable String baseIRI) {
        writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        this.jsonLdFrame = jsonLdFrame;
        this.baseIRI = baseIRI;
        nQuadsOutputStream = new ByteArrayOutputStream();
        nQuadsWriter = new NQuadsWriter(nQuadsOutputStream);
    }

    /**
     * TODO: consider base IRI
     */
    public FramedJSONLDWriter(Map<String, Object> jsonLdFrame, Writer writer, @Nullable String baseIRI) {
        this.writer = writer;
        this.jsonLdFrame = jsonLdFrame;
        this.baseIRI = baseIRI;
        nQuadsOutputStream = new ByteArrayOutputStream();
        nQuadsWriter = new NQuadsWriter(nQuadsOutputStream);

    }
    public FramedJSONLDWriter(Map<String, Object> jsonLdFrame, Writer writer) {
        this(jsonLdFrame, writer, null);
    }

    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.JSONLD;
    }

    @Override
    public RDFWriter setWriterConfig(WriterConfig config) {
        nQuadsWriter.setWriterConfig(config);
        return this;
    }

    @Override
    public WriterConfig getWriterConfig() {
        return nQuadsWriter.getWriterConfig();
    }

    /**
     * TODO: implement it more seriously
     */
    @Override
    public Collection<RioSetting<?>> getSupportedSettings() {
        return nQuadsWriter.getSupportedSettings();
    }

    @Override
    public <T> RDFWriter set(RioSetting<T> setting, T value) {
        return nQuadsWriter.set(setting, value);
    }

    @Override
    public void startRDF() throws RDFHandlerException {
        nQuadsWriter.startRDF();
    }

    @Override
    public void endRDF() throws RDFHandlerException {
        nQuadsWriter.endRDF();
        try {
            JsonLdOptions options = new JsonLdOptions();
            if (baseIRI != null)
                options.setBase(baseIRI);
            // TODO: make it optional
            options.setUseNativeTypes(true);
            options.setProcessingMode(JsonLdOptions.JSON_LD_1_1);
            //if (expandContext != null)
            //    options.setExpandContext(expandContext);
            Object parsedJsonLd = JsonLdProcessor.fromRDF(nQuadsOutputStream.toString(), options);
            Map<String, Object> framedJsonLd = JsonLdProcessor.frame(parsedJsonLd, jsonLdFrame, options);

            JsonUtils.write(writer, framedJsonLd);
            writer.flush();

        } catch (IOException e) {
            throw new RDFHandlerException(e);
        }
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
        nQuadsWriter.handleNamespace(prefix, uri);
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        nQuadsWriter.handleStatement(st);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
        nQuadsWriter.handleComment(comment);
    }
}
