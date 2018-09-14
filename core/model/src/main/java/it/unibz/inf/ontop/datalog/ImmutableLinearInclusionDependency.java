package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;

public class ImmutableLinearInclusionDependency<P extends AtomPredicate> {
    private final DataAtom<P> head, body;

    public ImmutableLinearInclusionDependency(DataAtom<P> head, DataAtom<P> body) {
        this.head = head;
        this.body = body;
    }

    public DataAtom<P> getHead() { return head; }

    public DataAtom<P> getBody() { return body; }

    @Override
    public String toString() { return head + " :- " + body; }
}
