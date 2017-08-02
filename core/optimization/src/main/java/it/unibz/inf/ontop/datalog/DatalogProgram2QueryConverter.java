package it.unibz.inf.ontop.datalog;


import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ImmutableQueryModifiers;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

import java.util.Collection;
import java.util.Optional;

public interface DatalogProgram2QueryConverter {
    IntermediateQuery convertDatalogProgram(DBMetadata dbMetadata,
                                            DatalogProgram queryProgram,
                                            Collection<Predicate> tablePredicates,
                                            ExecutorRegistry executorRegistry)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException, EmptyQueryException;

    Optional<IntermediateQuery> convertDatalogDefinitions(DBMetadata dbMetadata,
                                                          Predicate datalogAtomPredicate,
                                                          Multimap<Predicate, CQIE> datalogRuleIndex,
                                                          Collection<Predicate> tablePredicates,
                                                          Optional<ImmutableQueryModifiers> optionalModifiers,
                                                          ExecutorRegistry executorRegistry)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException;
}
