package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
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
        return new MappingAssertion(index, query, provenance);
    }

    public MappingAssertionIndex getIndex() {
        if (index == null) {
            RDFAtomPredicate rdfAtomPredicate = Optional.of(query.getProjectionAtom().getPredicate())
                    .filter(p -> p instanceof RDFAtomPredicate)
                    .map(p -> (RDFAtomPredicate) p)
                    .orElseThrow(() -> new MinorOntopInternalBugException("The mapping assertion does not have an RDFAtomPredicate"));

            if (!(query.getTree() instanceof UnaryIQTree))
                throw new MinorOntopInternalBugException("Not a unary tree" + query);
            UnaryIQTree tree = (UnaryIQTree)query.getTree();

            if (!(tree.getRootNode() instanceof ConstructionNode))
                throw new MinorOntopInternalBugException("Not a construction node" + query);
            ConstructionNode node = (ConstructionNode)tree.getRootNode();

            ImmutableList<? extends ImmutableTerm> substitutedArguments =
                    node.getSubstitution().apply(query.getProjectionAtom().getArguments());

            IRI propertyIRI = rdfAtomPredicate.getPropertyIRI(substitutedArguments)
                    .orElseThrow(() -> new MinorOntopInternalBugException("The definition of the predicate is not always a ground term"));

            index = propertyIRI.equals(RDF.TYPE)
                    ? MappingAssertionIndex.ofClass(rdfAtomPredicate, rdfAtomPredicate.getClassIRI(substitutedArguments)
                    .orElseThrow(() -> new MinorOntopInternalBugException("The definition of the predicate is not always a ground term")))
                    : MappingAssertionIndex.ofProperty(rdfAtomPredicate, propertyIRI);
        }
        return index;
    }

}
