package it.unibz.inf.ontop.answering.reformulation.generation;

import it.unibz.inf.ontop.iq.IQ;

/**
 * Generates an IQ containing a source query in a given native query language.
 *
 * See TranslationFactory for creating a new instance.
 *
 */
public interface NativeQueryGenerator {

	IQ generateSourceQuery(IQ query);

	IQ generateSourceQuery(IQ query, boolean avoidPostProcessing);
}
