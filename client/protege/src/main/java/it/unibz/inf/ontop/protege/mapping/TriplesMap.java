package it.unibz.inf.ontop.protege.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Map;
import java.util.Optional;

public class TriplesMap {

    public enum Status {
        NOT_VALIDATED,
        VALID,
        INVALID
    }

    private final SQLPPTriplesMap sqlppTriplesMap;
    private final TriplesMapFactory factory;

    private Status status;
    private String sqlErrorMessage;
    private ImmutableList<String> invalidPlaceholders = ImmutableList.of();

    public TriplesMap(SQLPPTriplesMap sqlppTriplesMap, TriplesMapFactory factory) {
        this.sqlppTriplesMap = sqlppTriplesMap;
        this.factory = factory;
        this.status = Status.NOT_VALIDATED;
    }

    public TriplesMap(String id, String sqlQuery, ImmutableList<TargetAtom> targetQuery,  TriplesMapFactory factory) {
        this(new OntopNativeSQLPPTriplesMap(id, factory.getSourceQuery(sqlQuery), targetQuery), factory);
    }

    public String getId() { return sqlppTriplesMap.getId(); }

    public String getSqlQuery() { return sqlppTriplesMap.getSourceQuery().getSQL(); }

    public ImmutableList<TargetAtom> getTargetAtoms() { return sqlppTriplesMap.getTargetAtoms(); }

    public String getTargetRendering() {
        return factory.getTargetRendering(sqlppTriplesMap.getTargetAtoms());
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    public String getSqlErrorMessage() { return sqlErrorMessage; }

    public void setSqlErrorMessage(String sqlErrorMessage) { this.sqlErrorMessage = sqlErrorMessage; }

    public ImmutableList<String> getInvalidPlaceholders() { return invalidPlaceholders; }

    public void setInvalidPlaceholders(ImmutableList<String> invalidPlaceholders) { this.invalidPlaceholders = invalidPlaceholders; }



    public SQLPPTriplesMap asSQLPPTriplesMap() { return sqlppTriplesMap; }


    public TriplesMap createDuplicate(String newId) {
        TriplesMap copy = new TriplesMap(
                new OntopNativeSQLPPTriplesMap(
                        newId, sqlppTriplesMap.getSourceQuery(), sqlppTriplesMap.getTargetAtoms()),
                factory);
        copy.status = this.status;
        copy.sqlErrorMessage = this.sqlErrorMessage;
        copy.invalidPlaceholders = this.invalidPlaceholders;
        return copy;
    }

    private TriplesMap updateTargetAtoms(ImmutableList<TargetAtom> targetAtoms) {
        return new TriplesMap(
                new OntopNativeSQLPPTriplesMap(
                        sqlppTriplesMap.getId(), sqlppTriplesMap.getSourceQuery(), targetAtoms),
                factory);
    }

    public TriplesMap renamePredicate(IRI removedIri, IRI newIri) {
        return containsIri(removedIri)
                ? updateTargetAtoms(sqlppTriplesMap.getTargetAtoms().stream()
                .map(a -> containsIri(a, removedIri)
                        ? renamePredicate(a, newIri, factory)
                        : a)
                .collect(ImmutableCollectors.toList()))
                : this; // has not changed
    }

    private static TargetAtom renamePredicate(TargetAtom a, IRI newIri, TriplesMapFactory factory) {
        DistinctVariableOnlyDataAtom projectionAtom = a.getProjectionAtom();
        RDFAtomPredicate predicate = (RDFAtomPredicate) projectionAtom.getPredicate();

        Variable predicateVariable = predicate.getClassIRI(a.getSubstitutedTerms()).isPresent()
                ? predicate.getObject(projectionAtom.getArguments())
                : predicate.getProperty(projectionAtom.getArguments());

        Map.Entry<Variable, ImmutableTerm> newEntry = Maps.immutableEntry(
                predicateVariable,
                factory.getConstantIRI(newIri));  // we build a ground term for the IRI

        return factory.getTargetAtom(
                projectionAtom,
                a.getSubstitution().getImmutableMap().entrySet().stream()
                        .map(e -> e.getKey().equals(predicateVariable) ? newEntry : e)
                        .collect(ImmutableCollectors.toMap()));
    }

    public boolean containsIri(IRI iri) {
        return sqlppTriplesMap.getTargetAtoms().stream()
                .anyMatch(a -> containsIri(a, iri));
    }

    private static boolean containsIri(TargetAtom a, IRI iri) {
        return a.getPredicateIRI()
                .filter(i -> i.equals(iri))
                .isPresent();
    }

    public Optional<TriplesMap> removePredicate(IRI predicateIRI) {

        ImmutableList<TargetAtom> newTargetAtoms = sqlppTriplesMap.getTargetAtoms().stream()
                .filter(a -> !containsIri(a, predicateIRI))
                .collect(ImmutableCollectors.toList());

        if (newTargetAtoms.isEmpty())
            return Optional.empty();

        if (newTargetAtoms.size() < sqlppTriplesMap.getTargetAtoms().size())
            return Optional.of(updateTargetAtoms(newTargetAtoms));

        return Optional.of(this); // has not changed
    }
}


