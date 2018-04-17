package it.unibz.inf.ontop.datalog;


import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.util.Collection;
import java.util.Optional;

public interface DatalogProgram2QueryConverter {

    IQ convertDatalogProgram(DatalogProgram queryProgram,
                             Collection<Predicate> tablePredicates)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException, EmptyQueryException;

    Optional<IQ> convertDatalogDefinitions(Predicate datalogAtomPredicate,
                                           Multimap<Predicate, CQIE> datalogRuleIndex,
                                           Collection<Predicate> tablePredicates,
                                           Optional<ImmutableQueryModifiers> optionalModifiers)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException;

    Optional<IQ> convertDatalogDefinitions(Collection<CQIE> atomDefinitions,
                                           Collection<Predicate> tablePredicates,
                                           Optional<ImmutableQueryModifiers> optionalModifiers)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException;
}
