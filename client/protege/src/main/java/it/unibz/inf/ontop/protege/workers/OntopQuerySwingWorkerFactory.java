package it.unibz.inf.ontop.protege.workers;

import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;

import java.util.function.BiFunction;

public interface OntopQuerySwingWorkerFactory<T, V> extends BiFunction<OntopProtegeReasoner, String, OntopQuerySwingWorker<T, V>> {

}
