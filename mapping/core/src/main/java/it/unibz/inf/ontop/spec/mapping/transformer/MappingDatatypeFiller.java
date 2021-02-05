package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.exception.UnknownDatatypeException;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

public interface MappingDatatypeFiller  {

    MappingAssertion transform(MappingAssertion assertion)
            throws UnknownDatatypeException;
}
