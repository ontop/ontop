package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.MappingInTransformation;

public interface MappingSameAsInverseRewriter {

    MappingInTransformation rewrite(MappingInTransformation mapping);
}
