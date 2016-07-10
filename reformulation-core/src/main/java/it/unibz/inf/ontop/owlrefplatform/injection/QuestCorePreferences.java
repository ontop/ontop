package it.unibz.inf.ontop.owlrefplatform.injection;

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

import it.unibz.inf.ontop.injection.OBDAProperties;

import java.util.List;


/**
 * A class that represents the preferences overwritten by the user.
 *
 * Immutable class.
 */
public interface QuestCorePreferences extends OBDAProperties {

	boolean isOntologyAnnotationQueryingEnabled();

	boolean isSameAsInMappingsEnabled();

	boolean isRewritingEnabled();

	boolean isEquivalenceOptimizationEnabled();

	boolean isKeyPrintingEnabled();

	boolean isDistinctPostProcessingEnabled();

	/**
	 * In the case of SQL, inserts REPLACE functions in the generated query
	 */
	boolean isIRISafeEncodingEnabled();

	/**
	 * TODO: remove it when the virtual and classic A-Box modes will be completely isolated.
     */
	boolean isInVirtualMode();


	//--------------------------
	// Connection configuration
	//--------------------------

	boolean isKeepAliveEnabled();
	boolean isRemoveAbandonedEnabled();
	int getAbandonedTimeout();
	int getConnectionPoolInitialSize();
	int getConnectionPoolMaxSize();


	//--------------------------
	// Keys
	//--------------------------

	String	REFORMULATION_TECHNIQUE	= "org.obda.owlreformulationplatform.reformulationTechnique";
	String	ABOX_MODE				= "org.obda.owlreformulationplatform.aboxmode";
	String  OPTIMIZE_EQUIVALENCES 	= "org.obda.owlreformulationplatform.optimizeEquivalences";
	String  ANNOTATIONS_IN_ONTO     = "org.obda.owlreformulationplatform.queryingAnnotationsInOntology";
	String  SAME_AS   				= "org.obda.owlreformulationplatform.sameAs";

	/**
	 * Options to specify base IRI.
	 *
	 * @see <a href="http://www.w3.org/TR/r2rml/#dfn-base-iri">Base IRI</a>
	 */
	String  BASE_IRI             	= "org.obda.owlreformulationplatform.baseiri";

	String OBTAIN_FULL_METADATA = "OBTAIN_FULL_METADATA";

    String SQL_GENERATE_REPLACE = "org.obda.owlreformulationplatform.sqlGenerateReplace";
	String DISTINCT_RESULTSET = "org.obda.owlreformulationplatform.distinctResultSet";

    String  REWRITE 	= "rewrite";
	
//	String  OPTIMIZE_TBOX_SIGMA 	= "org.obda.owlreformulationplatform.optimizeTboxSigma";
//	String 	CREATE_TEST_MAPPINGS 	= "org.obda.owlreformulationplatform.createTestMappings";


    //@Deprecated
	//public static final String JDBC_URL = OBDAProperties.JDBC_URL;
    @Deprecated
	String DBNAME = OBDAProperties.DB_NAME;
    @Deprecated
	String DBUSER = OBDAProperties.DB_USER;
    @Deprecated
	String DBPASSWORD = OBDAProperties.DB_PASSWORD;

	String PRINT_KEYS = "PRINT_KEYS";

	// Tomcat connection pool properties
	String MAX_POOL_SIZE = "max_pool_size";
	String INIT_POOL_SIZE = "initial_pool_size";
	String REMOVE_ABANDONED = "remove_abandoned";
	String ABANDONED_TIMEOUT = "abandoned_timeout";
	String KEEP_ALIVE = "keep_alive";


	//------------------------------
	// Classic A-box-specific keys
	// TODO: move them into an extension dedicated to the classic A-Box mode
	//------------------------------

	String	DBTYPE					= "org.obda.owlreformulationplatform.dbtype";
	//	String	DATA_LOCATION			= "org.obda.owlreformulationplatform.datalocation";
	String  OBTAIN_FROM_ONTOLOGY	= "org.obda.owlreformulationplatform.obtainFromOntology";
	String  OBTAIN_FROM_MAPPINGS	= "org.obda.owlreformulationplatform.obtainFromMappings";
	String STORAGE_LOCATION = "STORAGE_LOCATION";
}
