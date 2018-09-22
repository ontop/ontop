package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.model.term.Function;

public class LinearInclusionDependency {
    private final Function head, body;

    public LinearInclusionDependency(Function head, Function body) {
        this.head = head;
        this.body = body;
    }

    public Function getHead() { return head; }

    public Function getBody() { return body; }

    @Override
    public String toString() { return head + " :- " + body; }
}
