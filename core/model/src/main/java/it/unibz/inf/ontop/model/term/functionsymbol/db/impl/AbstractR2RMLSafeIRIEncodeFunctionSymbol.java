package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import io.mikael.urlbuilder.util.Encoder;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.nio.charset.Charset;

public abstract class AbstractR2RMLSafeIRIEncodeFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    private final Encoder iriEncoder;

    protected AbstractR2RMLSafeIRIEncodeFunctionSymbol(DBTermType dbStringType) {
        super("R2RMLIRISafeEncode", ImmutableList.of(dbStringType), dbStringType);
        this.iriEncoder = new Encoder(Charset.forName("utf-8"));
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof DBConstant)
            return encodeConstant((DBConstant) newTerm, termFactory);

        return super.buildTermAfterEvaluation(newTerms, isInConstructionNodeInOptimizationPhase, termFactory);
    }

    private DBConstant encodeConstant(DBConstant constant, TermFactory termFactory) {
        // Query element: percent-encoding except if in iunreserved
        // TODO: this implementation seems to ignore the ucschar range. Check if it is a problem
        // TODO: redundant with R2RMLIRISafeEncoder. Which one shall we choose?
        return termFactory.getDBStringConstant(iriEncoder.encodeQueryElement(constant.getValue()));
    }
}
