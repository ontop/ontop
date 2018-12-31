package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import org.apache.commons.rdf.api.RDF;

import java.util.Map;

public class TurtleOBDASQLParser extends AbstractTurtleOBDAParser implements TargetQueryParser{

    public TurtleOBDASQLParser(TermFactory termFactory, TargetAtomFactory targetAtomFactory, RDF rdfFactory) {
        super(new TurtleOBDASQLVisitor(termFactory, targetAtomFactory, rdfFactory));
    }

    public TurtleOBDASQLParser(Map<String, String> prefixes, TermFactory termFactory,
                               TargetAtomFactory targetAtomFactory, RDF rdfFactory) {
        super(prefixes, new TurtleOBDASQLVisitor(termFactory, targetAtomFactory, rdfFactory));
    }
}
