package it.unibz.inf.ontop.sql.parser.exceptions;

import it.unibz.inf.ontop.sql.parser.RelationalExpression;

/**
 * Created by Roman Kontchakov on 10/12/2016.
 */

public class IllegalJoinException extends Exception {
    private final RelationalExpression re1;
    private final RelationalExpression re2;

    public IllegalJoinException(RelationalExpression re1, RelationalExpression re2, String message) {
        super(message);
        this.re1 = re1;
        this.re2 = re2;
    }

    @Override
    public String toString() {
        return super.toString() + " " + re1 + " and " + re2;
    }
}
