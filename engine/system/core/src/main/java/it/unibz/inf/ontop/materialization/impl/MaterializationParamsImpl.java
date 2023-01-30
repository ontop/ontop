package it.unibz.inf.ontop.materialization.impl;

import it.unibz.inf.ontop.materialization.MaterializationParams;

public class MaterializationParamsImpl implements MaterializationParams {

    private final boolean enableIncompleteMaterialization;

    private MaterializationParamsImpl(boolean enableIncompleteMaterialization) {
        this.enableIncompleteMaterialization = enableIncompleteMaterialization;
    }


    @Override
    public boolean canMaterializationBeIncomplete() {
        return enableIncompleteMaterialization;
    }


    public static class DefaultBuilder implements Builder<DefaultBuilder> {

        private boolean canMaterializationBeIncomplete;

        public DefaultBuilder() {
            this.canMaterializationBeIncomplete = false;
        }

        @Override
        public DefaultBuilder enableIncompleteMaterialization(boolean enable) {
            this.canMaterializationBeIncomplete = enable;
            return this;
        }

        @Override
        public MaterializationParams build() {
            return new MaterializationParamsImpl(canMaterializationBeIncomplete);
        }
    }

}
