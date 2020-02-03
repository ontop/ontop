package it.unibz.inf.ontop.spec.mapping.serializer;

/*
 * #%L
 * ontop-obdalib-sesame
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


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import eu.optique.r2rml.api.binding.jena.JenaR2RMLMappingManager;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.model.atom.TargetAtom;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
//import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shared.PrefixMapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class SQLPPMappingToR2RMLConverter {

    private List<SQLPPTriplesMap> ppMappingAxioms;
    private PrefixManager prefixManager;
    private final RDF rdfFactory;
    private final TermFactory termFactory;

    public SQLPPMappingToR2RMLConverter(SQLPPMapping ppMapping, RDF rdfFactory, TermFactory termFactory) {
        this.ppMappingAxioms = ppMapping.getTripleMaps();
        this.prefixManager = ppMapping.getPrefixManager();
        this.rdfFactory = rdfFactory;
        this.termFactory = termFactory;
    }

    /**
     * TODO: remove the splitting logic, not needed anymore.
     */
    public Collection<TriplesMap> getTripleMaps() {
        OBDAMappingTransformer transformer = new OBDAMappingTransformer(rdfFactory, termFactory);
        return splitMappingAxioms(this.ppMappingAxioms).stream()
                .flatMap(a -> transformer.getTriplesMaps(a, prefixManager))
                .collect(Collectors.toList());
    }

    private ImmutableSet<SQLPPTriplesMap> splitMappingAxioms(List<SQLPPTriplesMap> mappingAxioms) {
        /*
		 * Delimiter string d used for assigning ids to split mapping axioms.
		 * If a mapping axiom with id j is split into multiple ones,
		 * each of then will have "j"+"d"+int as an identifier
		 */
        String delimiterSubstring = getSplitMappingAxiomIdDelimiterSubstring(mappingAxioms);
        return mappingAxioms.stream()
                .flatMap(m -> splitMappingAxiom(m, delimiterSubstring).stream())
                .collect(ImmutableCollectors.toSet());
    }

    private String getSplitMappingAxiomIdDelimiterSubstring(List<SQLPPTriplesMap> mappingAxioms) {
        String delimiterSubstring = "";
        boolean matched;
        do {
            delimiterSubstring += "_";
            Pattern pattern = Pattern.compile(delimiterSubstring + "(\\d)*$");
            matched = mappingAxioms.stream()
                    .anyMatch(a -> pattern.matcher(a.getId()).matches());
        } while (matched);
        return delimiterSubstring;
    }

    private ImmutableList<SQLPPTriplesMap> splitMappingAxiom(SQLPPTriplesMap mappingAxiom, String delimiterSubstring) {
        Multimap<ImmutableFunctionalTerm, TargetAtom> subjectTermToTargetTriples = ArrayListMultimap.create();
        for (TargetAtom targetTriple : mappingAxiom.getTargetAtoms()) {
            ImmutableFunctionalTerm subjectTerm = getFirstFunctionalTerm(targetTriple)
                    .orElseThrow(() -> new IllegalStateException("Invalid OBDA mapping"));
            subjectTermToTargetTriples.put(subjectTerm, targetTriple);
        }
        // If the partition per target triple subject is non trivial
        if (subjectTermToTargetTriples.size() > 1) {
            // Create ids for the new mapping axioms
            Map<ImmutableFunctionalTerm, String> subjectTermToMappingIndex = new HashMap<>();
            int i = 1;
            for (ImmutableFunctionalTerm subjectTerm : subjectTermToTargetTriples.keySet()) {
                subjectTermToMappingIndex.put(subjectTerm, mappingAxiom.getId() + delimiterSubstring + i);
                i++;
            }
            // Generate one mapping axiom per subject
            return subjectTermToTargetTriples.asMap().entrySet().stream()
                    .map(e -> mappingAxiom.extractPPMappingAssertions(
                            subjectTermToMappingIndex.get(e.getKey()),
                            ImmutableList.copyOf(e.getValue())))
                    .collect(ImmutableCollectors.toList());
        }
        return ImmutableList.of(mappingAxiom);
    }

    private Optional<ImmutableFunctionalTerm> getFirstFunctionalTerm(TargetAtom targetAtom) {
        return targetAtom.getSubstitution().getImmutableMap().values().stream()
                .findFirst()
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t);
    }


    /**
     * the method to write the R2RML mappings
     * from an rdf Model to a file
     *
     * @param file the ttl file to write to
     */
    public void write(File file) throws Exception {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            write(fos);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * the method to write the R2RML mappings
     * from an rdf Model to a file
     *
     * @param os the output target
     */
    public void write(OutputStream os) throws Exception {
        try {
            JenaR2RMLMappingManager mm = JenaR2RMLMappingManager.getInstance();
            Collection<TriplesMap> tripleMaps = getTripleMaps();

            final JenaGraph jenaGraph = mm.exportMappings(tripleMaps);
            final Graph graph = new JenaRDF().asJenaGraph(jenaGraph);

            final PrefixMapping jenaPrefixMapping = graph.getPrefixMapping();
            prefixManager.getPrefixMap()
                    .forEach((s, s1) ->
                            jenaPrefixMapping.setNsPrefix(
                                    s.substring(0, s.length()-1) // remove the last ":" from the prefix
                                    , s1));
            jenaPrefixMapping.setNsPrefix("rr", "http://www.w3.org/ns/r2rml#");

            RDFWriter.create()
                    .lang(Lang.TTL)
                    .format(RDFFormat.TURTLE_PRETTY)
                    .source(graph)
                    .output(os);

            // use Jena to output pretty turtle syntax
            //RDFDataMgr.write(os, graph, org.apache.jena.riot.RDFFormat.TURTLE_PRETTY);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
