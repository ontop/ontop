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

public abstract class AbstractEncodeURIorIRIFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    private final EnDecoder enDecoder;

    protected AbstractEncodeURIorIRIFunctionSymbol(DBTermType dbStringType, boolean preserveInternationalChars) {
        super(preserveInternationalChars ? "R2RMLIRISafeEncode" : "DB_ENCODE_FOR_URI", ImmutableList.of(dbStringType), dbStringType);
        this.enDecoder = preserveInternationalChars
                ? new IRISafeEnDecoder()
                : new EnDecoderForURI();
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
        return termFactory.getDBStringConstant(enDecoder.encode(constant.getValue()));
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        DBConstant decodedConstant = termFactory.getDBStringConstant(
                enDecoder.decode(otherTerm.getValue()));

        ImmutableExpression newExpression = termFactory.getStrictEquality(terms.get(0), decodedConstant);

        return newExpression.evaluate(variableNullability, true);
    }

    interface EnDecoder {
        String encode(String value);
        String decode(String value);
    }

    /**
     * Not preserving international characters
     */
    protected static class EnDecoderForURI implements EnDecoder {

        private final Encoder uriEncoder;
        private final Decoder uriDecoder;

        protected EnDecoderForURI() {
            Charset charset = StandardCharsets.UTF_8;
            this.uriEncoder = new Encoder(charset);
            this.uriDecoder = new Decoder(charset);
        }

        /**
         * Query element: percent-encoding except if in iunreserved
         */
        @Override
        public String encode(String value) {
            return uriEncoder.encodeQueryElement(value);
        }

        @Override
        public String decode(String value) {
            return uriDecoder.urlDecode(value, true);
        }
    }

    /**
     * reserving international characters
     */
    protected static class IRISafeEnDecoder implements EnDecoder {

        protected IRISafeEnDecoder() {
        }

        @Override
        public String encode(String value) {
            return R2RMLIRISafeEncoder.encode(value);
        }

        @Override
        public String decode(String value) {
            return R2RMLIRISafeEncoder.decode(value);
        }
    }


}
