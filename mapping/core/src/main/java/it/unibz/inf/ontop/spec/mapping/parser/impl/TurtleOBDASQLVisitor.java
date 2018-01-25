package it.unibz.inf.ontop.spec.mapping.parser.impl;

public class TurtleOBDASQLVisitor extends AbstractTurtleOBDAVisitor implements TurtleOBDAVisitor{

    @Override
    protected boolean validateAttributeName(String value) {
        if (value.contains(".")) {
            throw new IllegalArgumentException("Fully qualified columns as "+value+" are not accepted.\nPlease, use an alias instead.");
        }
        return true;
    }
}
