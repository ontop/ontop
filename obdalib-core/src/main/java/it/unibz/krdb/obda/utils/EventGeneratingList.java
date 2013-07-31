/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano This source code is
 * available under the terms of the Affero General Public License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.utils;

import java.util.List;

/**
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 * @param <E>
 */
public interface EventGeneratingList<E> extends List<E>{

	public abstract void addListener(ListListener listener);

	public abstract void removeListener(ListListener listener);

	public abstract void riseListChanged();

}