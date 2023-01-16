package it.unibz.inf.ontop.protege.mapping;

import it.unibz.inf.ontop.shaded.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.shaded.com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.apache.commons.rdf.api.IRI;

public interface TriplesMapFactory {

    TargetAtom getTargetAtom(DistinctVariableOnlyDataAtom projectionAtom, ImmutableSubstitution<ImmutableTerm> sub);

    SQLPPSourceQuery getSourceQuery(String query);

    ImmutableList<TargetAtom> getTargetQuery(String target) throws TargetQueryParserException;

    String getTargetRendering(ImmutableList<TargetAtom> targetAtoms);

    IRIConstant getConstantIRI(IRI iri);

    SQLPPMapping createSQLPreProcessedMapping(ImmutableList<SQLPPTriplesMap> newMappings);
}
