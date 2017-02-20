package it.unibz.inf.ontop.io;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.renderer.SourceQueryRenderer;
import it.unibz.inf.ontop.renderer.TargetQueryRenderer;

import java.io.*;
import java.util.List;

/**
 *
 * Serializer for the Ontop Native Mapping Language (SQL-specific).
 *
 * TODO: add a MappingSerializer interface.
 * TODO: consider build from a factory, but make it optional.
 *
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
        writeMappingDeclaration(bufferWriter);
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
        final ImmutableMap<String, String> prefixMap = model.getMetadata().getPrefixManager().getPrefixMap();

        if (prefixMap.size() == 0) {
            return; // do nothing if there is no prefixes to write
        }

        writer.write(OntopNativeMappingParser.PREFIX_DECLARATION_TAG);
        writer.write("\n");
        for (String prefix : prefixMap.keySet()) {
            String uri = prefixMap.get(prefix);
            writer.write(prefix + (prefix.length() >= 9 ? "\t" : "\t\t") + uri + "\n");
        }
        writer.write("\n");
    }

    private void writeMappingDeclaration(BufferedWriter writer) throws IOException {

        writer.write(OntopNativeMappingParser.MAPPING_DECLARATION_TAG + " " + OntopNativeMappingParser.START_COLLECTION_SYMBOL);
        writer.write("\n");

        boolean needLineBreak = false;
        for (OBDAMappingAxiom axiom : model.getMappings()) {
            if (!(axiom.getSourceQuery() instanceof OBDASQLQuery)) {
                throw new IllegalArgumentException("The ontop native mapping serializer only supports OBDAMappingAxioms with OBDASQLQueries");
            }

            if (needLineBreak) {
                writer.write("\n");
            }
            writer.write(OntopNativeMappingParser.Label.mappingId.name() + "\t" + axiom.getId() + "\n");

            List<Function> targetQuery = axiom.getTargetQuery();
            writer.write(OntopNativeMappingParser.Label.target.name() + "\t\t" + printTargetQuery(targetQuery) + "\n");

            OBDASQLQuery sourceQuery = (OBDASQLQuery) axiom.getSourceQuery();
            writer.write(OntopNativeMappingParser.Label.source.name() + "\t\t" + printSourceQuery(sourceQuery) + "\n");
            needLineBreak = true;
        }
        writer.write(OntopNativeMappingParser.END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    private String printTargetQuery(List<Function> query) {
        return TargetQueryRenderer.encode(query, model.getMetadata().getPrefixManager());
    }

    private String printSourceQuery(OBDASQLQuery query) {
        String sourceString = SourceQueryRenderer.encode(query);
        String toReturn = convertTabToSpaces(sourceString);
        return toReturn.replaceAll("\n", "\n\t\t\t");
    }
    private String convertTabToSpaces(String input) {
        return input.replaceAll("\t", "   ");
    }
}
