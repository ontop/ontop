package it.unibz.inf.ontop.temporal.model;

public enum UnaryTemporalOperator {

    BoxMinus("⊟"), BoxPlus("⊞"), DiamondMinus("<->"), DiamondPlus("<+>");

    private final String name;

    UnaryTemporalOperator(String name) {
        this.name = name;
    }
}
