package org.semanticweb.ontop.io;


import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.renderer.SourceQueryRenderer;
import org.semanticweb.ontop.renderer.TargetQueryRenderer;

import java.io.*;
import java.net.URI;
import java.util.Map;

import static org.semanticweb.ontop.io.OntopNativeMappingParser.*;
import static org.semanticweb.ontop.io.OntopNativeMappingParser.END_COLLECTION_SYMBOL;
import static org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants.*;

/**
 * TODO: describe
 */
public class OntopNativeMappingSerializer {

    private final OBDAModel model;

    /**
     * TODO: may consider building it through Assisted Injection.
     */
    public OntopNativeMappingSerializer(OBDAModel model) {
        this.model = model;
    }

    /**
     * The save/write operation.
     *
     * @param writer
     *          The target writer to which the model is saved.
     * @throws IOException
     */
    public void save(Writer writer) throws IOException {
        BufferedWriter bufferWriter = new BufferedWriter(writer);
        writePrefixDeclaration(bufferWriter);
        for (OBDADataSource source : model.getSources()) {
            writeSourceDeclaration(source, bufferWriter);
            writeMappingDeclaration(source, bufferWriter);
        }
        bufferWriter.flush();
        bufferWriter.close();
    }

    public void save(File file) throws IOException {
        try {
            save(new FileWriter(file));
        } catch (IOException e) {
            throw new IOException(String.format("Error while saving the OBDA model to the file located at %s.\n" +
                    "Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
        }
    }

        /*
     * Helper methods related to save file.
     */

    private void writePrefixDeclaration(BufferedWriter writer) throws IOException {
        final Map<String, String> prefixMap = model.getPrefixManager().getPrefixMap();

        if (prefixMap.size() == 0) {
            return; // do nothing if there is no prefixes to write
        }

        writer.write(PREFIX_DECLARATION_TAG);
        writer.write("\n");
        for (String prefix : prefixMap.keySet()) {
            String uri = prefixMap.get(prefix);
            writer.write(prefix + (prefix.length() >= 9 ? "\t" : "\t\t") + uri + "\n");
        }
        writer.write("\n");
    }

    private void writeSourceDeclaration(OBDADataSource source, BufferedWriter writer) throws IOException {
        writer.write(SOURCE_DECLARATION_TAG);
        writer.write("\n");
        writer.write(Label.sourceUri.name() + "\t" + source.getSourceID() + "\n");
        writer.write(Label.connectionUrl.name() + "\t" + source.getParameter(DATABASE_URL) + "\n");
        writer.write(Label.username.name() + "\t" + source.getParameter(DATABASE_USERNAME) + "\n");
        writer.write(Label.password.name() + "\t" + source.getParameter(DATABASE_PASSWORD) + "\n");
        writer.write(Label.driverClass.name() + "\t" + source.getParameter(DATABASE_DRIVER) + "\n");
        writer.write("\n");
    }

    private void writeMappingDeclaration(OBDADataSource source, BufferedWriter writer) throws IOException {
        final URI sourceUri = source.getSourceID();

        writer.write(MAPPING_DECLARATION_TAG + " " + START_COLLECTION_SYMBOL);
        writer.write("\n");

        boolean needLineBreak = false;
        for (OBDAMappingAxiom mapping : model.getMappings(sourceUri)) {
            if (needLineBreak) {
                writer.write("\n");
            }
            writer.write(Label.mappingId.name() + "\t" + mapping.getId() + "\n");

            OBDAQuery targetQuery = mapping.getTargetQuery();
            writer.write(Label.target.name() + "\t\t" + printTargetQuery(targetQuery) + "\n");

            OBDAQuery sourceQuery = mapping.getSourceQuery();
            writer.write(Label.source.name() + "\t\t" + printSourceQuery(sourceQuery) + "\n");
            needLineBreak = true;
        }
        writer.write(END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    private String printTargetQuery(OBDAQuery query) {
        return TargetQueryRenderer.encode(query, model.getPrefixManager());
    }

    private String printSourceQuery(OBDAQuery query) {
        String sourceString = SourceQueryRenderer.encode(query);
        String toReturn = convertTabToSpaces(sourceString);
        return toReturn.replaceAll("\n", "\n\t\t\t");
    }
    private String convertTabToSpaces(String input) {
        return input.replaceAll("\t", "   ");
    }
}
