package it.unibz.inf.ontop.spec.mapping.serializer;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import eu.optique.r2rml.api.binding.rdf4j.RDF4JR2RMLMappingManager;
import eu.optique.r2rml.api.model.TriplesMap;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.rdf4j.RDF4JGraph;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

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

    private final List<SQLPPTriplesMap> ppMappingAxioms;
    private final PrefixManager prefixManager;
    private final RDF rdfFactory;
    private final TermFactory termFactory;

    public SQLPPMappingToR2RMLConverter(SQLPPMapping ppMapping, RDF rdfFactory, TermFactory termFactory) {
        this.ppMappingAxioms = ppMapping.getTripleMaps();
        this.prefixManager = ppMapping.getPrefixManager();
        this.rdfFactory = rdfFactory;
        this.termFactory = termFactory;
    }

    /**
     * TODO: remove the splitting logic, not needed anymore.
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
        Multimap<NonVariableTerm, TargetAtom> subjectTermToTargetTriples = ArrayListMultimap.create();
        for (TargetAtom targetTriple : mappingAxiom.getTargetAtoms()) {
            NonVariableTerm subjectTerm = getFirstTerm(targetTriple)
                    .orElseThrow(() -> new IllegalStateException("Invalid OBDA mapping"));
            subjectTermToTargetTriples.put(subjectTerm, targetTriple);
        }
        // If the partition per target triple subject is non trivial
        if (subjectTermToTargetTriples.size() > 1) {
            // Create ids for the new mapping axioms
            Map<NonVariableTerm, String> subjectTermToMappingIndex = new HashMap<>();
            int i = 1;
            for (NonVariableTerm subjectTerm : subjectTermToTargetTriples.keySet()) {
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

    private Optional<NonVariableTerm> getFirstTerm(TargetAtom targetAtom) {
        return targetAtom.getSubstitution().getImmutableMap().values().stream()
                .findFirst()
                .filter(t -> t instanceof ImmutableFunctionalTerm  || (t instanceof RDFConstant))
                .map(t -> (NonVariableTerm) t);
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
        Collection<TriplesMap> tripleMaps = getTripleMaps();
        try {
            RDF4JR2RMLMappingManager mm = RDF4JR2RMLMappingManager.getInstance();
            RDF4JGraph rdf4JGraph = mm.exportMappings(tripleMaps);

            Model m = rdf4JGraph.asModel().get();

            m.setNamespace("rr", "http://www.w3.org/ns/r2rml#");
            prefixManager.getPrefixMap()
                    .forEach((key, value) ->
                            m.setNamespace(new SimpleNamespace(
                                    key.substring(0, key.length() - 1), // remove the last ":" from the prefix,
                                    value)));

            WriterConfig settings = new WriterConfig();
            settings.set(BasicWriterSettings.PRETTY_PRINT, true);
            settings.set(BasicWriterSettings.INLINE_BLANK_NODES, true);
            Rio.write(m, os, RDFFormat.TURTLE, settings);
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
