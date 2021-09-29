package it.unibz.inf.ontop.protege.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.apache.commons.rdf.api.IRI;

public interface TriplesMapFactory {

    TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableMap<Variable, ImmutableTerm> map);

    SQLPPSourceQuery getSourceQuery(String query);

    ImmutableList<TargetAtom> getTargetQuery(String target) throws TargetQueryParserException;

    String getTargetRendering(ImmutableList<TargetAtom> targetAtoms);

    IRIConstant getConstantIRI(IRI iri);

    SQLPPMapping createSQLPreProcessedMapping(ImmutableList<SQLPPTriplesMap> newMappings);
}
