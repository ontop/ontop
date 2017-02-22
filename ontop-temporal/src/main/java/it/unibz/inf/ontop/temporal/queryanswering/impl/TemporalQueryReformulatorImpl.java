package it.unibz.inf.ontop.temporal.queryanswering.impl;

import it.unibz.inf.ontop.sql.RDBMSMappingAxiom;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.TemporalQuery;
import it.unibz.inf.ontop.temporal.queryanswering.TemporalQueryReformulator;

import java.util.List;

public class TemporalQueryReformulatorImpl implements TemporalQueryReformulator {

    private DatalogMTLProgram program;
    List<RDBMSMappingAxiom> axioms;

    public TemporalQueryReformulatorImpl(DatalogMTLProgram program, List<RDBMSMappingAxiom> axioms) {
        this.program = program;
        this.axioms = axioms;
    }

    @Override
    public String reformulate(TemporalQuery query) {
        return null;
    }
}
