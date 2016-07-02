package it.unibz.inf.ontop.owlrefplatform.owlapi;

import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.core.R2RMLQuestPreferences;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import org.openrdf.model.Model;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.util.Optional;
import java.util.Properties;

@SuppressWarnings("serial")
public class QuestOWLConfiguration extends SimpleConfiguration {

    private final QuestPreferences preferences;

    @Nonnull
    public QuestPreferences getPreferences() {
        return preferences;
    }

    public QuestOWLConfiguration(@Nonnull QuestPreferences preferences) {
        super(new NullReasonerProgressMonitor());
        this.preferences = preferences;
    }

    /**
     * Constructor
     */
    public QuestOWLConfiguration(@Nonnull QuestPreferences preferences,
                                 @Nonnull ReasonerProgressMonitor progressMonitor) {
        super(progressMonitor);
        this.preferences = preferences;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();
        private Optional<ImplicitDBConstraintsReader> userConstraints = Optional.empty();
        private Optional<OBDAModel> obdaModel = Optional.empty();
        private Optional<File> mappingFile = Optional.empty();
        private Optional<Reader> mappingReader = Optional.empty();
        private Optional<Model> mappingModel = Optional.empty();
        private Optional<Boolean> queryingAnnotationsInOntology = Optional.empty();
        private Optional<Boolean> sameAsMappings = Optional.empty();
        private Optional<ReasonerProgressMonitor> progressMonitor = Optional.empty();
        private Optional<Properties> properties = Optional.empty();

        private boolean useR2rml = false;
        private boolean areMappingsDefined = false;

        private Builder() {
        }

        public Builder tMappingExclusionConfig(@Nonnull TMappingExclusionConfig config) {
            this.excludeFromTMappings = Optional.of(config);
            return this;
        }

        public Builder dbConstraintsReader(@Nonnull ImplicitDBConstraintsReader constraints) {
            this.userConstraints = Optional.of(constraints);
            return this;
        }

        /**
         * Not for end-users! Please consider giving a mapping file or a mapping reader.
         */
        public Builder obdaModel(@Nonnull OBDAModel obdaModel) {
            if (areMappingsDefined) {
                throw new IllegalArgumentException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            this.obdaModel = Optional.of(obdaModel);
            return this;
        }

        public Builder nativeOntopMappingFile(@Nonnull File mappingFile) {
            if (areMappingsDefined) {
                throw new IllegalArgumentException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            this.mappingFile = Optional.of(mappingFile);
            return this;
        }

        public Builder nativeOntopMappingFile(@Nonnull String mappingFilename) {
            if (areMappingsDefined) {
                throw new IllegalArgumentException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            this.mappingFile = Optional.of(new File(mappingFilename));
            return this;
        }

        public Builder nativeOntopMappingReader(@Nonnull Reader mappingReader) {
            if (areMappingsDefined) {
                throw new IllegalArgumentException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            this.mappingReader = Optional.of(mappingReader);
            return this;
        }

        public Builder r2rmlMappingFile(@Nonnull File mappingFile) {
            if (areMappingsDefined) {
                throw new IllegalArgumentException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            useR2rml = true;
            this.mappingFile = Optional.of(mappingFile);
            return this;
        }

        public Builder r2rmlMappingFile(@Nonnull String mappingFilename) {
            if (areMappingsDefined) {
                throw new IllegalArgumentException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            useR2rml = true;
            this.mappingFile = Optional.of(new File(mappingFilename));
            return this;
        }

        public Builder r2rmlMappingReader(@Nonnull Reader mappingReader) {
            if (areMappingsDefined) {
                throw new IllegalArgumentException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            useR2rml = true;
            this.mappingReader = Optional.of(mappingReader);
            return this;
        }

        public Builder r2rmlMappingModel(@Nonnull Model mappingModel) {
            if (areMappingsDefined) {
                throw new IllegalArgumentException("OBDA model or mappings already defined!");
            }
            areMappingsDefined = true;
            useR2rml = true;
            this.mappingModel = Optional.of(mappingModel);
            return this;
        }

        public Builder queryingAnnotationsInOntology(boolean queryingAnnotationsInOntology) {
            this.queryingAnnotationsInOntology = Optional.of(queryingAnnotationsInOntology);
            return this;
        }

        public Builder sameAsMappings(boolean sameAsMappings) {
            this.sameAsMappings = Optional.of(sameAsMappings);
            return this;
        }

        /**
         * Have precedence over other parameters
         */
        public Builder properties(@Nonnull Properties properties) {
            this.properties = Optional.of(properties);
            return this;
        }

        public Builder progressMonitor(@Nonnull ReasonerProgressMonitor progressMonitor){
            this.progressMonitor = Optional.of(progressMonitor);
            return this;
        }

        public QuestOWLConfiguration build() throws IllegalConfigurationException {

            Properties p = new Properties();
            userConstraints.ifPresent(constraints -> p.put(OBDAProperties.DB_CONSTRAINTS, constraints));
            excludeFromTMappings.ifPresent(config -> p.put(QuestPreferences.TMAPPING_EXCLUSION, config));

            mappingFile.ifPresent(f -> p.put(OBDAProperties.MAPPING_FILE_OBJECT, f));
            mappingReader.ifPresent(r -> p.put(OBDAProperties.MAPPING_FILE_READER, r));
            mappingModel.ifPresent(m -> p.put(OBDAProperties.MAPPING_FILE_MODEL, m));
            obdaModel.ifPresent(m -> p.put(OBDAProperties.PREDEFINED_OBDA_MODEL, m));

            // By default, virtual A-box mode
            if (!areMappingsDefined) {
                p.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
            }

            queryingAnnotationsInOntology.ifPresent(b -> p.put(QuestPreferences.ANNOTATIONS_IN_ONTO, b));
            sameAsMappings.ifPresent(b -> p.put(QuestPreferences.SAME_AS, b));

            /**
             * User-provided properties have the highest precedence.
             */
            properties.ifPresent(p::putAll);

            QuestPreferences preferences = useR2rml
                    ? new R2RMLQuestPreferences(p)
                    : new QuestPreferences(p);

            return progressMonitor.isPresent()
                    ? new QuestOWLConfiguration(preferences, progressMonitor.get())
                    : new QuestOWLConfiguration(preferences);
        }
    }
}
