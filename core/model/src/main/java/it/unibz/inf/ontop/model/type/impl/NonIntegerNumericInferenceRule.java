package it.unibz.inf.ontop.model.type.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

import static it.unibz.inf.ontop.model.OntopModelSingletons.TYPE_FACTORY;

/**
 * Cannot infer COL_TYPE.INTEGER (will put COL_TYPE.DECIMAL instead)
 */
public class NonIntegerNumericInferenceRule extends NumericTermTypeInferenceRule {

    private final RDFDatatype integerDatatype = TYPE_FACTORY.getXsdIntegerDatatype();
    private final RDFDatatype decimalDatatype = TYPE_FACTORY.getXsdDecimalDatatype();

    @Override
    protected Optional<TermType> postprocessInferredType(Optional<TermType> optionalTermType) {
        // No need to call super.postprocessInferredType()
        return optionalTermType
                .map(t -> t.equals(integerDatatype) ? decimalDatatype : t);
    }
}
