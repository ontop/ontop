package it.unibz.inf.ontop.model.term.functionsymbol.impl.geof;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.ReduciblePositiveAritySPARQLFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.*;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class AbstractGeofIRIFunctionSymbolImpl extends ReduciblePositiveAritySPARQLFunctionSymbolImpl {
    private final RDFDatatype xsdAnyUri;

    protected AbstractGeofIRIFunctionSymbolImpl(
            @Nonnull String functionSymbolName,
            @Nonnull IRI functionIRI,
            ImmutableList<TermType> inputTypes,
            RDFDatatype xsdAnyUri) {
        super(functionSymbolName, functionIRI, inputTypes);
        this.xsdAnyUri = xsdAnyUri;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(xsdAnyUri));
    }

    @Override
    protected ImmutableTerm computeLexicalTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, ImmutableTerm returnedTypeTerm) {
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getConversion2RDFLexical(
                dbTypeFactory.getDBBooleanType(),
                computeDBTerm(subLexicalTerms, typeTerms, termFactory),
                xsdAnyUri);
    }

    protected abstract ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms,
                                                   ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory);


    @Override
    protected ImmutableTerm computeTypeTerm(ImmutableList<? extends ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory, VariableNullability variableNullability) {
        return termFactory.getRDFTermTypeConstant(xsdAnyUri);
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
