package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

/**
 * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
 */
public class NonIntegerNumericInferenceRule extends NumericTermTypeInferenceRule {

    private final RDFDatatype integerDatatype;
    private final RDFDatatype decimalDatatype;

    public NonIntegerNumericInferenceRule(TypeFactory typeFactory) {
        integerDatatype = typeFactory.getXsdIntegerDatatype();
        decimalDatatype = typeFactory.getXsdDecimalDatatype();
    }

    @Override
    protected Optional<TermTypeInference> postprocessInferredType(Optional<TermTypeInference> optionalTypeInference) {
        return optionalTypeInference
                .flatMap(TermTypeInference::getTermType)
                .map(t -> t.equals(integerDatatype) ? decimalDatatype : t)
                .map(TermTypeInference::declareTermType);
    }
}
