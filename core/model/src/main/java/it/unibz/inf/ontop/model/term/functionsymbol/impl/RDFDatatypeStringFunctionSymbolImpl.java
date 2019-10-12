package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.*;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;

public class RDFDatatypeStringFunctionSymbolImpl extends FunctionSymbolImpl {

    private final DBTermType dbStringType;

    protected RDFDatatypeStringFunctionSymbolImpl(MetaRDFTermType metaRDFType, DBTermType dbStringType) {
        super("RDF_DATATYPE_STR", ImmutableList.of(metaRDFType));
        this.dbStringType = dbStringType;
    }

    /**
     * In case of non-literal RDF types
     */
    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbStringType));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof RDFTermTypeConstant) {
            RDFTermType termType = ((RDFTermTypeConstant) newTerm).getRDFTermType();
            return Optional.of(termType)
                    .filter(t -> t instanceof RDFDatatype)
                    .map(t -> ((RDFDatatype) t).getIRI())
                    .map(IRI::getIRIString)
                    .map(s -> (Constant) termFactory.getDBStringConstant(s))
                    .orElseGet(termFactory::getNullConstant);
        }
        return tryToLiftMagicNumbers(newTerms, termFactory, variableNullability, false)
                .orElseGet(() -> super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }
}
