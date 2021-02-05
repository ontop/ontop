package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
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
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class MappingAssertion {

    // lazy
    private MappingAssertionIndex index;
    private final IQ query;
    private final PPMappingAssertionProvenance provenance;

    public MappingAssertion(MappingAssertionIndex index, IQ query, PPMappingAssertionProvenance provenance) {
        this.index = index;
        this.query = query;
        this.provenance = provenance;
    }

    public MappingAssertion(IQ query, PPMappingAssertionProvenance provenance) {
        this.query = query;
        this.provenance = provenance;
    }

    public IQ getQuery() { return query; }

    public PPMappingAssertionProvenance getProvenance() { return provenance; }

    public MappingAssertion copyOf(IQ query) {
        return (query.getProjectionAtom() == this.query.getProjectionAtom())
                ? new MappingAssertion(index, query, provenance)
                : new MappingAssertion(query, provenance);
    }

    public MappingAssertion copyOf(IQTree tree, IntermediateQueryFactory iqFactory) {
        return new MappingAssertion(index, iqFactory.createIQ(query.getProjectionAtom(), tree), provenance);
    }

    public ImmutableSet<Variable> getProjectedVariables() {
        return query.getTree().getVariables();
    }

    public RDFAtomPredicate getRDFAtomPredicate() {
        if (index == null) {
            return Optional.of(query.getProjectionAtom().getPredicate())
                    .filter(p -> p instanceof RDFAtomPredicate)
                    .map(p -> (RDFAtomPredicate) p)
                    .orElseThrow(() -> new MinorOntopInternalBugException("The mapping assertion does not have an RDFAtomPredicate"));
        }
        else
            return index.getPredicate();
    }

    public ImmutableList<ImmutableTerm> getTerms() {
        return getTopSubstitution().apply(getProjectionAtom().getArguments());
    }

    public ImmutableSubstitution<ImmutableTerm> getTopSubstitution() {
       return Optional.of(query.getTree())
                .filter(t -> t.getRootNode() instanceof ConstructionNode)
                .map(IQTree::getRootNode)
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
        if (index == null) {
            RDFAtomPredicate rdfAtomPredicate = getRDFAtomPredicate();
            ImmutableList<? extends ImmutableTerm> substitutedArguments = getTerms();

            IRI propertyIRI = rdfAtomPredicate.getPropertyIRI(substitutedArguments)
                    .orElseThrow(() -> new MinorOntopInternalBugException("The definition of the predicate is not always a ground term " + query));

            index = propertyIRI.equals(RDF.TYPE)
                    ? MappingAssertionIndex.ofClass(rdfAtomPredicate, rdfAtomPredicate.getClassIRI(substitutedArguments)
                    .orElseThrow(() -> new MinorOntopInternalBugException("The definition of the predicate is not always a ground term " + query)))
                    : MappingAssertionIndex.ofProperty(rdfAtomPredicate, propertyIRI);
        }
        return index;
    }

}
