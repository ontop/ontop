/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.viewmanager;

import it.unibz.krdb.obda.model.Function;

import java.io.Serializable;


/**
 * The view manager interface should be implemented by any class which is used to
 * establish a connection between data log queries and source queries. The Source generator
 * uses the information provided by it to translate datalog programs into source queries 
 * 
 * @author Manfred Gerstgrasser
 *
 */

public interface ViewManager extends Serializable {
	
	/**
	 * Translates the given atom into the alias which was used for it during
	 * some other processes, like abox dump, etc. Might not always necessary to
	 * implement
	 * 
	 * @param atom the atom
	 * @return the alias of the atom
	 * @throws Exception
	 */
	public String getTranslatedName(Function atom) throws Exception;
	
	/**
	 * Stores the original query head, so that also the source generator 
	 * knows the original variable names, which otherwise might get lost during the
	 * rewriting and unfolding process. Might not always necessary to
	 * implement
	 * @param head original head of the input query
	 */
	public void storeOrgQueryHead(Function head);
}
