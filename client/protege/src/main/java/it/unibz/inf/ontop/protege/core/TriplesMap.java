package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.serializer.impl.TargetQueryRenderer;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
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
    private final TriplesMapCollection triplesMapCollection;
    private Status status;
    private String sqlErrorMessage;
    private ImmutableList<String> invalidPlaceholders = ImmutableList.of();

    public TriplesMap(SQLPPTriplesMap sqlppTriplesMap, TriplesMapCollection triplesMapCollection) {
        this.sqlppTriplesMap = sqlppTriplesMap;
        this.triplesMapCollection = triplesMapCollection;
        this.status = Status.NOT_VALIDATED;
    }

    public TriplesMap(String id, String sqlQuery, ImmutableList<TargetAtom> targetQuery, TriplesMapCollection triplesMapCollection) {
        this(new OntopNativeSQLPPTriplesMap(id, triplesMapCollection.sourceQueryFactory.createSourceQuery(sqlQuery), targetQuery), triplesMapCollection);
    }

    public String getId() { return sqlppTriplesMap.getId(); }

    public String getSqlQuery() { return sqlppTriplesMap.getSourceQuery().getSQL(); }

    public ImmutableList<TargetAtom> getTargetAtoms() { return sqlppTriplesMap.getTargetAtoms(); }

    public String getTargetRendering() {
        TargetQueryRenderer targetQueryRenderer = new TargetQueryRenderer(triplesMapCollection.getMutablePrefixManager());
        return targetQueryRenderer.encode(sqlppTriplesMap.getTargetAtoms());
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    public String getSqlErrorMessage() { return sqlErrorMessage; }

    public void setSqlErrorMessage(String sqlErrorMessage) { this.sqlErrorMessage = sqlErrorMessage; }

    public ImmutableList<String> getInvalidPlaceholders() { return invalidPlaceholders; }

    public void setInvalidPlaceholders(ImmutableList<String> invalidPlaceholders) { this.invalidPlaceholders = invalidPlaceholders; }



    SQLPPTriplesMap asSQLPPTriplesMap() { return sqlppTriplesMap; }


    TriplesMap createDuplicate(String newId) {
        TriplesMap copy = new TriplesMap(
                new OntopNativeSQLPPTriplesMap(
                        newId, sqlppTriplesMap.getSourceQuery(), sqlppTriplesMap.getTargetAtoms()),
                triplesMapCollection);
        copy.status = this.status;
        copy.sqlErrorMessage = this.sqlErrorMessage;
        copy.invalidPlaceholders = this.invalidPlaceholders;
        return copy;
    }

    private TriplesMap updateTargetAtoms(ImmutableList<TargetAtom> targetAtoms) {
        return new TriplesMap(
                new OntopNativeSQLPPTriplesMap(
                        sqlppTriplesMap.getId(), sqlppTriplesMap.getSourceQuery(), targetAtoms),
                triplesMapCollection);
    }


    private static TargetAtom renamePredicate(TargetAtom a, ImmutableTerm newIri, TargetAtomFactory targetAtomFactory, SubstitutionFactory substitutionFactory) {
        DistinctVariableOnlyDataAtom projectionAtom = a.getProjectionAtom();
        RDFAtomPredicate predicate = (RDFAtomPredicate) projectionAtom.getPredicate();

        Variable predicateVariable = predicate.getClassIRI(a.getSubstitutedTerms()).isPresent()
                ? predicate.getObject(projectionAtom.getArguments())
                : predicate.getProperty(projectionAtom.getArguments());

        Map.Entry<Variable, ImmutableTerm> newEntry = Maps.immutableEntry(
                predicateVariable,
                newIri);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(
                a.getSubstitution().getImmutableMap().entrySet().stream()
                        .map(e -> e.getKey().equals(predicateVariable) ? newEntry : e)
                        .collect(ImmutableCollectors.toMap()));

        return targetAtomFactory.getTargetAtom(projectionAtom, newSubstitution);
    }

    public TriplesMap renamePredicate(IRI removedIri, IRIConstant newIri) {
        return containsIri(removedIri)
                ? updateTargetAtoms(sqlppTriplesMap.getTargetAtoms().stream()
                        .map(a -> containsIri(a, removedIri)
                                ? renamePredicate(a, newIri, triplesMapCollection.targetAtomFactory, triplesMapCollection.substitutionFactory)
                                : a)
                .collect(ImmutableCollectors.toList()))
                : this; // has not changed
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


