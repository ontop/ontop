package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;

import java.util.Map;

public class TurtleOBDASQLParser extends AbstractTurtleOBDAParser implements TargetQueryParser{
    public TurtleOBDASQLParser() {
        super();
        this.visitor = new TurtleOBDASQLVisitor();
    }

    public TurtleOBDASQLParser(Map<String, String> prefixes) {
        super(prefixes);
        this.visitor = new TurtleOBDASQLVisitor();
    }
}
