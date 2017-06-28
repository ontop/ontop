package it.unibz.inf.ontop.temporal.model;

public enum ComparisonOperator {

    Less("<"), LessOrEqual("<="), Greater(">"), GreaterOrEqual(">="), Equal("="), NotEqual("!=");

    private final String name;

    ComparisonOperator(String name) {
        this.name = name;
    }
}
