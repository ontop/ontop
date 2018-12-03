package it.unibz.inf.ontop.datalog;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Variable;

public class InternalSparqlQuery {

    private final DatalogProgram program;
    private final ImmutableList<Variable> signature;

    public InternalSparqlQuery(DatalogProgram program, ImmutableList<Variable> signature) {
        this.program = program;
        this.signature = signature;
    }

    public DatalogProgram getProgram() {
        return program;
    }

    public ImmutableList<Variable> getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return signature.toString() + "\n" + program.toString();
    }
}
