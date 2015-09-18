package org.semanticweb.ontop.owlrefplatform.core.basicoperations;

import org.semanticweb.ontop.model.CQIE;

/**
 * Normalizer that "pulls out equalities".
 * This transformation is an important step of the transformation from relational calculus to relational algebra.
 *
 * Basically, after this transformation, constants are moved out of data atoms (only variables remain)
 * and variables appear only once in a data atom. Equalities have to be added to keep track of (i) equivalent variables
 * and of (ii) constants associated to some of these variables.
 *
 * Simple example:
 *    ans(x,y) :- Table1(x,x,y,2),Table2(x)
 *       is transformed into
 *    ans(x,y) :- Table1(x,x1,y,z),Table2(x2),EQ(x,x1),EQ(x,x2),EQ(z,2)
 *
 * These new equalities typically serve in WHERE and ON conditions when the target language is SQL.
 *
 * This normalization becomes a little bit challenging when we have to deal with left joins. With LJs, some equalities
 * should be applied to the LOCAL ON clause (the "joining" condition and "filtering" conditions for the right part
 * ONLY).
 *
 * NB: Recall that ONE query can only have one WHERE clause but multiple ONs.
 * WHERE and ON semantics differ significantly for LJs but not for Joins.
 *
 */
public interface PullOutEqualityNormalizer {

    /**
     * Returns a normalized rule.
     */
    CQIE normalizeByPullingOutEqualities(CQIE initialRule);
}
