package it.unibz.krdb.obda.protege4.gui.treemodels;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
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
 * An abstract filter that can be used by TreeModels to discriminate certain
 * elements stored in the controller associated to the tree model.
 * 
 * @param <T>
 *            T is the class name of the kind of object that is taken as input
 *            by an implementation of TreeModelFilter.
 */
public abstract class TreeModelFilter<T extends Object> {

	protected boolean bNegation;
	protected String[] vecKeyword;

	public void putNegation() {
		bNegation = true;
	}

	public void addStringFilter(String[] values) {
		vecKeyword = values;
	}

	/**
	 * A matching function that returns true if the object complies to the
	 * internal logic of the Filter. Instances of TreeModelFilter should define
	 * their own logic.
	 * 
	 * @param object
	 *            The object that needs to be matches
	 * @return true if the object matches the internal logic, false otherwise
	 */
	public abstract boolean match(T object);
}
