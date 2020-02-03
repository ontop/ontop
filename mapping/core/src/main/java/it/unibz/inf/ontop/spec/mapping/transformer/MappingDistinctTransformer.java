package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;

public interface MappingDistinctTransformer {

    /**
     * TODO: rename the method (too low-level)
     */
    MappingInTransformation addDistinct(MappingInTransformation mapping);
}
