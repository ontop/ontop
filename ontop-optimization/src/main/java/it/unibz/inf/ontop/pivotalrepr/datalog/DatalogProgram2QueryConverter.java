package it.unibz.inf.ontop.pivotalrepr.datalog;


import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.DatalogProgram;
import it.unibz.inf.ontop.model.Predicate;
import it.unibz.inf.ontop.pivotalrepr.EmptyQueryException;
import it.unibz.inf.ontop.pivotalrepr.ImmutableQueryModifiers;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.pivotalrepr.tools.ExecutorRegistry;

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
