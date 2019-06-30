package it.unibz.inf.ontop.datalog;


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.util.Collection;
import java.util.Optional;

public interface DatalogProgram2QueryConverter {

    IQ convertDatalogProgram(DatalogProgram queryProgram, ImmutableList<Predicate> tablePredicates,
                             ImmutableList<Variable> signature) throws EmptyQueryException;
}
