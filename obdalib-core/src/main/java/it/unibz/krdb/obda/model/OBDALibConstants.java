/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

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
