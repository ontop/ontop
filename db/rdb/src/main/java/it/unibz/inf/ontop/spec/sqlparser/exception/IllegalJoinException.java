package it.unibz.inf.ontop.spec.sqlparser.exception;

import it.unibz.inf.ontop.spec.sqlparser.RAExpressionAttributes;

/**
 * Created by Roman Kontchakov on 10/12/2016.
 */

public class IllegalJoinException extends Exception {
    private final RAExpressionAttributes re1;
    private final RAExpressionAttributes re2;

    public IllegalJoinException(RAExpressionAttributes re1, RAExpressionAttributes re2, String message) {
        super(message);
        this.re1 = re1;
        this.re2 = re2;
    }

    @Override
    public String toString() {
        return super.toString() + " " + re1 + " and " + re2;
    }
}
