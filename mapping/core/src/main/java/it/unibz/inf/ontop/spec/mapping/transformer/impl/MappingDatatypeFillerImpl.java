package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonVariableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TypeInference;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

public class MappingDatatypeFillerImpl implements MappingDatatypeFiller {

    private final ProvenanceMappingFactory mappingFactory;
    private final OntopMappingSettings settings;

    @Inject
    private MappingDatatypeFillerImpl(ProvenanceMappingFactory mappingFactory, OntopMappingSettings settings) {
        this.mappingFactory = mappingFactory;
        this.settings = settings;
    }

    @Override
    public MappingWithProvenance inferMissingDatatypes(MappingWithProvenance mapping, DBMetadata dbMetadata)
            throws UnknownDatatypeException {

        ImmutableMap.Builder<IQ, PPMappingAssertionProvenance> newProvenanceMapBuilder = ImmutableMap.builder();

        for (Map.Entry<IQ, PPMappingAssertionProvenance> entry : mapping.getProvenanceMap().entrySet()) {
            IQ newIQ = fillDatatypeIfMissing(entry.getKey(), entry.getValue());
            newProvenanceMapBuilder.put(newIQ, entry.getValue());
        }

        return mappingFactory.create(newProvenanceMapBuilder.build(), mapping.getMetadata());
    }

    private IQ fillDatatypeIfMissing(IQ mappingAssertion, PPMappingAssertionProvenance provenance)
            throws UnknownDatatypeException {
        NonVariableTerm objectDefinition = extractObjectDefinition(mappingAssertion);

        TypeInference typeInference = objectDefinition.inferType();

        switch (typeInference.getStatus()) {
            case NOT_DETERMINED:
                return fillMissingDatatype(objectDefinition, mappingAssertion, provenance);
            case NON_FATAL_ERROR:
                throw new MinorOntopInternalBugException("A non-fatal error is not expected in a mapping assertion\n"
                        + mappingAssertion);
            // DETERMINED:
            default:
                return mappingAssertion;
        }
    }

    private NonVariableTerm extractObjectDefinition(IQ mappingAssertion) {
        DistinctVariableOnlyDataAtom projectionAtom = mappingAssertion.getProjectionAtom();

        RDFAtomPredicate rdfAtomPredicate = Optional.of(projectionAtom.getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "An RDFAtomPredicate was expected for the mapping assertion"));

        Variable objectVariable = rdfAtomPredicate.getObject(projectionAtom.getArguments());

        ImmutableList<ImmutableTerm> objectDefinitions = mappingAssertion.getTree().getPossibleVariableDefinitions().stream()
                .map(s -> s.get(objectVariable))
                .distinct()
                .collect(ImmutableCollectors.toList());

        if (objectDefinitions.size() != 1)
            throw new MinorOntopInternalBugException("Only one object definition was expected in a mapping assertion\n"
                    + mappingAssertion);

        ImmutableTerm objectDefinition = objectDefinitions.get(0);

        if (objectDefinition instanceof  NonVariableTerm)
            return (NonVariableTerm) objectDefinition;
        else
            throw new MinorOntopInternalBugException("The object was expected to be defined by a non-variable term\n"
                    + mappingAssertion);
    }

    /**
     * TODO: implement
     */
    private IQ fillMissingDatatype(NonVariableTerm objectDefinition, IQ mappingAssertion,
                                   PPMappingAssertionProvenance provenance) throws UnknownDatatypeException {
        throw new RuntimeException("TODO: implement fillMissingDatatype(). Object definition: " + objectDefinition);
    }
}
