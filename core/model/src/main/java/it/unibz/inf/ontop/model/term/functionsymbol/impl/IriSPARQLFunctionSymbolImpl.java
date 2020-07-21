package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


public class IriSPARQLFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {

    private final RDFDatatype xsdStringType;
    private final ObjectRDFType iriType;
    private final IRI baseIRI;

    protected IriSPARQLFunctionSymbolImpl(RDFTermType abstractRDFType, RDFDatatype xsdStringType, ObjectRDFType iriType) {
        this(null, abstractRDFType, xsdStringType, iriType);
    }

    public IriSPARQLFunctionSymbolImpl(@Nullable IRI baseIRI, RDFTermType abstractRDFTermType, RDFDatatype xsdStringType, ObjectRDFType iriType) {
        super("SP_IRI", SPARQL.IRI, ImmutableList.of(abstractRDFTermType));
        this.xsdStringType = xsdStringType;
        this.iriType = iriType;
        this.baseIRI = baseIRI;
    }

    /**
     * TODO: create and use a post-processable dedicated DBIsIRIString, which can be simplified when its argument is a constant
     */
    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                               ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        ImmutableTerm argLexical = subLexicalTerms.get(0);

        if (baseIRI == null)
            return argLexical;

        return termFactory.getIfThenElse(
                termFactory.getIsAExpression(typeTerms.get(0), xsdStringType),
                // xsd:string case
                termFactory.getDBIriStringResolution(baseIRI, argLexical),
                // IRI case
                argLexical);
    }


    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory,
                                            VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(iriType);
    }

    /**
     * Only IRIs and strings
     */
    @Override
    protected ImmutableExpression.Evaluation evaluateInputTypeError(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms,
                                                                    TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm typeTerm = typeTerms.get(0);

        return termFactory.getDisjunction(
                termFactory.getIsAExpression(typeTerm, iriType),
                termFactory.getIsAExpression(typeTerm, xsdStringType))
                .evaluate(variableNullability);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(iriType));
    }

    /**
     * Could be allowed in the future
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
