package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.reformulation.unfolding.QueryUnfolder;

/**
 * Following the Guice AssistedInject pattern
 */
public interface ReformulationFactory {

    QueryUnfolder create(Mapping mapping);
}
