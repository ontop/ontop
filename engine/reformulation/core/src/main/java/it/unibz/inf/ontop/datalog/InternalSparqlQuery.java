package it.unibz.inf.ontop.datalog;

import it.unibz.inf.ontop.datalog.DatalogProgram;

import java.util.List;

public class InternalSparqlQuery {

    private final DatalogProgram program;
    private final List<String> signature;

    public InternalSparqlQuery(DatalogProgram program, List<String> signature) {
        this.program = program;
        this.signature = signature;
    }

    public DatalogProgram getProgram() {
        return program;
    }

    public List<String> getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return signature.toString() + "\n" + program.toString();
    }
}
