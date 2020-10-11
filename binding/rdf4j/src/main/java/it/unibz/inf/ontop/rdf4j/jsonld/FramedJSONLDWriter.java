package it.unibz.inf.ontop.rdf4j.jsonld;

import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdConsts;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.nquads.NQuadsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FramedJSONLDWriter implements RDFWriter {

    private final NQuadsWriter nQuadsWriter;
    private final ByteArrayOutputStream nQuadsOutputStream;
    private final Writer writer;
    private final Map<String, Object> jsonLdFrame;
    private static final Logger LOGGER = LoggerFactory.getLogger(FramedJSONLDWriter.class);

    @Nullable
    private final String baseIRI;
    private final boolean throwExceptionIfEmpty;
    @Nullable
    private final DocumentLoader documentLoader;

    public FramedJSONLDWriter(Map<String, Object> jsonLdFrame, @Nullable DocumentLoader documentLoader,
                              OutputStream outputStream, boolean throwExceptionIfEmpty) {
        this(jsonLdFrame, documentLoader, outputStream, null, throwExceptionIfEmpty);
    }

    public FramedJSONLDWriter(Map<String, Object> jsonLdFrame, @Nullable DocumentLoader documentLoader, OutputStream outputStream,
                              @Nullable String baseIRI, boolean throwExceptionIfEmpty) {
        this.documentLoader = documentLoader;
        writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        this.jsonLdFrame = jsonLdFrame;
        this.baseIRI = baseIRI;
        this.throwExceptionIfEmpty = throwExceptionIfEmpty;
        nQuadsOutputStream = new ByteArrayOutputStream();
        nQuadsWriter = new NQuadsWriter(nQuadsOutputStream);
    }

    public FramedJSONLDWriter(Map<String, Object> jsonLdFrame, @Nullable DocumentLoader documentLoader, Writer writer,
                              @Nullable String baseIRI, boolean throwExceptionIfEmpty) {
        this.writer = writer;
        this.jsonLdFrame = jsonLdFrame;
        this.baseIRI = baseIRI;
        this.throwExceptionIfEmpty = throwExceptionIfEmpty;
        nQuadsOutputStream = new ByteArrayOutputStream();
        nQuadsWriter = new NQuadsWriter(nQuadsOutputStream);
        this.documentLoader = documentLoader;

    }
    public FramedJSONLDWriter(Map<String, Object> jsonLdFrame, @Nullable DocumentLoader documentLoader,
                              Writer writer, boolean throwExceptionIfEmpty) {
        this(jsonLdFrame, documentLoader, writer, null, throwExceptionIfEmpty);
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
            if (documentLoader != null)
                options.setDocumentLoader(documentLoader);

            Object parsedJsonLd = JsonLdProcessor.fromRDF(nQuadsOutputStream.toString(StandardCharsets.UTF_8.name()), options);

            // Empty result: was causing a NullPointerException while framing
            if ((parsedJsonLd instanceof List) && ((List) parsedJsonLd).isEmpty()) {
                if (throwExceptionIfEmpty)
                    throw new EmptyResultException();

                // After framing, the convention seems to be providing an empty map
                JsonUtils.write(writer, new HashMap<>());
                writer.flush();
                return;
            }

            Map<String, Object> framedJsonLd = JsonLdProcessor.frame(parsedJsonLd, jsonLdFrame, options);

            // Forces the usage of the @context of the frame (not the resolved one)
            Optional.ofNullable(jsonLdFrame.get(JsonLdConsts.CONTEXT))
                    .ifPresent(v -> framedJsonLd.put(JsonLdConsts.CONTEXT, v));

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
