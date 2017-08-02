package it.unibz.inf.ontop.materialization.impl;

import it.unibz.inf.ontop.materialization.MaterializationParams;

public class MaterializationParamsImpl implements MaterializationParams {

    private final boolean enableDBResultStreaming;
    private final boolean enableIncompleteMaterialization;

    private MaterializationParamsImpl(boolean enableDBResultStreaming, boolean enableIncompleteMaterialization) {
        this.enableDBResultStreaming = enableDBResultStreaming;
        this.enableIncompleteMaterialization = enableIncompleteMaterialization;
    }


    @Override
    public boolean isDBResultStreamingEnabled() {
        return enableDBResultStreaming;
    }

    @Override
    public boolean canMaterializationBeIncomplete() {
        return enableIncompleteMaterialization;
    }


    public static class DefaultBuilder<B extends Builder<B>> implements Builder<B> {

        private final B builder;
        private boolean isDBResultStreamingEnabled;
        private boolean canMaterializationBeIncomplete;

        public DefaultBuilder() {
            this.isDBResultStreamingEnabled = false;
            this.canMaterializationBeIncomplete = false;
            this.builder = (B) this;
        }


        @Override
        public B enableDBResultsStreaming(boolean enable) {
            this.isDBResultStreamingEnabled = enable;
            return builder;
        }

        @Override
        public B enableIncompleteMaterialization(boolean enable) {
            this.canMaterializationBeIncomplete = enable;
            return builder;
        }

        @Override
        public MaterializationParams build() {
            return new MaterializationParamsImpl(isDBResultStreamingEnabled, canMaterializationBeIncomplete);
        }
    }

}
