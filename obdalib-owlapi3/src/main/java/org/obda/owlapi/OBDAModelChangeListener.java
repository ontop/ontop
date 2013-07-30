/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.obda.owlapi;

import java.util.List;

import org.obda.owlapi.OBDAModelChange;
import org.semanticweb.owlapi.model.OWLException;

public interface OBDAModelChangeListener {

	void modelChanged(List<? extends OBDAModelChange> changes) throws OWLException;
}
