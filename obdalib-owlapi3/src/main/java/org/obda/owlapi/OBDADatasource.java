/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package org.obda.owlapi;

import java.net.URI;

public interface OBDADatasource {
	public URI getID();

	public String getJDBCURL();

	public String getPassword();

	public String getUsername();

	public String getJDBCDriver();
}
