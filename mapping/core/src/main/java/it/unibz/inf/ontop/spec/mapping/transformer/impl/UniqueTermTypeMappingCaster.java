package it.unibz.inf.ontop.spec.mapping.transformer.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCaster;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation making use of the UniqueTermTypeExtractor
 *   (and thus sharing its assumptions on how the source query of the mapping assertion is typed)
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class UniqueTermTypeMappingCaster implements MappingCaster {

    private final FunctionSymbolFactory functionSymbolFactory;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final UniqueTermTypeExtractor typeExtractor;
    private final TermFactory termFactory;
    private final DBTermType dBStringType;

    @Inject
    private UniqueTermTypeMappingCaster(FunctionSymbolFactory functionSymbolFactory,
                                        CoreSingletons coreSingletons,
                                        UniqueTermTypeExtractor typeExtractor) {
        this.functionSymbolFactory = functionSymbolFactory;
        this.iqFactory = coreSingletons.getIQFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.typeExtractor = typeExtractor;
        this.termFactory = coreSingletons.getTermFactory();
        this.dBStringType = coreSingletons.getTypeFactory().getDBTypeFactory().getDBStringType();
    }

    @Override
    public ImmutableList<MappingAssertion> transform(ImmutableList<MappingAssertion> mapping) {
        return mapping.stream()
                .map(this::transformMappingAssertion)
                .collect(ImmutableCollectors.toList());
    }

    private MappingAssertion transformMappingAssertion(MappingAssertion assertion) {
        ImmutableSubstitution<ImmutableTerm> topSubstitution = assertion.getTopSubstitution();
        ImmutableSet<Variable> projectedVariables = assertion.getQuery().getTree().getVariables();

        RDFTermFunctionSymbol rdfTermFunctionSymbol = functionSymbolFactory.getRDFTermFunctionSymbol();

        if (!projectedVariables.stream()
                .map(topSubstitution::apply)
                .allMatch(t -> ((t instanceof ImmutableFunctionalTerm) &&
                        ((ImmutableFunctionalTerm) t).getFunctionSymbol().equals(rdfTermFunctionSymbol))
                        || (t instanceof RDFConstant))) {
            throw new MinorOntopInternalBugException(
                    "The root construction node is not defining all the variables with a RDF functional or constant term\n"
                            + assertion);
        }
        IQTree childTree = assertion.getTopChild();

        ImmutableSubstitution<ImmutableTerm> newSubstitution = transformTopSubstitution(
                topSubstitution.getImmutableMap(), childTree);

        ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables, newSubstitution);

        return assertion.copyOf(iqFactory.createIQ(
                assertion.getProjectionAtom(),
                iqFactory.createUnaryIQTree(newRootNode, childTree)));
    }

    /**
     * TODO: explain why all the transformation is done in the top construction node substitution
     */
    private ImmutableSubstitution<ImmutableTerm> transformTopSubstitution(
            ImmutableMap<Variable, ImmutableTerm> substitutionMap,
            IQTree childTree) {

        return substitutionFactory.getSubstitution(
                substitutionMap.entrySet().stream()
                        .collect(ImmutableCollectors.toMap(
                                Map.Entry::getKey,
                                e -> (ImmutableTerm) transformDefinition(e.getValue(), childTree)
                        )));
    }

    private ImmutableTerm transformDefinition(ImmutableTerm rdfTerm, IQTree childTree) {
        if (rdfTerm instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm rdfTermDefinition = (ImmutableFunctionalTerm) rdfTerm;
            ImmutableTerm uncastLexicalTerm = uncast(rdfTermDefinition.getTerm(0));
            ImmutableTerm rdfTypeTerm = rdfTermDefinition.getTerm(1);

            Optional<DBTermType> dbType = extractInputDBType(uncastLexicalTerm, childTree);
            RDFTermType rdfType = extractRDFTermType(rdfTypeTerm);

            ImmutableTerm newLexicalTerm = transformNestedTemporaryCasts(
                    transformTopOfLexicalTerm(uncastLexicalTerm, dbType, rdfType), childTree);

            return termFactory.getRDFFunctionalTerm(newLexicalTerm, rdfTypeTerm);
        }
        else if (rdfTerm instanceof RDFConstant) {
            return rdfTerm;
        }
        else
            throw new IllegalArgumentException("Was expecting an ImmutableFunctionalTerm or a Constant");
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

    private Optional<DBTermType> extractInputDBType(ImmutableTerm uncastLexicalTerm, IQTree childTree) {
        Optional<TermType> type = typeExtractor.extractUniqueTermType(uncastLexicalTerm, childTree);
        if (type
                .filter(t -> !(t instanceof DBTermType))
                .isPresent()) {
            throw new MinorOntopInternalBugException("Was expecting to get a DBTermType, not a "
                    + type.get().getClass() + " (" + type.get() + ")");
        }
        return type
                .map(t -> (DBTermType)t);
    }

    private RDFTermType extractRDFTermType(ImmutableTerm rdfTypeTerm) {
        if (rdfTypeTerm instanceof RDFTermTypeConstant) {
            return ((RDFTermTypeConstant) rdfTypeTerm).getRDFTermType();
        }
        throw new MinorOntopInternalBugException("Was expecting a RDFTermTypeConstant in the RDF term function, " +
                    "not " + rdfTypeTerm);
    }

    private ImmutableTerm transformTopOfLexicalTerm(ImmutableTerm uncastLexicalTerm, Optional<DBTermType> dbType,
                                                  RDFTermType rdfType) {

        return dbType
                .map(i ->
                        i.equals(dBStringType)
                                ? uncastLexicalTerm
                                : termFactory.getConversion2RDFLexical(i, uncastLexicalTerm, rdfType))
                .orElseGet(() -> termFactory.getDBCastFunctionalTerm(dBStringType, uncastLexicalTerm));
    }

    /**
     * For dealing with arguments of templates (which are always cast as strings)
     *
     * Either remove the casting function (if not needed), replace it by a casting function
     * knowing its input type or, in the "worst" case, by a replace it by a casting function
     * NOT knowing the input type.
     *
     */
    private ImmutableTerm transformNestedTemporaryCasts(ImmutableTerm term, IQTree childTree) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            FunctionSymbol functionSymbol = functionalTerm.getFunctionSymbol();

            if ((functionSymbol instanceof DBTypeConversionFunctionSymbol) &&
                    ((DBTypeConversionFunctionSymbol) functionSymbol).isTemporary()) {
                DBTypeConversionFunctionSymbol castFunctionSymbol = (DBTypeConversionFunctionSymbol) functionSymbol;
                if (functionSymbol.getArity() != 1)
                    throw new MinorOntopInternalBugException("The casting function was expected to be unary");

                // Optimization: recursion does not seem to be needed
                ImmutableTerm childTerm = functionalTerm.getTerm(0);

                Optional<DBTermType> inputType = extractInputDBType(childTerm, childTree);

                return Optional.of(castFunctionSymbol.getTargetType())
                        .filter(targetType -> !inputType.filter(targetType::equals).isPresent())
                        .map(targetType -> inputType
                                .map(i -> termFactory.getDBCastFunctionalTerm(i, targetType, childTerm))
                                .orElseGet(() -> termFactory.getDBCastFunctionalTerm(targetType, childTerm)))
                        .map(t -> (ImmutableTerm) t)
                        .orElse(childTerm);
            }
            else {
                // Recursive
                return termFactory.getImmutableFunctionalTerm(functionSymbol,
                        functionalTerm.getTerms().stream()
                                .map(t -> transformNestedTemporaryCasts(t, childTree))
                                .collect(ImmutableCollectors.toList()));
            }
        }
        else
            return term;
    }
}
