package it.unibz.inf.ontop.spec.mapping.parser.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.*;
import it.unibz.inf.ontop.injection.TargetQueryParserFactory;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import org.apache.commons.rdf.api.Graph;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

import static it.unibz.inf.ontop.spec.mapping.OntopNativeMappingSyntax.*;

/**
 * Mapping parser specific to the Ontop Native Mapping Language for SQL.
 *
 * Available through Guice.
 *
 */
public class OntopNativeMappingParser implements SQLMappingParser {
    private static final ImmutableList<String> DEPRECATED_TAGS = ImmutableList.of(
            "[ClassDeclaration]", "[ObjectPropertyDeclaration]", "[DataPropertyDeclaration]");
    private static final String SOURCE_DECLARATION_TAG = "[SourceDeclaration]";
    private static final String COMMENT_SYMBOL = ";";

    private final TargetQueryParserFactory targetQueryParserFactory;
    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final SQLPPSourceQueryFactory sourceQueryFactory;

    /**
     * Create an SQL Mapping Parser for generating an OBDA model.
     */
    @Inject
    private OntopNativeMappingParser(SpecificationFactory specificationFactory,
                                     TargetQueryParserFactory targetQueryParserFactory,
                                     SQLPPMappingFactory ppMappingFactory,
                                     SQLPPSourceQueryFactory sourceQueryFactory) {
        this.targetQueryParserFactory = targetQueryParserFactory;
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.sourceQueryFactory = sourceQueryFactory;
    }

    /**
     * Parsing is not done at construction time because our dependency
     * injection framework (Guice) does not manage exceptions nicely.
     */
    @Override
    public SQLPPMapping parse(File file) throws InvalidMappingException, MappingIOException {
        if (!file.exists()) {
            throw new MappingIOException("WARNING: Cannot locate OBDA file at: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new MappingIOException(String.format("Error while reading the file located at %s.\n" +
                    "Make sure you have the read permission at the location specified.", file.getAbsolutePath()));
        }
        try (Reader reader = new FileReader(file)) {
            return load(reader, specificationFactory, ppMappingFactory, file.getName());
        }
        catch (IOException e) {
            throw new MappingIOException(e);
        }
    }

    @Override
    public SQLPPMapping parse(Reader reader) throws InvalidMappingException, MappingIOException {
        return load(reader, specificationFactory, ppMappingFactory, ".obda file");
    }

    @Override
    public SQLPPMapping parse(Graph mappingGraph)  {
        throw new IllegalArgumentException("The Ontop native mapping language has no RDF serialization. Passing a RDF graph" +
                "to the OntopNativeMappingParser is thus invalid.");
    }


    private SQLPPMapping load(Reader reader, SpecificationFactory specificationFactory,
                              SQLPPMappingFactory ppMappingFactory, String fileName)
            throws MappingIOException, InvalidMappingException {

        ImmutableMap.Builder<String, String> prefixes = ImmutableMap.builder();
        ImmutableList.Builder<SQLPPTriplesMap> mappings = ImmutableList.builder();
        List<String> invalidMappingIndicators = new ArrayList<>();

        try (LineNumberReader lineNumberReader = new LineNumberReader(reader)) {
            try {
                String line;
                while ((line = lineNumberReader.readLine()) != null) {
                    //noinspection StatementWithEmptyBody
                    if (line.isEmpty() || line.trim().indexOf(COMMENT_SYMBOL) == 0) {
                        // skip blank lines and comment lines (starting with ;)
                    }
                    else if (line.contains(PREFIX_DECLARATION_TAG)) {
                        prefixes.putAll(readPrefixDeclaration(lineNumberReader));
                    }
                    else if (line.contains(MAPPING_DECLARATION_TAG)) {
                        TargetQueryParser parser = targetQueryParserFactory.createParser(specificationFactory.createPrefixManager(prefixes.build()));
                        mappings.addAll(readMappingDeclaration(lineNumberReader, parser, invalidMappingIndicators));
                    }
                    // These exceptions will be rethrown as IOException and then as MappingIOException
                    else {
                        DEPRECATED_TAGS.stream()
                                .filter(line::contains)
                                .forEach(tag -> { throw new RuntimeException("The tag " + tag
                                        + " is no longer supported. You may safely remove the content from the file."); });

                        if (line.contains(SOURCE_DECLARATION_TAG)) {
                            throw new RuntimeException("Source declaration is not supported anymore (since 3.0). " +
                                    "Please give this information with the Ontop configuration.");
                        }
                        throw new RuntimeException("Unknown syntax: " + line);
                    }
                }
            }
            catch (Exception e) {
                throw new IOException(String.format("ERROR reading %s at line: %d\nMESSAGE: %s", fileName,
                        lineNumberReader.getLineNumber(), e.getMessage()), e);
            }
        }
        catch (IOException e) {
            throw new MappingIOException(e);
        }

        if (!invalidMappingIndicators.isEmpty())
            throw new InvalidMappingException("\nThe syntax of the mapping is invalid (and therefore cannot be processed). Problems:\n\n"
                    + String.join("\n", invalidMappingIndicators));

        PrefixManager prefixManager = specificationFactory.createPrefixManager(prefixes.build());
        return ppMappingFactory.createSQLPreProcessedMapping(mappings.build(), prefixManager);
    }

    /*
     * Helper methods related to load file.
     */

    private static ImmutableMap<String, String> readPrefixDeclaration(BufferedReader reader) throws IOException {
        ImmutableMap.Builder<String, String> prefixes = ImmutableMap.builder();
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            String[] tokens = line.split("[\t| ]+");
            prefixes.put(tokens[0], tokens[1]);
        }
        return prefixes.build();
    }


    private final class MappingBuilder {
        private final List<String> invalidMappingIndicators;
        private final Supplier<Integer> line;

        private String mappingId = "";
        private final ImmutableList.Builder<String> source = ImmutableList.builder();
        private final ImmutableList.Builder<String> target = ImmutableList.builder();
        private ImmutableList<TargetAtom> targetQuery = null;
        boolean isMappingValid = true;

        MappingBuilder(List<String> invalidMappingIndicators, Supplier<Integer> line) {
            this.invalidMappingIndicators = invalidMappingIndicators;
            this.line = line;
        }

        private void fail(String message) {
            invalidMappingIndicators.add((!mappingId.isEmpty() ? String.format("MappingId = '%s'\n", mappingId) : "") + message);
            isMappingValid = false;
        }

        boolean isValid() { return isMappingValid; }

        Optional<OntopNativeSQLPPTriplesMap> build(TargetQueryParser parser) {
            if (mappingId.isEmpty() || !isMappingValid)
                return Optional.empty();

            String targetString = String.join(" ", target.build());
            if (!targetString.isEmpty()) {
                try {
                    targetQuery = parser.parse(targetString);
                }
                catch (TargetQueryParserException e) {
                    fail(String.format("Line %d: Invalid target: '%s'\nDebug information\n%s", line.get(), targetString, e.getMessage()));
                }
            }
            else
                fail(String.format("Line %d: Target is missing\n", line.get()));

            String sourceString = String.join("\n", source.build());
            SQLPPSourceQuery sourceQuery = null;
            if (!sourceString.isEmpty()) {
                sourceQuery = sourceQueryFactory.createSourceQuery(sourceString);
            }
            else
                fail(String.format("Line %d: Source is missing\n", line.get()));

            return isMappingValid
                    ? Optional.of(new OntopNativeSQLPPTriplesMap(mappingId, sourceQuery, targetString, targetQuery))
                    : Optional.empty();
        }

        void setMappingId(String value) {
            mappingId = value;
            if (mappingId.isEmpty())
                fail(String.format("Line %d: Mapping ID is missing\n", line.get()));
        }

        void setTarget(String value) {
            if (!value.isEmpty())
                target.add(value);
            else
                fail(String.format("Line %d: Target query is missing\n", line.get()));
        }

        void setSource(String value) {
            if (!value.isEmpty())
                source.add(value);
            else
                fail(String.format("Line %d: Source query is missing\n", line.get()));
        }
    }

    /**
     * @param reader LineNumberReader
     * @param parser TargetQueryParser
     * @param invalidMappingIndicators Read-write list of error indicators.
     * @return SQLPPTriplesMaps of the current source
     * @throws IOException
     */
    private ImmutableList<SQLPPTriplesMap> readMappingDeclaration(LineNumberReader reader,
                                                                  TargetQueryParser parser,
                                                                  List<String> invalidMappingIndicators) throws IOException {

        ImmutableList.Builder<SQLPPTriplesMap> mappings = ImmutableList.builder();
        MappingBuilder current = new MappingBuilder(invalidMappingIndicators, reader::getLineNumber);

        String currentLabel = ""; // the reader is working on which label
        String line;
        while ((line = reader.readLine())!= null) {
            if (line.trim().equals(END_COLLECTION_SYMBOL)) {
                current.build(parser).ifPresent(mappings::add);
                return mappings.build();
            }
            else if (line.isEmpty()) {
                current.build(parser).ifPresent(mappings::add);
                current = new MappingBuilder(invalidMappingIndicators, reader::getLineNumber);
            }
            // skip the comment lines (which have ; as their first non-space symbol)
            //      and invalid mappings
            else if (line.trim().indexOf(COMMENT_SYMBOL) != 0 && current.isValid()) {
                String[] tokens = line.split("[\t| ]+", 2);
                String value = tokens[tokens.length - 1].trim();
                if (tokens.length > 1) {
                    String label = tokens[0].trim();
                    if (!label.isEmpty())
                        currentLabel = label;
                }

                switch (currentLabel) {
                    case MAPPING_ID_LABEL:
                        current.setMappingId(value);
                        break;
                    case TARGET_LABEL:
                        current.setTarget(value);
                        break;
                    case SOURCE_LABEL:
                        current.setSource(value);
                        break;
                    default:
                        throw new IOException(String.format("Unknown parameter name \"%s\" at line: %d.", tokens[0], reader.getLineNumber()));
                }
            }
        }

        throw new IOException(String.format("End collection symbol %s is missing.", END_COLLECTION_SYMBOL));
    }
}
