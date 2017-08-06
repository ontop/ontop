package it.unibz.inf.ontop.temporal.queryanswering.impl;

import it.unibz.inf.ontop.temporal.mapping.TemporalMappingAxiom;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.TemporalQuery;
import it.unibz.inf.ontop.temporal.queryanswering.TemporalQueryReformulator;

import java.util.List;

public class TemporalQueryReformulatorImpl implements TemporalQueryReformulator {

    private DatalogMTLProgram program;
    List<TemporalMappingAxiom> axioms;

    public TemporalQueryReformulatorImpl(DatalogMTLProgram program, List<TemporalMappingAxiom> axioms) {
        this.program = program;
        this.axioms = axioms;
    }

    @Override
    public String reformulate(TemporalQuery query) {
        return null;
    }
}
