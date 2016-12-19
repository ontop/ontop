package it.unibz.inf.ontop.owlrefplatform.injection;

import it.unibz.inf.ontop.injection.OBDACoreConfiguration;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.owlrefplatform.injection.impl.QuestCoreConfigurationImpl;
import it.unibz.inf.ontop.pivotalrepr.OptimizationConfiguration;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * TODO: explain
 *
 */
public interface QuestCoreConfiguration extends OBDACoreConfiguration {

    Optional<TMappingExclusionConfig> getTmappingExclusions();

    Optional<DBMetadata> getDatasourceMetadata();

    QuestCorePreferences getPreferences();

    OptimizationConfiguration getOptimizationConfiguration();

    CardinalityPreservationMode getCardinalityPreservationMode();


    static Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder<Builder>>>>>>>>>>>> defaultBuilder() {
        return new QuestCoreConfigurationImpl.BuilderImpl<>();
    }

    /**
     * By default, it assumes that the A-box is virtual.
     *
     * If you want to enable the classic A-box, please use a Properties object/file.
     * Not the classic A-box mode is not intended to be used by end-users (but for test purposes).
     *
     */
    interface Builder<B extends Builder> extends OBDACoreConfiguration.Builder<B> {

        B tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config);

        B dbMetadata(@Nonnull DBMetadata dbMetadata);

        B enableOntologyAnnotationQuerying(boolean queryingAnnotationsInOntology);

        /**
         * In the case of SQL, inserts REPLACE functions in the generated query
         */
        B enableIRISafeEncoding(boolean enable);

        B sameAsMappings(boolean sameAsMappings);

        B enableEquivalenceOptimization(boolean enable);

        B enableExistentialReasoning(boolean enable);

        QuestCoreConfiguration build();
    }

    enum CardinalityPreservationMode {
        /**
         * Cardinality is not important and may not be respected
         * (allows to optimize more)
         */
        LOOSE,
        /**
         * Cardinality is preserved in case a cardinality-sensitive
         * aggregation function is detected.
         */
        STRICT_FOR_AGGREGATION,
        /**
         * Cardinality is strictly preserved
         */
        STRICT
    }

}
