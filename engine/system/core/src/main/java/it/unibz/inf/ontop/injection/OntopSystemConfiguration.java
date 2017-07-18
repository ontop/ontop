package it.unibz.inf.ontop.injection;


public interface OntopSystemConfiguration extends OntopTranslationConfiguration, OntopOBDASpecificationConfiguration {

    @Override
    OntopSystemSettings getSettings();

    interface OntopSystemBuilderFragment<B extends Builder<B>> {

        B keepPermanentDBConnection(boolean keep);
    }

    interface Builder<B extends Builder<B>>
            extends OntopSystemBuilderFragment<B>,
            OntopTranslationConfiguration.Builder<B>, OntopOBDASpecificationConfiguration.Builder<B> {

        @Override
        OntopSystemConfiguration build();
    }
}
