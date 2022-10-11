package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import it.unibz.inf.ontop.query.KGQueryFactory;
import it.unibz.inf.ontop.query.RDF4JQueryFactory;
import it.unibz.inf.ontop.query.translation.InsertClauseNormalizer;
import it.unibz.inf.ontop.query.translation.KGQueryTranslator;
import it.unibz.inf.ontop.query.unfolding.QueryUnfolder;
import it.unibz.inf.ontop.injection.*;


public class OntopKGQueryModule extends OntopAbstractModule {

    private final OntopKGQuerySettings settings;

    protected OntopKGQueryModule(OntopKGQuerySettings settings) {
        super(settings);
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(OntopKGQuerySettings.class).toInstance(settings);
        bindFromSettings(KGQueryTranslator.class);
        bindFromSettings(RDF4JQueryFactory.class);
        bindFromSettings(KGQueryFactory.class);
        bindFromSettings(InsertClauseNormalizer.class);

        Module unfolderFactory = buildFactory(
                ImmutableList.of(
                        QueryUnfolder.class
                ),
                QueryUnfolder.Factory.class);
        install(unfolderFactory);
    }
}
