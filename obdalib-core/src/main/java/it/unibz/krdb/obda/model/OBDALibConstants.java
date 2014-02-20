package it.unibz.krdb.obda.model;

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

import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

//import com.hp.hpl.jena.iri.IRI;

public class OBDALibConstants {
	
	public static final String OBDA_PREFIX_MAPPING_PREDICATE = "obdap";
	public static final String OBDA_URI_MAPPING_PREDICATE = "http://obda.inf.unibz.it/quest/vocabulary#";
	public static final String OBDA_QUERY_PREDICATE = "q";
	
	public static final String QUERY_HEAD = OBDA_URI_MAPPING_PREDICATE + OBDA_QUERY_PREDICATE;
	//public static final IRI QUERY_HEAD_URI = OBDADataFactoryImpl.getIRI(QUERY_HEAD);

	
	public static final String DATALOG_IMPLY_SYMBOL = ":-";
}
