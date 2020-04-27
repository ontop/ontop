package it.unibz.inf.ontop.materialization;

import it.unibz.inf.ontop.materialization.impl.MaterializationParamsImpl;

/**
 * Materialization-specific parameters
 */
public interface MaterializationParams {

    /**
     * If true, stops exceptions due to materialization issues
     * for some RDF properties/classes.
     *
     * False by default.
     *
     */
    boolean canMaterializationBeIncomplete();


    static Builder defaultBuilder() {
        return new MaterializationParamsImpl.DefaultBuilder<>();
    }


    interface Builder<B extends Builder<B>> {

        B enableIncompleteMaterialization(boolean enable);

        MaterializationParams build();

    }

}
