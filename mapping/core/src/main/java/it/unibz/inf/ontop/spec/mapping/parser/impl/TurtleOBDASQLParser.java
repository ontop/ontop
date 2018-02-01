package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;

import java.util.Map;

public class TurtleOBDASQLParser extends AbstractTurtleOBDAParser implements TargetQueryParser{

    public TurtleOBDASQLParser(AtomFactory atomFactory, TermFactory termFactory) {
        super(new TurtleOBDASQLVisitor(termFactory, atomFactory), atomFactory, termFactory);
    }

    public TurtleOBDASQLParser(Map<String, String> prefixes, AtomFactory atomFactory, TermFactory termFactory) {
        super(prefixes, new TurtleOBDASQLVisitor(termFactory, atomFactory), atomFactory, termFactory);
    }
}
