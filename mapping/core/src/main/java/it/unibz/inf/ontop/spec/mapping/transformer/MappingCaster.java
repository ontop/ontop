package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

public interface MappingCaster  {

    MappingAssertion transform(MappingAssertion mapping);
}
