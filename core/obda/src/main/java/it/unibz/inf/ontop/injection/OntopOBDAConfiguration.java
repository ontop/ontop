package it.unibz.inf.ontop.injection;


import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.spec.OBDASpecification;

public interface OntopOBDAConfiguration extends OntopModelConfiguration {

    @Override
    OntopOBDASettings getSettings();

    /**
     * If the OBDA specification object has been directly assigned by the user, returns it.
     *
     * Otherwise, loads it.
     */
    OBDASpecification loadSpecification() throws OBDASpecificationException;

    SpecificationFactory getSpecificationFactory();



    interface OntopOBDABuilderFragment<B extends Builder<B>> {

        /**
         * When the OBDA specification object has been created by the client
         */
        B obdaSpecification(OBDASpecification specification);

        /**
         * TODO: rename
         */
        B sameAsMappings(boolean enable);
    }

    interface Builder<B extends Builder<B>> extends OntopOBDABuilderFragment<B>, OntopModelConfiguration.Builder<B> {

        @Override
        OntopOBDAConfiguration build();
    }

}
