package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import io.mikael.urlbuilder.util.Decoder;
import io.mikael.urlbuilder.util.Encoder;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBTypeConversionFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class AbstractR2RMLSafeIRIEncodeFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    private final Encoder iriEncoder;
    private final Decoder iriDecoder;

    protected AbstractR2RMLSafeIRIEncodeFunctionSymbol(DBTermType dbStringType) {
        super("R2RMLIRISafeEncode", ImmutableList.of(dbStringType), dbStringType);
        Charset charset = StandardCharsets.UTF_8;
        this.iriEncoder = new Encoder(charset);
        this.iriDecoder = new Decoder(charset);
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof DBConstant)
            return encodeConstant((DBConstant) newTerm, termFactory);

        /*
         * Looks for DB type conversions (e.g. casts) from a DBTermType that is known to be safe (e.g. decimals)
         */
        if (newTerm instanceof ImmutableFunctionalTerm) {
            FunctionSymbol functionSymbol = ((ImmutableFunctionalTerm) newTerm).getFunctionSymbol();

            if ((functionSymbol instanceof DBTypeConversionFunctionSymbol)
                    && ((DBTypeConversionFunctionSymbol) functionSymbol).getInputType()
                    .filter(t -> !t.isNeedingIRISafeEncoding())
                    .isPresent())
                return newTerm;
        }

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    private DBConstant encodeConstant(DBConstant constant, TermFactory termFactory) {
        // Query element: percent-encoding except if in iunreserved
        // TODO: this implementation seems to ignore the ucschar range. Check if it is a problem
        // YES, it is a problem for RDB2RDFTest, see test dg0017
        // However, this is precisely what is required for ENCODE_FOR_URI in SPARQL
        // TODO: redundant with R2RMLIRISafeEncoder. Which one shall we choose?
        // both, but we need to differentiate between URIs (incorrectly called iriEncoder) and IRIs
        //   this one for IRIs
        // return termFactory.getDBStringConstant(R2RMLIRISafeEncoder.encode(constant.getValue()));
        //   this one of URIs
        return termFactory.getDBStringConstant(iriEncoder.encodeQueryElement(constant.getValue()));
    }

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        DBConstant decodedConstant = termFactory.getDBStringConstant(
                iriDecoder.urlDecode(otherTerm.getValue(), true));

        ImmutableExpression newExpression = termFactory.getStrictEquality(terms.get(0), decodedConstant);

        return newExpression.evaluate(variableNullability, true);
    }
}
