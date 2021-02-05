package it.unibz.inf.ontop.iq.transformer;

import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;

/**
 * Pushes down in the tree a new definition (for a new variable) until:
 *   1. the condition can be evaluated to true/false/null
 *   2. or the definition is blocked by a (inner/left) join (its variables are defined in different children)
 */
public interface DefinitionPushDownTransformer extends IQTreeTransformer {


}
