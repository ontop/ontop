package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.ProvenanceMappingFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.PartiallyDefinedCastFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.spec.mapping.MappingWithProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MappingDatatypeFillerImpl implements MappingDatatypeFiller {

    private final ProvenanceMappingFactory mappingFactory;
    private final OntopMappingSettings settings;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;
    private final UniqueTermTypeExtractor typeExtractor;

    @Inject
    private MappingDatatypeFillerImpl(ProvenanceMappingFactory mappingFactory, OntopMappingSettings settings,
                                      TermFactory termFactory, SubstitutionFactory substitutionFactory,
                                      IntermediateQueryFactory iqFactory, UniqueTermTypeExtractor typeExtractor) {
        this.mappingFactory = mappingFactory;
        this.settings = settings;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.iqFactory = iqFactory;
        this.typeExtractor = typeExtractor;
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
        Variable objectVariable = extractObjectVariable(mappingAssertion);
        ImmutableSet<ImmutableFunctionalTerm> objectDefinitions = extractDefinitions(objectVariable, mappingAssertion);

        ImmutableSet<Optional<TermTypeInference>> typeInferences = objectDefinitions.stream()
                .map(ImmutableFunctionalTerm::inferType)
                .collect(ImmutableCollectors.toSet());

        if (typeInferences.size() > 1) {
            throw new MinorOntopInternalBugException("Multiple types found for the object in a mapping assertion\n"
                    + mappingAssertion);
        }

        Optional<TermTypeInference> optionalTypeInference = typeInferences.stream()
                .findAny()
                .orElseThrow(() -> new MinorOntopInternalBugException("No object definition found"));

        if (optionalTypeInference
                .filter(TermTypeInference::isNonFatalError)
                .isPresent())
            throw new MinorOntopInternalBugException("A non-fatal error is not expected in a mapping assertion\n"
                    + mappingAssertion);

        /*
         * If the datatype is abstract --> we consider it as missing
         */
        if (optionalTypeInference
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> !t.isAbstract())
                .isPresent())
            return mappingAssertion;
        else
            return fillMissingDatatype(objectVariable, mappingAssertion, provenance);
    }

    private ImmutableSet<ImmutableFunctionalTerm> extractDefinitions(Variable objectVariable, IQ mappingAssertion) {

        ImmutableSet<? extends ImmutableTerm> objectDefinitions = mappingAssertion.getTree().getPossibleVariableDefinitions().stream()
                .map(s -> s.get(objectVariable))
                .collect(ImmutableCollectors.toSet());

        if (objectDefinitions.stream()
                .allMatch(t -> t instanceof ImmutableFunctionalTerm))
            return (ImmutableSet<ImmutableFunctionalTerm>) objectDefinitions;
        else
            throw new MinorOntopInternalBugException("The object was expected to be defined by functional terms only\n"
                    + mappingAssertion);
    }

    Variable extractObjectVariable(IQ mappingAssertion) {
        DistinctVariableOnlyDataAtom projectionAtom = mappingAssertion.getProjectionAtom();

        RDFAtomPredicate rdfAtomPredicate = Optional.of(projectionAtom.getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "An RDFAtomPredicate was expected for the mapping assertion"));

        return rdfAtomPredicate.getObject(projectionAtom.getArguments());
    }

    private IQ fillMissingDatatype(Variable objectVariable, IQ mappingAssertion,
                                   PPMappingAssertionProvenance provenance) throws UnknownDatatypeException {
        ImmutableSubstitution<ImmutableTerm> topSubstitution = Optional.of(mappingAssertion.getTree())
                .filter(t -> t.getRootNode() instanceof ConstructionNode)
                .map(IQTree::getRootNode)
                .map(n -> (ConstructionNode) n)
                .map(ConstructionNode::getSubstitution)
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "The mapping assertion was expecting to start with a construction node\n" + mappingAssertion));

        ImmutableTerm objectLexicalTerm = Optional.ofNullable(topSubstitution.get(objectVariable))
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> ((ImmutableFunctionalTerm) t).getTerm(0))
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "The root construction node is not defining the object variable with a functional term\n" + mappingAssertion));

        IQTree childTree = ((UnaryIQTree)mappingAssertion.getTree()).getChild();

        // May throw an UnknownDatatypeException
        RDFDatatype datatype = extractObjectType(objectLexicalTerm, childTree, provenance);

        ImmutableTerm objectDefinition = termFactory.getRDFLiteralFunctionalTerm(objectLexicalTerm, datatype);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(
                Stream.concat(
                        topSubstitution.getImmutableMap().entrySet().stream()
                                .filter(e -> e.getKey().equals(objectVariable)),
                        Stream.of(Maps.immutableEntry(objectVariable, objectDefinition)))
                        .collect(ImmutableCollectors.toMap()));

        IQTree newTree = iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        mappingAssertion.getProjectionAtom().getVariables(),
                        newSubstitution),
                childTree);

        return iqFactory.createIQ(mappingAssertion.getProjectionAtom(), newTree);
    }

    private RDFDatatype extractObjectType(ImmutableTerm objectLexicalTerm, IQTree subTree,
                                          PPMappingAssertionProvenance provenance) throws UnknownDatatypeException {

        // Only if partially cast
        ImmutableTerm uncastObjectLexicalTerm = uncast(objectLexicalTerm);
        Optional<TermType> optionalType = typeExtractor.extractUniqueTermType(uncastObjectLexicalTerm, subTree);

        throw new RuntimeException("TODO: deduce the RDF datatype from the DB type");
    }

    /**
     * Uncast the term if it is only partially cast (to string)
     */
    private ImmutableTerm uncast(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && ((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof PartiallyDefinedCastFunctionSymbol
                ? ((ImmutableFunctionalTerm) term).getTerm(0)
                : term;
    }
}
