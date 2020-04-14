package it.unibz.inf.ontop.spec.mapping.serializer.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.parser.impl.OntopNativeMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.SourceQueryRenderer;
import it.unibz.inf.ontop.spec.mapping.serializer.TargetQueryRenderer;

import java.io.*;

/**
 *
 * Serializer for the Ontop Native Mapping Language (SQL-specific).
 *
 * TODO: add a MappingSerializer interface.
 * TODO: consider build from a factory, but make it optional.
 *
 */
public class OntopNativeMappingSerializer {

    private final SQLPPMapping ppMapping;

    /**
     * TODO: may consider building it through Assisted Injection.
     */
    public OntopNativeMappingSerializer(SQLPPMapping ppMapping) {
        this.ppMapping = ppMapping;
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
        final ImmutableMap<String, String> prefixMap = ppMapping.getPrefixManager().getPrefixMap();

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
        for (SQLPPTriplesMap axiom : ppMapping.getTripleMaps()) {

            if (needLineBreak) {
                writer.write("\n");
            }
            writer.write(OntopNativeMappingParser.Label.mappingId.name() + "\t" + axiom.getId() + "\n");

            ImmutableList<TargetAtom> targetQuery = axiom.getTargetAtoms();
            writer.write(OntopNativeMappingParser.Label.target.name() + "\t\t" + printTargetQuery(targetQuery) + "\n");

            SQLPPSourceQuery sourceQuery = axiom.getSourceQuery();
            writer.write(OntopNativeMappingParser.Label.source.name() + "\t\t" + printSourceQuery(sourceQuery) + "\n");
            needLineBreak = true;
        }
        writer.write(OntopNativeMappingParser.END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    private String printTargetQuery(ImmutableList<TargetAtom> query) {
        return TargetQueryRenderer.encode(query, ppMapping.getPrefixManager());
    }

    private String printSourceQuery(SQLPPSourceQuery query) {
        String sourceString = SourceQueryRenderer.encode(query);
        String toReturn = convertTabToSpaces(sourceString);
        return toReturn.replaceAll("\n", "\n\t\t\t");
    }
    private String convertTabToSpaces(String input) {
        return input.replaceAll("\t", "   ");
    }
}
