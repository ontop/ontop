package it.unibz.inf.ontop.query.unfolding.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.spec.mapping.Mapping.RDFAtomIndexPattern.*;

public class FirstPhaseQueryMergingTransformer extends AbstractMultiPhaseQueryMergingTransformer {

    private boolean areSomeIntensionalNodesRemaining; //(?s ?p ?o) or (?s ?p ?o ?g)

    protected FirstPhaseQueryMergingTransformer(Mapping mapping, VariableGenerator variableGenerator,
                                                CoreSingletons coreSingletons) {
        super(mapping, variableGenerator, coreSingletons);
        this.areSomeIntensionalNodesRemaining = false;
    }

    @Override
    protected Optional<IQ> getDefinition(IntensionalDataNode dataNode) {
        DataAtom<AtomPredicate> atom = dataNode.getProjectionAtom();
        return Optional.of(atom)
                .map(DataAtom::getPredicate)
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .flatMap(p -> getDefinition(p, atom.getArguments()));
    }

    private Optional<IQ> getDefinition(RDFAtomPredicate predicate,
                                       ImmutableList<? extends VariableOrGroundTerm> arguments) {
        return predicate.getPropertyIRI(arguments)
                .map(i -> i.equals(RDF.TYPE)
                        ? getRDFClassDefinition(predicate, arguments)
                        : mapping.getRDFPropertyDefinition(predicate, i))
                .orElseGet(() -> getStarDefinition(predicate, arguments));
    }

    private Optional<IQ> getRDFClassDefinition(RDFAtomPredicate predicate,
                                               ImmutableList<? extends VariableOrGroundTerm> arguments) {
        return predicate.getClassIRI(arguments)
                .map(i -> mapping.getRDFClassDefinition(predicate, i))
                .orElseGet(() -> getStarClassDefinition(predicate, arguments));
    }

    private Optional<IQ> getStarClassDefinition(RDFAtomPredicate predicate,
                                                ImmutableList<? extends VariableOrGroundTerm> arguments) {
        VariableOrGroundTerm subject = predicate.getSubject(arguments);
        if (subject instanceof ObjectConstant) {
            Optional<IQ> definition = getDefinitionCompatibleWithConstant(predicate, SUBJECT_OF_ALL_CLASSES, (ObjectConstant) subject);
            if (definition.isPresent())
                return definition;
            else
                return mapping.getMergedClassDefinitions(predicate);
        }

        // Leave it for next phase
        return Optional.empty();
    }

    private Optional<IQ> getStarDefinition(RDFAtomPredicate predicate, ImmutableList<? extends VariableOrGroundTerm> arguments) {
        VariableOrGroundTerm subject = predicate.getSubject(arguments);

        if (subject instanceof ObjectConstant) {
            return getDefinitionCompatibleWithConstant(predicate, SUBJECT_OF_ALL_DEFINITIONS, (ObjectConstant) subject);
        }

        VariableOrGroundTerm object = predicate.getObject(arguments);
        if (object instanceof ObjectConstant) {
            return getDefinitionCompatibleWithConstant(predicate, OBJECT_OF_ALL_DEFINITIONS, (ObjectConstant) object);
        }

        // Leave it for next phase
        return Optional.empty();
    }

    @Override
    protected IQTree handleIntensionalWithoutDefinition(IntensionalDataNode dataNode) {
        DataAtom<AtomPredicate> projectionAtom = dataNode.getProjectionAtom();
        AtomPredicate atomPredicate = projectionAtom.getPredicate();

        if ((atomPredicate instanceof RDFAtomPredicate)
                && ((RDFAtomPredicate) atomPredicate).getPredicateIRI(projectionAtom.getArguments()).isEmpty()) {
            areSomeIntensionalNodesRemaining = true;
            return dataNode;
        }

        return iqFactory.createEmptyNode(dataNode.getVariables());
    }

    public boolean areSomeIntensionalNodesRemaining() {
        return areSomeIntensionalNodesRemaining;
    }
}
