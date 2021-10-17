package it.unibz.inf.ontop.iq.transformer;

import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;

/**
 * Adds placeholder/fake data to Values Node in the edge case when empty rows are projected i.e. VALUES [] () ()
 * All database engines which support the VALUES clause do not support expressions such as VALUES () ()
 */
public interface ValuesNodeTransformer extends IQTreeTransformer {
}

