package it.unibz.inf.ontop.spec.mapping.serializer.impl;

import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.MappingSerializer;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.spec.mapping.OntopNativeMappingSyntax.*;

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
            Map<String, String> prefixMap = prefixManager.getPrefixMap();
            if (!prefixMap.isEmpty()) {
                writer.write(prefixMap.entrySet().stream()
                        .map(e -> e.getKey() + (e.getKey().length() >= 9 ? "\t" : "\t\t") + e.getValue())
                        .collect(Collectors.joining("\n",
                                PREFIX_DECLARATION_TAG + "\n",
                                "\n\n")));
            }

            TargetQueryRenderer targetQueryRenderer = new TargetQueryRenderer(prefixManager);
            writer.write(ppMapping.getTripleMaps().stream()
                    .map(ax -> renderTriplesMap(ax, targetQueryRenderer))
                    .collect(Collectors.joining("\n",
                            MAPPING_DECLARATION_TAG + " " + START_COLLECTION_SYMBOL + "\n",
                            END_COLLECTION_SYMBOL + "\n\n")));
        }
        catch (IOException e) {
            throw new IOException(String.format("Error while saving the OBDA model to the file located at %s.\n" +
                    "Make sure you have the write permission at the location specified.", file.getAbsolutePath()));
        }
    }

    private String renderTriplesMap(SQLPPTriplesMap axiom, TargetQueryRenderer targetQueryRenderer) {
        return MAPPING_ID_LABEL + "\t" + axiom.getId() + "\n" +
                TARGET_LABEL + "\t\t" + targetQueryRenderer.encode(axiom.getTargetAtoms()) + "\n" +
                SOURCE_LABEL + "\t\t" + axiom.getSourceQuery().getSQL()
                        .replaceAll("\t", "   ")
                        .replaceAll("\n", "\n\t\t\t") +
                "\n";
    }
}
