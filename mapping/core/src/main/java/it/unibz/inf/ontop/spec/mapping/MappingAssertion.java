package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
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
        return getTopNode().getSubstitution().apply(getProjectionAtom().getArguments());
    }

    public Variable getSubject() {
        return getRDFAtomPredicate().getSubject(query.getProjectionAtom().getArguments());
    }

    public Variable getObject() {
        return getRDFAtomPredicate().getObject(query.getProjectionAtom().getArguments());
    }

    public ImmutableList<Variable> updateSubject(Variable newSubject) {
        return getRDFAtomPredicate().updateSubject(query.getProjectionAtom().getArguments(), newSubject);
    }

    public ImmutableList<Variable> updateObject(Variable newObject) {
        return getRDFAtomPredicate().updateObject(query.getProjectionAtom().getArguments(), newObject);
    }

    public ImmutableList<Variable> updateSO(Variable newSubject, Variable newObject) {
        return getRDFAtomPredicate().updateSO(query.getProjectionAtom().getArguments(), newSubject, newObject);
    }

    public ConstructionNode getTopNode() {
       return Optional.of(query.getTree())
                .filter(t -> t.getRootNode() instanceof ConstructionNode)
                .map(IQTree::getRootNode)
                .map(n -> (ConstructionNode) n)
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
