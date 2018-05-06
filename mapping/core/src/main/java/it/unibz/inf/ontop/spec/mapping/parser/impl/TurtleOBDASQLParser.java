package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;

import java.util.Map;

public class TurtleOBDASQLParser extends AbstractTurtleOBDAParser implements TargetQueryParser{

    public TurtleOBDASQLParser(TermFactory termFactory, TargetAtomFactory targetAtomFactory) {
        super(new TurtleOBDASQLVisitor(termFactory, targetAtomFactory));
    }

    public TurtleOBDASQLParser(Map<String, String> prefixes, TermFactory termFactory,
                               TargetAtomFactory targetAtomFactory) {
        super(prefixes, new TurtleOBDASQLVisitor(termFactory, targetAtomFactory));
    }
}
