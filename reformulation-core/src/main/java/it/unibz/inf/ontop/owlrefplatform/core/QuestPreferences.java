package it.unibz.inf.ontop.owlrefplatform.core;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.model.DataSourceMetadata;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing.TMappingExclusionConfig;
import it.unibz.inf.ontop.sql.ImplicitDBConstraintsReader;
import org.openrdf.model.Model;

import javax.annotation.Nonnull;

/**
 * A class that represents the preferences overwritten by the user.
 *
 * Immutable class.
 */
public class QuestPreferences extends OBDAProperties {

	// TODO create a configuration listener to handle changes in these values
	private static final long	serialVersionUID		= -5954970472045517594L;

	private static final String DEFAULT_QUEST_PROPERTIES_FILE = "QuestDefaults.properties";

	public static final String	REFORMULATION_TECHNIQUE	= "org.obda.owlreformulationplatform.reformulationTechnique";
	public static final String	ABOX_MODE				= "org.obda.owlreformulationplatform.aboxmode";
	public static final String	DBTYPE					= "org.obda.owlreformulationplatform.dbtype";
//	public static final String	DATA_LOCATION			= "org.obda.owlreformulationplatform.datalocation";
	public static final String  OBTAIN_FROM_ONTOLOGY	= "org.obda.owlreformulationplatform.obtainFromOntology";
	public static final String  OBTAIN_FROM_MAPPINGS	= "org.obda.owlreformulationplatform.obtainFromMappings";
	public static final String  OPTIMIZE_EQUIVALENCES 	= "org.obda.owlreformulationplatform.optimizeEquivalences";
	public static final String  ANNOTATIONS_IN_ONTO     = "org.obda.owlreformulationplatform.queryingAnnotationsInOntology";
	public static final String  SAME_AS   				= "org.obda.owlreformulationplatform.sameAs";

	/**
	 * Options to specify base IRI.
	 *
	 * @see <a href="http://www.w3.org/TR/r2rml/#dfn-base-iri">Base IRI</a>
	 */
	public static final String  BASE_IRI             	= "org.obda.owlreformulationplatform.baseiri";

	public static final String OBTAIN_FULL_METADATA = "OBTAIN_FULL_METADATA";

    public static final String SQL_GENERATE_REPLACE = "org.obda.owlreformulationplatform.sqlGenerateReplace";
	public static final String DISTINCT_RESULTSET = "org.obda.owlreformulationplatform.distinctResultSet";

    public static final String  REWRITE 	= "rewrite";
	
//	public static final String  OPTIMIZE_TBOX_SIGMA 	= "org.obda.owlreformulationplatform.optimizeTboxSigma";
//	public static final String 	CREATE_TEST_MAPPINGS 	= "org.obda.owlreformulationplatform.createTestMappings";

	public static final String STORAGE_LOCATION = "STORAGE_LOCATION";

	private static final String TMAPPING_EXCLUSION = "TMAPPING_EXCLUSION";
	private static final String DB_METADATA_OBJECT = "DB_METADATA_OBJECT";

	public static final String ONTOLOGY_FILE_PATH = "ONTOLOGY_FILE_PATH";
	public static final String ONTOLOGY_FILE_OBJECT = "ONTOLOGY_FILE_OBJECT";
	private static final String ONTOLOGY_OBJECT = "ONTOLOGY_OBJECT";



    //@Deprecated
	//public static final String JDBC_URL = OBDAProperties.JDBC_URL;
    @Deprecated
	public static final String DBNAME = OBDAProperties.DB_NAME;
    @Deprecated
	public static final String DBUSER = OBDAProperties.DB_USER;
    @Deprecated
	public static final String DBPASSWORD = OBDAProperties.DB_PASSWORD;
    //@Deprecated
	//public static final String JDBC_DRIVER = OBDAProperties.JDBC_DRIVER;

	public static final String PRINT_KEYS = "PRINT_KEYS";

	// Tomcat connection pool properties
	public static final String MAX_POOL_SIZE = "max_pool_size";
	public static final String INIT_POOL_SIZE = "initial_pool_size";
	public static final String REMOVE_ABANDONED = "remove_abandoned";
	public static final String ABANDONED_TIMEOUT = "abandoned_timeout";
	public static final String KEEP_ALIVE = "keep_alive";


	/**
	 * Beware: immutable class!
	 *
	 * --> Only default properties.
	 *
	 * TODO: remove this constructor
	 */
	@Deprecated
	public QuestPreferences() {
		this(new Properties());
	}

	/**
	 * Recommended constructor.
	 *
	 * Beware: immutable class!
	 *
	 * Changing the Properties object afterwards will not have any effect
	 * on this OBDAProperties object.
	 */
	public QuestPreferences(Properties userPreferences) {
		super(loadQuestPreferences(userPreferences));
	}

	/**
	 * TODO: complete
	 */
	@Override
	public void validate() throws InvalidOBDAConfigurationException {
		boolean areMappingsDefined =
				contains(OBDAProperties.MAPPING_FILE_OBJECT)
						|| contains(OBDAProperties.MAPPING_FILE_READER)
						|| contains(OBDAProperties.MAPPING_FILE_MODEL)
						|| contains(OBDAProperties.MAPPING_FILE_PATH)
						|| contains(OBDAProperties.PREDEFINED_OBDA_MODEL);
		if ((!areMappingsDefined) && get(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL)) {
			throw new InvalidOBDAConfigurationException("mappings are not specified in virtual mode", this);
		} else if (areMappingsDefined && get(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC)) {
			throw new InvalidOBDAConfigurationException("mappings are specified in classic mode", this);
		}

		/**
		 * TODO: complete
		 */

		// TODO: check the types of some Object properties.
	}

	private static Properties loadQuestPreferences(Properties userPreferences) {
		Properties properties = loadDefaultPropertiesFromFile(QuestPreferences.class, DEFAULT_QUEST_PROPERTIES_FILE);
		properties.putAll(userPreferences);
		return properties;
	}

	
	public List<String> getReformulationPlatformPreferencesKeys(){
		ArrayList<String> keys = new ArrayList<String>();
		keys.add(REFORMULATION_TECHNIQUE);
		keys.add(ABOX_MODE);
		keys.add(DBTYPE);
//		keys.add(DATA_LOCATION);
		keys.add(OBTAIN_FROM_ONTOLOGY);
		keys.add(OBTAIN_FROM_MAPPINGS);
		keys.add(OPTIMIZE_EQUIVALENCES);
//		keys.add(ANNOTATIONS_IN_ONTO);
//		keys.add(OPTIMIZE_TBOX_SIGMA);
//		keys.add(CREATE_TEST_MAPPINGS);

		return keys;
	}

	public Optional<File> getMappingFile() {
		if (contains(QuestPreferences.MAPPING_FILE_PATH)) {
			File mappingFile = new File(getProperty(QuestPreferences.MAPPING_FILE_PATH));
			return Optional.of(mappingFile);
		}
		else if (contains(QuestPreferences.MAPPING_FILE_OBJECT)) {
			File mappingFile = (File) get(QuestPreferences.MAPPING_FILE_OBJECT);
			return Optional.of(mappingFile);
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<Reader> getMappingReader() {
		return Optional.ofNullable(get(QuestPreferences.MAPPING_FILE_READER))
				.map(r -> (Reader) r);
	}

	public Optional<Model> getMappingModel() {
		return Optional.ofNullable(get(QuestPreferences.MAPPING_FILE_MODEL))
				.map(o -> (Model) o);
	}

	public Optional<OBDAModel> getPredefinedOBDAModel() {
		return Optional.ofNullable(get(QuestPreferences.MAPPING_FILE_MODEL))
				.map(o -> (OBDAModel) o);
	}

	public boolean isInVirtualMode() {
		return getProperty(ABOX_MODE).equals(QuestConstants.VIRTUAL);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Optional<TMappingExclusionConfig> excludeFromTMappings = Optional.empty();
		private Optional<ImplicitDBConstraintsReader> userConstraints = Optional.empty();
		private Optional<OBDAModel> obdaModel = Optional.empty();
		private Optional<File> mappingFile = Optional.empty();
		private Optional<Reader> mappingReader = Optional.empty();
		private Optional<Model> mappingGraph = Optional.empty();
		private Optional<Boolean> queryingAnnotationsInOntology = Optional.empty();
		private Optional<Boolean> sameAsMappings = Optional.empty();
		private Optional<Properties> properties = Optional.empty();
		private Optional<DataSourceMetadata> dbMetadata = Optional.empty();

		private Optional<File> owlFile = Optional.empty();
		private Optional<OWLOntology> ontology = Optional.empty();

		private boolean useR2rml = false;
		private boolean areMappingsDefined = false;
		private boolean isOntologyDefined = false;

		protected Builder() {
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

		public Builder owlFile(@Nonnull String owlFilename) {
			if (isOntologyDefined) {
				throw new IllegalArgumentException("Ontology already defined!");
			}
			isOntologyDefined = true;
			this.owlFile = Optional.of(new File(owlFilename));
			return this;
		}

		public Builder owlFile(@Nonnull File owlFile) {
			if (isOntologyDefined) {
				throw new IllegalArgumentException("Ontology already defined!");
			}
			isOntologyDefined = true;
			this.owlFile = Optional.of(owlFile);
			return this;
		}

		public Builder ontology(@Nonnull OWLOntology ontology) {
			if (isOntologyDefined) {
				throw new IllegalArgumentException("Ontology already defined!");
			}
			isOntologyDefined = true;
			this.ontology = Optional.of(ontology);
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

		public Builder r2rmlMappingGraph(@Nonnull Model rdfGraph) {
			if (areMappingsDefined) {
				throw new IllegalArgumentException("OBDA model or mappings already defined!");
			}
			areMappingsDefined = true;
			useR2rml = true;
			this.mappingGraph = Optional.of(rdfGraph);
			return this;
		}

		public Builder dbMetadata(@Nonnull DataSourceMetadata dbMetadata) {
			this.dbMetadata = Optional.of(dbMetadata);
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

		public final QuestPreferences build() throws InvalidOBDAConfigurationException {
			Properties p = generateIndirectProperties();

			/**
			 * User-provided properties have the highest precedence.
			 */
			properties.ifPresent(p::putAll);

			return createPreferences(p);
		}

		/**
		 * TODO: explain
		 * TODO: find a better term
		 *
		 * Can be overloaded (for extensions)
         */
		protected Properties generateIndirectProperties() {
			Properties p = new Properties();
			userConstraints.ifPresent(constraints -> p.put(OBDAProperties.DB_CONSTRAINTS, constraints));
			excludeFromTMappings.ifPresent(config -> p.put(QuestPreferences.TMAPPING_EXCLUSION, config));
			dbMetadata.ifPresent(m -> p.put(QuestPreferences.DB_METADATA_OBJECT, m));

			mappingFile.ifPresent(f -> p.put(OBDAProperties.MAPPING_FILE_OBJECT, f));
			mappingReader.ifPresent(r -> p.put(OBDAProperties.MAPPING_FILE_READER, r));
			mappingGraph.ifPresent(m -> p.put(OBDAProperties.MAPPING_FILE_MODEL, m));
			obdaModel.ifPresent(m -> p.put(OBDAProperties.PREDEFINED_OBDA_MODEL, m));

			owlFile.ifPresent(o -> p.put(QuestPreferences.ONTOLOGY_FILE_OBJECT, o));
			ontology.ifPresent(o -> p.put(QuestPreferences.ONTOLOGY_OBJECT, o));

			// By default, virtual A-box mode
			if (!areMappingsDefined) {
				p.put(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
			}

			queryingAnnotationsInOntology.ifPresent(b -> p.put(QuestPreferences.ANNOTATIONS_IN_ONTO, b));
			sameAsMappings.ifPresent(b -> p.put(QuestPreferences.SAME_AS, b));

			return p;
		}

		/**
		 * Can be overloaded by specialized classes (extensions).
		 */
		protected QuestPreferences createPreferences(Properties p) {
			return useR2rml
					? new R2RMLQuestPreferences(p)
					: new QuestPreferences(p);
		}
	}
}
