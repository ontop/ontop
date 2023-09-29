package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.substitution.Substitution;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class MappingAssertion {

    // lazy
    private MappingAssertionIndex index;
    private final IQ query;
    private final PPMappingAssertionProvenance provenance;

    private MappingAssertion(MappingAssertionIndex index, IQ query, PPMappingAssertionProvenance provenance) {
        this.index = index;
        this.query = query;
        this.provenance = provenance;
    }

    public MappingAssertion(IQ query, PPMappingAssertionProvenance provenance) {
        this.query = query;
        this.provenance = provenance;
        RDFAtomPredicate rdfAtomPredicate = Optional.of(query.getProjectionAtom().getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .orElseThrow(() -> new MinorOntopInternalBugException("The mapping assertion does not have an RDFAtomPredicate"));

        // some mapping assertions (from rules) have UNION as the top node
        // TODO: revise this
        Optional<ImmutableList<? extends ImmutableTerm>> optionalArgs = Optional.of(query.getTree())
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .map(ConstructionNode::getSubstitution)
                .map(s -> s.apply(query.getProjectionAtom().getArguments()));

        Optional<IRI> propertyIRI = optionalArgs.flatMap(rdfAtomPredicate::getPropertyIRI);

        this.index = propertyIRI.filter(iri -> iri.equals(RDF.TYPE)).isPresent()
                ? MappingAssertionIndex.ofClass(rdfAtomPredicate, optionalArgs.flatMap(rdfAtomPredicate::getClassIRI))
                : MappingAssertionIndex.ofProperty(rdfAtomPredicate, propertyIRI);
    }

    public IQ getQuery() { return query; }

    public PPMappingAssertionProvenance getProvenance() { return provenance; }

    public MappingAssertion copyOf(IQ query) {
        return query.getTree().getRootNode().equals(this.query.getTree().getRootNode())
                ? new MappingAssertion(index, query, provenance)
                : new MappingAssertion(query, provenance);
    }

    public MappingAssertion copyOf(IQTree tree, IntermediateQueryFactory iqFactory) {
        return query.getTree().getRootNode().equals(tree.getRootNode())
                ? new MappingAssertion(index, iqFactory.createIQ(query.getProjectionAtom(), tree), provenance)
                : new MappingAssertion(iqFactory.createIQ(query.getProjectionAtom(), tree), provenance);
    }

    public ImmutableSet<Variable> getProjectedVariables() {
        return query.getTree().getVariables();
    }

    public RDFAtomPredicate getRDFAtomPredicate() {
        return index.getPredicate();
    }

    public ImmutableList<ImmutableTerm> getTerms() {
        return getTopSubstitution().apply(getProjectionAtom().getArguments());
    }

    public Substitution<ImmutableTerm> getTopSubstitution() {
        return Optional.of(query.getTree())
                .map(IQTree::getRootNode)
                .filter(n -> n instanceof ConstructionNode)
                .map(n -> (ConstructionNode) n)
                .map(ConstructionNode::getSubstitution)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "The mapping assertion was expecting to start with a construction node\n" + query));
    }

    public IQTree getTopChild() {
        return ((UnaryIQTree)query.getTree()).getChild();
    }

    public DistinctVariableOnlyDataAtom getProjectionAtom() {
        return query.getProjectionAtom();
    }

    public MappingAssertionIndex getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return query.toString();
    }

    public static class NoGroundPredicateOntopInternalBugException extends OntopInternalBugException {

        protected NoGroundPredicateOntopInternalBugException(String message) {
            super(message);
        }
    }

}
