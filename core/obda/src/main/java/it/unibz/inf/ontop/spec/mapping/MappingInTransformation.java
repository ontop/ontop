package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.*;
import it.unibz.inf.ontop.iq.IQ;

import java.util.Optional;

public interface MappingInTransformation {

    ImmutableMap<MappingAssertionIndex, IQ> getAssertions();

    Optional<IQ> getAssertion(MappingAssertionIndex idx);
}
