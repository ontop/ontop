package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.injection.impl.OntopOBDAConfigurationImpl;

public interface OntopOBDAConfiguration extends OntopModelConfiguration {

    @Override
    OntopOBDASettings getSettings();

    static Builder<Builder<Builder<Builder>>> defaultBuilder() {
        return new OntopOBDAConfigurationImpl.BuilderImpl<>();
    }

    /**
     * To be extended if needed
     */
    interface OntopOBDABuilderFragment<B extends Builder> {
    }

    interface Builder<B extends Builder> extends OntopOBDABuilderFragment<B>, OntopModelConfiguration.Builder<B> {

        @Override
        OntopOBDAConfiguration build();
    }

}
