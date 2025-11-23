package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.RDFTermFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.Optional;

/**
 * Serializes the RDF term type of a term into the code used by the embedded triple lexical form.
 */
public class SerializeRDFTermTypeFunctionSymbolImpl extends FunctionSymbolImpl implements FunctionSymbol {

    private final DBTermType dbStringType;
    private final RDFTermType tripleTermType;

    public SerializeRDFTermTypeFunctionSymbolImpl(RDFTermType abstractRDFTermType,
                                                  DBTermType dbStringType,
                                                  TypeFactory typeFactory) {
        super("SERIALIZE_RDF_TERM_TYPE", ImmutableList.of(abstractRDFTermType));
        this.dbStringType = dbStringType;
        this.tripleTermType = typeFactory.getRDFStarTripleTermType();
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbStringType));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        Optional<String> serialized = serializeType(newTerm);
        if (serialized.isPresent())
            return termFactory.getDBStringConstant(serialized.get());
        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    private Optional<String> serializeType(ImmutableTerm term) {
        if (term instanceof RDFConstant)
            return Optional.of(serialize(((RDFConstant) term).getType()));

        if (term instanceof ImmutableFunctionalTerm
                && ((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof RDFTermFunctionSymbol) {
            ImmutableTerm typeTerm = ((ImmutableFunctionalTerm) term).getTerm(1);
            if (typeTerm instanceof RDFTermTypeConstant)
                return Optional.of(serialize(((RDFTermTypeConstant) typeTerm).getRDFTermType()));
        }
        return Optional.empty();
    }

    private String serialize(RDFTermType rdfTermType) {
        if (rdfTermType instanceof RDFDatatype) {
            RDFDatatype datatype = (RDFDatatype) rdfTermType;
            return datatype.getLanguageTag()
                    .map(lang -> "@" + lang.getFullString())
                    .orElseGet(() -> datatype.getIRI().getIRIString());
        }

        if (rdfTermType instanceof ObjectRDFType) {
            ObjectRDFType objectType = (ObjectRDFType) rdfTermType;
            return objectType.isBlankNode() ? "BNODE" : "IRI";
        }

        if (rdfTermType.equals(tripleTermType))
            return "TRIPLE";

        return rdfTermType.toString();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }
}
