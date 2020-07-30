package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import org.apache.commons.rdf.api.RDF;


public class TurtleOBDASQLParser extends AbstractTurtleOBDAParser implements TargetQueryParser {

    @AssistedInject
    private TurtleOBDASQLParser(@Assisted ImmutableMap<String, String> prefixes, TermFactory termFactory,
                                TypeFactory typeFactory, TargetAtomFactory targetAtomFactory, RDF rdfFactory,
                                OntopMappingSettings settings) {
        super(prefixes,
                () -> new TurtleOBDASQLVisitor(termFactory, typeFactory, targetAtomFactory, rdfFactory, settings));
    }
}
