package it.unibz.inf.ontop.spec.mapping.transformer;


import it.unibz.inf.ontop.iq.IQ;

import java.util.stream.Stream;

/**
 * TODO: remove it after getting rid of Datalog in the mapping process
 */
public interface QueryUnionSplitter {

    Stream<IQ> splitUnion(IQ query);
}
