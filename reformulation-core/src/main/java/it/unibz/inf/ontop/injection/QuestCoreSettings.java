package it.unibz.inf.ontop.injection;

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


/**
 * A class that represents the preferences overwritten by the user.
 *
 * Immutable class.
 */
public interface QuestCoreSettings extends OBDASettings, OntopQueryAnsweringSQLSettings {

	boolean isKeyPrintingEnabled();

	//-------------------
	// Low-level methods
	// TODO: hide them
	//-------------------

	@Deprecated
	boolean getRequiredBoolean(String key);


	//--------------------------
	// Keys
	//--------------------------

	@Deprecated
	String	REFORMULATION_TECHNIQUE	= "org.obda.owlreformulationplatform.reformulationTechnique";
	
//	String  OPTIMIZE_TBOX_SIGMA 	= "org.obda.owlreformulationplatform.optimizeTboxSigma";
//	String 	CREATE_TEST_MAPPINGS 	= "org.obda.owlreformulationplatform.createTestMappings";


	String PRINT_KEYS = "ontop.debug.printKeys";
}
