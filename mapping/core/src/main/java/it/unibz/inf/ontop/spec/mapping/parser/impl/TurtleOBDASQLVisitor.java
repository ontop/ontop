package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;

public class TurtleOBDASQLVisitor extends AbstractTurtleOBDAVisitor implements TurtleOBDAVisitor{

    public TurtleOBDASQLVisitor(TermFactory termFactory, AtomFactory atomFactory) {
        super(termFactory, atomFactory);
    }

    @Override
    protected boolean validateAttributeName(String value) {
        if (value.contains(".")) {
            throw new IllegalArgumentException("Fully qualified columns as "+value+" are not accepted.\nPlease, use an alias instead.");
        }
        return true;
    }
}
