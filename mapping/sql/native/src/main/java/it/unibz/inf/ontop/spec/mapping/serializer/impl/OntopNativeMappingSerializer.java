package it.unibz.inf.ontop.spec.mapping.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.parser.impl.OntopNativeMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.MappingSerializer;
import it.unibz.inf.ontop.spec.mapping.serializer.SourceQueryRenderer;
import it.unibz.inf.ontop.spec.mapping.serializer.TargetQueryRenderer;

import java.io.*;
import java.util.Map;

/**
 * Serializer for the Ontop Native Mapping Language (SQL-specific).
 */

public class OntopNativeMappingSerializer implements MappingSerializer {

    /**
     * The save/write operation.
     *
     * @param file The file where the mapping is saved.
     * @param ppMapping The mapping
     * @throws IOException
     */
    @Override
    public void write(File file, SQLPPMapping ppMapping) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writePrefixDeclaration(writer, ppMapping.getPrefixManager());
            writeMappingDeclaration(writer, ppMapping);
        }
        catch (IOException e) {
            throw new IOException(String.format("Error while saving the OBDA model to the file located at %s.\n" +
                    "Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
        }
    }


    private void writePrefixDeclaration(BufferedWriter writer, PrefixManager prefixManager) throws IOException {
        final ImmutableMap<String, String> prefixMap = prefixManager.getPrefixMap();

        if (prefixMap.size() == 0) {
            return; // do nothing if there is no prefixes to write
        }

        writer.write(OntopNativeMappingParser.PREFIX_DECLARATION_TAG);
        writer.write("\n");
        for (Map.Entry<String, String> e : prefixMap.entrySet()) {
            String prefix = e.getKey();
            String uri = e.getValue();
            writer.write(prefix + (prefix.length() >= 9 ? "\t" : "\t\t") + uri + "\n");
        }
        writer.write("\n");
    }

    private void writeMappingDeclaration(BufferedWriter writer, SQLPPMapping ppMapping) throws IOException {

        writer.write(OntopNativeMappingParser.MAPPING_DECLARATION_TAG + " " + OntopNativeMappingParser.START_COLLECTION_SYMBOL);
        writer.write("\n");

        boolean needLineBreak = false;
        for (SQLPPTriplesMap axiom : ppMapping.getTripleMaps()) {

            if (needLineBreak) {
                writer.write("\n");
            }
            writer.write(OntopNativeMappingParser.Label.mappingId.name() + "\t" + axiom.getId() + "\n");

            ImmutableList<TargetAtom> targetQuery = axiom.getTargetAtoms();
            writer.write(OntopNativeMappingParser.Label.target.name() + "\t\t" + printTargetQuery(targetQuery, ppMapping.getPrefixManager()) + "\n");

            SQLPPSourceQuery sourceQuery = axiom.getSourceQuery();
            writer.write(OntopNativeMappingParser.Label.source.name() + "\t\t" + printSourceQuery(sourceQuery) + "\n");
            needLineBreak = true;
        }
        writer.write(OntopNativeMappingParser.END_COLLECTION_SYMBOL);
        writer.write("\n\n");
    }

    private String printTargetQuery(ImmutableList<TargetAtom> query, PrefixManager prefixManager) {
        return TargetQueryRenderer.encode(query, prefixManager);
    }

    private static String printSourceQuery(SQLPPSourceQuery query) {
        String sourceString = SourceQueryRenderer.encode(query);
        String toReturn = convertTabToSpaces(sourceString);
        return toReturn.replaceAll("\n", "\n\t\t\t");
    }

    private static String convertTabToSpaces(String input) {
        return input.replaceAll("\t", "   ");
    }
}
