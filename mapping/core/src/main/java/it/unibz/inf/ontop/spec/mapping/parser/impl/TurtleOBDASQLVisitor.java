package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import org.apache.commons.rdf.api.RDF;

public class TurtleOBDASQLVisitor extends AbstractTurtleOBDAVisitor implements TurtleOBDAVisitor{

    public TurtleOBDASQLVisitor(TermFactory termFactory,TargetAtomFactory targetAtomFactory, RDF rdfFactory) {
        super(termFactory, targetAtomFactory, rdfFactory);
    }

    @Override
    protected boolean validateAttributeName(String value) {
        if (value.contains(".")) {
            throw new IllegalArgumentException("Fully qualified columns as "+value+" are not accepted.\nPlease, use an alias instead.");
        }
        return true;
    }
}
