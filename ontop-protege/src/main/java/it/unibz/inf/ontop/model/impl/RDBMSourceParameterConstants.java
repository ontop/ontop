package it.unibz.inf.ontop.model.impl;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.inf.ontop.injection.OntopSQLSettings;

public class RDBMSourceParameterConstants {

	public static final String	DATABASE_USERNAME			= OntopSQLSettings.JDBC_USER;
	
	public static final String	DATABASE_PASSWORD			= OntopSQLSettings.JDBC_PASSWORD;
	
	public static final String	DATABASE_DRIVER				= OntopSQLSettings.JDBC_DRIVER;
	
	public static final String	DATABASE_URL				= OntopSQLSettings.JDBC_URL;
	
	public static final String	USE_DATASOURCE_FOR_ABOXDUMP	= "use_datasource_for_aboxdump";
	
	public static final String	IS_IN_MEMORY				= "is_in_memory";
}
