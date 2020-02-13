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


    public static class DefaultBuilder<B extends Builder<B>> implements Builder<B> {

        private final B builder;
        private boolean canMaterializationBeIncomplete;

        public DefaultBuilder() {
            this.canMaterializationBeIncomplete = false;
            this.builder = (B) this;
        }

        @Override
        public B enableIncompleteMaterialization(boolean enable) {
            this.canMaterializationBeIncomplete = enable;
            return builder;
        }

        @Override
        public MaterializationParams build() {
            return new MaterializationParamsImpl(canMaterializationBeIncomplete);
        }
    }

}
