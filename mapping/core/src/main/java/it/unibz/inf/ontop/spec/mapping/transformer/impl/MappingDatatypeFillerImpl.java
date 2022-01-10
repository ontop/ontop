package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingDatatypeFiller;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class MappingDatatypeFillerImpl implements MappingDatatypeFiller {

    private final OntopMappingSettings settings;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TypeFactory typeFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SingleTermTypeExtractor typeExtractor;

    @Inject
    private MappingDatatypeFillerImpl(OntopMappingSettings settings,
                                      CoreSingletons coreSingletons,
                                      SingleTermTypeExtractor typeExtractor) {
        this.settings = settings;
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.typeFactory = coreSingletons.getTypeFactory();
        this.iqFactory = coreSingletons.getIQFactory();
        this.typeExtractor = typeExtractor;
    }

    @Override
    public MappingAssertion transform(MappingAssertion assertion) throws UnknownDatatypeException {
        try {
            return transformMappingAssertion(assertion);
        }
        catch (UnknownDatatypeException e) {
            throw new UnknownDatatypeException(e, "\nMapping assertion:\n" + assertion.getProvenance());
        }
    }

    private MappingAssertion transformMappingAssertion(MappingAssertion assertion) throws UnknownDatatypeException {
        Variable objectVariable = assertion.getRDFAtomPredicate().getObject(assertion.getProjectionAtom().getArguments());
        ImmutableSet<ImmutableTerm> objectDefinitions = extractDefinitions(objectVariable, assertion.getQuery());

        ImmutableSet<Optional<TermTypeInference>> typeInferences = objectDefinitions.stream()
                .map(ImmutableTerm::inferType)
                .collect(ImmutableCollectors.toSet());

        if (typeInferences.size() > 1) {
            throw new MinorOntopInternalBugException("Multiple types found for the object in a mapping assertion\n"
                    + assertion);
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
            return assertion;
        else
            return assertion.copyOf(fillMissingDatatype(objectVariable, assertion), iqFactory);
    }

    private ImmutableSet<ImmutableTerm> extractDefinitions(Variable objectVariable, IQ iq) {

        ImmutableSet<ImmutableTerm> objectDefinitions = iq.getTree().getPossibleVariableDefinitions().stream()
                .map(s -> s.get(objectVariable))
                .collect(ImmutableCollectors.toSet());

        if (!objectDefinitions.stream()
                .allMatch(t -> (t instanceof ImmutableFunctionalTerm) || (t instanceof RDFConstant)))
            throw new MinorOntopInternalBugException("The object was expected to be defined by functional terms " +
                    "or RDF constant only\n"
                    + iq);

        return objectDefinitions;
    }

    private IQTree fillMissingDatatype(Variable objectVariable, MappingAssertion assertion) throws UnknownDatatypeException {
        ImmutableSubstitution<ImmutableTerm> topSubstitution = assertion.getTopSubstitution();

        ImmutableTerm objectLexicalTerm = Optional.ofNullable(topSubstitution.get(objectVariable))
                .filter(t -> (t instanceof ImmutableFunctionalTerm) || (t instanceof RDFConstant))
                .map(t -> (t instanceof ImmutableFunctionalTerm)
                        ? ((ImmutableFunctionalTerm) t).getTerm(0)
                        : termFactory.getRDFTermTypeConstant(((RDFConstant) t).getType()))
                .orElseThrow(() -> new MinorOntopInternalBugException(
                        "The root construction node is not defining the object variable with a functional term " +
                                "or a RDF constant\n" + assertion));

        IQTree childTree = assertion.getTopChild();

        // May throw an UnknownDatatypeException
        RDFDatatype datatype = extractObjectType(objectLexicalTerm, childTree);

        ImmutableTerm objectDefinition = termFactory.getRDFLiteralFunctionalTerm(objectLexicalTerm, datatype);

        ImmutableSubstitution<ImmutableTerm> newSubstitution = topSubstitution
                .filter(v -> !v.equals(objectVariable))
                .union(substitutionFactory.getSubstitution(objectVariable, objectDefinition))
                .orElseThrow(() -> new MinorOntopInternalBugException("Cannot happen"));

        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        assertion.getProjectedVariables(),
                        newSubstitution),
                childTree);
    }

    private RDFDatatype extractObjectType(ImmutableTerm objectLexicalTerm, IQTree subTree) throws UnknownDatatypeException {

        // Only if partially cast
        ImmutableTerm uncastObjectLexicalTerm = DBTypeConversionFunctionSymbol.uncast(objectLexicalTerm);
        Optional<TermType> optionalType = typeExtractor.extractSingleTermType(uncastObjectLexicalTerm, subTree);

        if (optionalType
                .filter(t -> !(t instanceof DBTermType))
                .isPresent()) {
            throw new MinorOntopInternalBugException("Was expecting to get a DBTermType, not a "
                    + optionalType.get().getClass());
        }

        if ((!settings.isDefaultDatatypeInferred())
                && (!optionalType.isPresent())) {
            throw new UnknownDatatypeException(
                    String.format("Could not infer the type of %s and the option \"%s\" is disabled.\n",
                            uncastObjectLexicalTerm, OntopMappingSettings.INFER_DEFAULT_DATATYPE));
        }

        Optional<RDFDatatype> optionalRDFDatatype = optionalType
                .map(t -> (DBTermType) t)
                .flatMap(DBTermType::getNaturalRDFDatatype);

        if ((!settings.isDefaultDatatypeInferred())
                && (!optionalRDFDatatype.isPresent())) {
            throw new UnknownDatatypeException(
                    String.format("Could infer the type %s for %s, " +
                                    "but this type is not mapped to an RDF datatype " +
                                    "and the option \"%s\" is disabled.",
                            optionalType.get(), uncastObjectLexicalTerm, OntopMappingSettings.INFER_DEFAULT_DATATYPE));
        }

        return optionalRDFDatatype
                .orElseGet(typeFactory::getXsdStringDatatype);
    }
}
