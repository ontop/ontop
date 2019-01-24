package it.unibz.inf.ontop.model.term.functionsymbol;


public enum InequalityLabel {

    LT("<"),
    LTE("<="),
    GT(">"),
    GTE(">=");

    private final String mathString;

    InequalityLabel(String mathString) {
        this.mathString = mathString;
    }

    public String getMathString() {
        return mathString;
    }
}
