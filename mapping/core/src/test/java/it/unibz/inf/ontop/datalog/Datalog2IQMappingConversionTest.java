package it.unibz.inf.ontop.datalog;


import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.impl.DatalogProgram2QueryConverterImpl;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.DBMetadataTestingTools;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.junit.Test;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.ATOM_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.utils.MappingTestingTools.*;

public class Datalog2IQMappingConversionTest {

    private static final Variable X = DATA_FACTORY.getVariable("x");
    private static final Variable Y = DATA_FACTORY.getVariable("y");
    private static final Variable Z = DATA_FACTORY.getVariable("z");

    public static final DBMetadata EMPTY_METADATA = DBMetadataTestingTools.createDummyMetadata();
    private static AtomPredicate ANS1_AR3_DATALOG_PREDICATE = ATOM_FACTORY.getAtomPredicate("ans1", 3);
    private static AtomPredicate ANSSQ1_AR3_DATALOG_PREDICATE = ATOM_FACTORY.getAtomPredicate("ansSQ1", 3);

    @Test
    public void testUnion() throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {

        ImmutableList<Variable> variables = ImmutableList.of(X, Y, Z);
        ImmutableList<String> extPredicates = ImmutableList.of("TABLE1", "TABLE2");
        DataAtom mainQueryHead = ATOM_FACTORY.getDataAtom(ANS1_AR3_DATALOG_PREDICATE, variables);
        DataAtom sbQueryHead = ATOM_FACTORY.getDataAtom(ANSSQ1_AR3_DATALOG_PREDICATE, variables);
        ImmutableList<DataAtom> bodyAtoms = extPredicates.stream()
                .map(p -> ATOM_FACTORY.getDataAtom(
                        ATOM_FACTORY.getAtomPredicate(
                                p,
                                3
                        ),
                        variables
                )).collect(ImmutableCollectors.toList());

        ImmutableList<CQIE> rules = ImmutableList.<CQIE>builder()
                .add(DATALOG_FACTORY.getCQIE(mainQueryHead, sbQueryHead))
                .addAll(bodyAtoms.stream()
                        .map(a -> DATALOG_FACTORY.getCQIE(sbQueryHead, a))
                        .collect(ImmutableCollectors.toList())
                ).build();

        UriTemplateMatcher.create(Stream.of(mainQueryHead));

        Mapping mapping = DATALOG_2_QUERY_MAPPING_CONVERTER.convertMappingRules(
                rules,
                EMPTY_METADATA,
                EXECUTOR_REGISTRY,
                EMPTY_MAPPING_METADATA
        );
    }
}
