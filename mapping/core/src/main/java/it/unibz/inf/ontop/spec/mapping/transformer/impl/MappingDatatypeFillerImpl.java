package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
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
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
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
    private final TypeFactory typeFactory;
    private final IntermediateQueryFactory iqFactory;
    private final UniqueTermTypeExtractor typeExtractor;

    @Inject
    private MappingDatatypeFillerImpl(ProvenanceMappingFactory mappingFactory, OntopMappingSettings settings,
                                      TermFactory termFactory, SubstitutionFactory substitutionFactory,
                                      TypeFactory typeFactory, IntermediateQueryFactory iqFactory,
                                      UniqueTermTypeExtractor typeExtractor) {
        this.mappingFactory = mappingFactory;
        this.settings = settings;
        this.termFactory = termFactory;
        this.substitutionFactory = substitutionFactory;
        this.typeFactory = typeFactory;
        this.iqFactory = iqFactory;
        this.typeExtractor = typeExtractor;
    }

    @Override
    public MappingWithProvenance transform(MappingWithProvenance mapping)
            throws UnknownDatatypeException {

        ImmutableMap.Builder<IQ, PPMappingAssertionProvenance> newProvenanceMapBuilder = ImmutableMap.builder();

        // no streams because of exception handling
        for (Map.Entry<IQ, PPMappingAssertionProvenance> entry : mapping.getProvenanceMap().entrySet()) {
            IQ newIQ = transformMappingAssertion(entry.getKey(), entry.getValue());
            newProvenanceMapBuilder.put(newIQ, entry.getValue());
        }

        return mappingFactory.create(newProvenanceMapBuilder.build());
    }

    private IQ transformMappingAssertion(IQ mappingAssertion, PPMappingAssertionProvenance provenance)
            throws UnknownDatatypeException {
        Variable objectVariable = extractObjectVariable(mappingAssertion);
        ImmutableSet<ImmutableTerm> objectDefinitions = extractDefinitions(objectVariable, mappingAssertion);

        ImmutableSet<Optional<TermTypeInference>> typeInferences = objectDefinitions.stream()
                .map(ImmutableTerm::inferType)
                .collect(ImmutableCollectors.toSet());

        if (typeInferences.size() > 1) {
            throw new MinorOntopInternalBugException("Multiple types found for the object in a mapping assertion\n"
                    + mappingAssertion);
        }

        Optional<TermTypeInference> optionalTypeInference = typeInferences.stream()
                .findAny()
                .orElseThrow(() -> new MinorOntopInternalBugException("No object definition found"));

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

    private ImmutableSet<ImmutableTerm> extractDefinitions(Variable objectVariable, IQ mappingAssertion) {

        ImmutableSet<ImmutableTerm> objectDefinitions = mappingAssertion.getTree().getPossibleVariableDefinitions().stream()
                .map(s -> s.get(objectVariable))
                .collect(ImmutableCollectors.toSet());

        if (!objectDefinitions.stream()
                .allMatch(t -> (t instanceof ImmutableFunctionalTerm) || (t instanceof RDFConstant)))
            throw new MinorOntopInternalBugException("The object was expected to be defined by functional terms " +
                    "or RDF constant only\n"
                    + mappingAssertion);

        return objectDefinitions;
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
                .filter(t -> (t instanceof ImmutableFunctionalTerm) || (t instanceof RDFConstant))
                .map(t -> (t instanceof ImmutableFunctionalTerm)
                        ? ((ImmutableFunctionalTerm) t).getTerm(0)
                        : termFactory.getRDFTermTypeConstant(((RDFConstant) t).getType()))
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "The root construction node is not defining the object variable with a functional term " +
                                "or a RDF constant\n" + mappingAssertion));

        IQTree childTree = ((UnaryIQTree)mappingAssertion.getTree()).getChild();

        // May throw an UnknownDatatypeException
        RDFDatatype datatype = extractObjectType(objectLexicalTerm, childTree, provenance);

        ImmutableTerm objectDefinition = termFactory.getRDFLiteralFunctionalTerm(objectLexicalTerm, datatype);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = substitutionFactory.getSubstitution(
                Stream.concat(
                        topSubstitution.getImmutableMap().entrySet().stream()
                                .filter(e -> !e.getKey().equals(objectVariable)),
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

        if (optionalType
                .filter(t -> !(t instanceof DBTermType))
                .isPresent()) {
            throw new MinorOntopInternalBugException("Was expecting to get a DBTermType, not a "
                    + optionalType.get().getClass());
        }

        if ((!settings.isDefaultDatatypeInferred())
                && (!optionalType.isPresent())) {
            throw new UnknownDatatypeException(
                    String.format("Could not infer the type of %s and the option \"%s\" is disabled.\n" +
                                    "Mapping assertion:\n%s",
                            uncastObjectLexicalTerm, OntopMappingSettings.INFER_DEFAULT_DATATYPE, provenance));
        }

        Optional<RDFDatatype> optionalRDFDatatype = optionalType
                .map(t -> (DBTermType) t)
                .flatMap(DBTermType::getNaturalRDFDatatype);

        if ((!settings.isDefaultDatatypeInferred())
                && (!optionalRDFDatatype.isPresent())) {
            throw new UnknownDatatypeException(
                    String.format("Could infer the type %s for %s, " +
                                    "but this type is not mapped to an RDF datatype " +
                                    "and the option \"%s\" is disabled.\nMapping assertion:\n%s",
                            optionalType.get(), uncastObjectLexicalTerm, OntopMappingSettings.INFER_DEFAULT_DATATYPE, provenance));
        }

        return optionalRDFDatatype
                .orElseGet(typeFactory::getXsdStringDatatype);
    }

    /**
     * Uncast the term only if it is temporally cast
     */
    private ImmutableTerm uncast(ImmutableTerm term) {
        return (term instanceof ImmutableFunctionalTerm)
                && (((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof DBTypeConversionFunctionSymbol)
                && (((DBTypeConversionFunctionSymbol) ((ImmutableFunctionalTerm) term).getFunctionSymbol()).isTemporary())
                ? ((ImmutableFunctionalTerm) term).getTerm(0)
                : term;
    }
}
