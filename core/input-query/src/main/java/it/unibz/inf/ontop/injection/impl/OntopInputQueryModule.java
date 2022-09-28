package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.answering.reformulation.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.*;


public class OntopInputQueryModule extends OntopAbstractModule {

    private final OntopInputQuerySettings settings;

    protected OntopInputQueryModule(OntopInputQuerySettings settings) {
        super(settings);
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(OntopInputQuerySettings.class).toInstance(settings);

        Module unfolderFactory = buildFactory(
                ImmutableList.of(
                        QueryUnfolder.class
                ),
                QueryUnfolder.Factory.class);
        install(unfolderFactory);
    }
}
