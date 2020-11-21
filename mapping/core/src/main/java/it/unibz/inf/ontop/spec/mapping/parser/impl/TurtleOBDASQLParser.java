package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.model.vocabulary.*;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.stream.Stream;


public class TurtleOBDASQLParser extends AbstractTurtleOBDAParser implements TargetQueryParser {

    @AssistedInject
    private TurtleOBDASQLParser(@Assisted ImmutableMap<String, String> prefixes, TermFactory termFactory,
                                TypeFactory typeFactory, TargetAtomFactory targetAtomFactory, org.apache.commons.rdf.api.RDF rdfFactory,
                                OntopMappingSettings settings) {
        super(() -> new TurtleOBDASQLVisitor(termFactory, typeFactory, targetAtomFactory, rdfFactory, settings, addStandardPrefixes(prefixes)));
    }

    private static final ImmutableMap<String, String> standardPrefixes = ImmutableMap.of(
            OntopInternal.PREFIX_XSD, XSD.PREFIX,
            OntopInternal.PREFIX_OBDA, Ontop.PREFIX,
            OntopInternal.PREFIX_RDF, RDF.PREFIX,
            OntopInternal.PREFIX_RDFS, RDFS.PREFIX,
            OntopInternal.PREFIX_OWL, OWL.PREFIX);

    private static ImmutableMap<String, String> addStandardPrefixes(ImmutableMap<String, String> prefixes) {
        return Stream.concat(standardPrefixes.entrySet().stream(), prefixes.entrySet().stream())
                .collect(ImmutableCollectors.toMap(
                        e -> e.getKey().substring(0, e.getKey().length() - 1), // remove :
                        Map.Entry::getValue,
                        (v1, v2) -> v2)); // prefer new prefix definitions (or throw an exception if they differ?)
    }

}
