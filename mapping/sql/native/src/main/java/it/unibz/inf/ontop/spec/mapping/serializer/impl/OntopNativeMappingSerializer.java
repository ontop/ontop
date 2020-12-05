package it.unibz.inf.ontop.spec.mapping.serializer.impl;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.impl.OntopNativeMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.MappingSerializer;

import java.io.*;
import java.util.stream.Collectors;

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
        PrefixManager prefixManager = ppMapping.getPrefixManager();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(renderPrefixDeclaration(prefixManager));

            TargetQueryRenderer targetQueryRenderer = new TargetQueryRenderer(prefixManager);

            writer.write(OntopNativeMappingParser.MAPPING_DECLARATION_TAG + " "
                    + OntopNativeMappingParser.START_COLLECTION_SYMBOL + "\n");

            writer.write(ppMapping.getTripleMaps().stream()
                    .map(ax -> renderTriplesMap(ax, targetQueryRenderer))
                    .collect(Collectors.joining("\n")));

            writer.write(OntopNativeMappingParser.END_COLLECTION_SYMBOL + "\n\n");
        }
        catch (IOException e) {
            throw new IOException(String.format("Error while saving the OBDA model to the file located at %s.\n" +
                    "Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
        }
    }

    private String renderPrefixDeclaration(PrefixManager prefixManager)  {
        ImmutableMap<String, String> prefixMap = prefixManager.getPrefixMap();

        if (prefixMap.isEmpty())
            return "";

        return OntopNativeMappingParser.PREFIX_DECLARATION_TAG + "\n" +
                prefixMap.entrySet().stream()
                    .map(e -> e.getKey() + (e.getKey().length() >= 9 ? "\t" : "\t\t") + e.getValue())
                    .collect(Collectors.joining("\n"))
                + "\n\n";
    }

    private String renderTriplesMap(SQLPPTriplesMap axiom, TargetQueryRenderer targetQueryRenderer) {
        return OntopNativeMappingParser.MAPPING_ID_LABEL + "\t" +
                axiom.getId() + "\n" +
                OntopNativeMappingParser.TARGET_LABEL + "\t\t" +
                targetQueryRenderer.encode(axiom.getTargetAtoms()) + "\n" +
                OntopNativeMappingParser.SOURCE_LABEL + "\t\t" +
                axiom.getSourceQuery().getSQL()
                        .replaceAll("\t", "   ")
                        .replaceAll("\n", "\n\t\t\t") +
                "\n";
    }
}
