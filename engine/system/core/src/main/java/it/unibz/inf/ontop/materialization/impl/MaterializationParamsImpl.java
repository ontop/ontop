package it.unibz.inf.ontop.materialization.impl;

import it.unibz.inf.ontop.materialization.MaterializationParams;

public class MaterializationParamsImpl implements MaterializationParams {

    private final boolean enableIncompleteMaterialization;
    private final boolean allowDuplicates;

    private MaterializationParamsImpl(boolean enableIncompleteMaterialization, boolean allowDuplicates) {
        this.enableIncompleteMaterialization = enableIncompleteMaterialization;
        this.allowDuplicates = allowDuplicates;
    }

    @Override
    public boolean canMaterializationBeIncomplete() {
        return enableIncompleteMaterialization;
    }

    @Override
    public boolean areDuplicatesAllowed() {
        return allowDuplicates;
    }

    public static class DefaultBuilder implements Builder<DefaultBuilder> {

        private boolean canMaterializationBeIncomplete;
        private boolean canMaterializationAllowDuplicates;

        public DefaultBuilder() {
            this.canMaterializationBeIncomplete = false;
        }

        @Override
        public DefaultBuilder enableIncompleteMaterialization(boolean enable) {
            this.canMaterializationBeIncomplete = enable;
            return this;
        }

        @Override
        public DefaultBuilder allowDuplicates(boolean allow) {
            this.canMaterializationAllowDuplicates = allow;
            return this;
        }

        @Override
        public MaterializationParams build() {
            return new MaterializationParamsImpl(canMaterializationBeIncomplete, canMaterializationAllowDuplicates);
        }
    }

}
