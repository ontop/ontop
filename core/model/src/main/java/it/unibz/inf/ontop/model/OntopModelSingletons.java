package it.unibz.inf.ontop.model;

import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.impl.DatalogFactoryImpl;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.impl.AtomFactoryImpl;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.impl.TypeFactoryImpl;
import it.unibz.inf.ontop.model.term.impl.TermFactoryImpl;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.SubstitutionFactoryImpl;

/**
 * Ubiquitously used in the code
 */
public interface OntopModelSingletons {

    TypeFactory TYPE_FACTORY = TypeFactoryImpl.getInstance();
    TermFactory TERM_FACTORY = TermFactoryImpl.getInstance();
    AtomFactory ATOM_FACTORY = AtomFactoryImpl.getInstance();
    SubstitutionFactory SUBSTITUTION_FACTORY = SubstitutionFactoryImpl.getInstance();
    DatalogFactory DATALOG_FACTORY = DatalogFactoryImpl.getInstance();
}
