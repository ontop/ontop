package it.unibz.inf.ontop.materialization.impl;

import it.unibz.inf.ontop.materialization.MaterializationParams;

public class MaterializationParamsImpl implements MaterializationParams {

    private final boolean enableIncompleteMaterialization;
    private final boolean allowDuplicates;
    private final boolean useLegacyMaterializer;

    private MaterializationParamsImpl(boolean enableIncompleteMaterialization, boolean allowDuplicates, boolean useLegacyMaterializer) {
        this.enableIncompleteMaterialization = enableIncompleteMaterialization;
        this.allowDuplicates = allowDuplicates;
        this.useLegacyMaterializer = useLegacyMaterializer;
    }

    @Override
    public boolean canMaterializationBeIncomplete() {
        return enableIncompleteMaterialization;
    }

    @Override
    public boolean areDuplicatesAllowed() {
        return allowDuplicates;
    }

    @Override
    public boolean useLegacyMaterializer() {
        return useLegacyMaterializer;
    }

    public static class DefaultBuilder implements Builder<DefaultBuilder> {

        private boolean canMaterializationBeIncomplete = false;
        private boolean canMaterializationAllowDuplicates = false;
        private boolean useLegacyMaterializer = false;

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
        public DefaultBuilder useLegacyMaterializer(boolean useLegacyMaterializer) {
            this.useLegacyMaterializer = useLegacyMaterializer;
            return this;
        }

        @Override
        public MaterializationParams build() {
            return new MaterializationParamsImpl(canMaterializationBeIncomplete, canMaterializationAllowDuplicates, useLegacyMaterializer);
        }
    }

}
