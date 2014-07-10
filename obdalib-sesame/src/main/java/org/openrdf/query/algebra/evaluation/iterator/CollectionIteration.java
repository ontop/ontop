/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Collection;
import java.util.Iterator;

import info.aduna.iteration.CloseableIterationBase;

/**
 * An iteration to access a materialized {@link Collection} of BindingSets.
 * 
 * @author Andreas Schwarte
 */
public class CollectionIteration<E, X extends Exception> extends CloseableIterationBase<E, X> {

	
	protected final Collection<E> collection;
	protected Iterator<E> iterator;	
	
	/**
	 * @param collection
	 */
	public CollectionIteration(Collection<E> collection) {
		super();
		this.collection = collection;
		iterator = collection.iterator();
	}
	

	public boolean hasNext() throws X {
		return iterator.hasNext();
	}

	public E next() throws X {
		return iterator.next();
	}

	public void remove() throws X {
		throw new UnsupportedOperationException("Remove not supported on CollectionIteration");		
	}
	

}
