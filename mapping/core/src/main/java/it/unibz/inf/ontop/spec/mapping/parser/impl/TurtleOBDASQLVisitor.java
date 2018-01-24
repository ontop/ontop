package it.unibz.inf.ontop.spec.mapping.parser.impl;

public class TurtleOBDASQLVisitor extends AbstractTurtleOBDAVisitor implements TurtleOBDAVisitor{

    @Override
    protected boolean validateAttributeName(String value) {
        if (value.contains(".")) {
            throw new IllegalArgumentException("Fully qualified columns are not accepted.");
        }
        return true;
    }
}
