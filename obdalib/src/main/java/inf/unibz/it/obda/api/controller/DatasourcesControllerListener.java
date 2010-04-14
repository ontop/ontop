/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, 
 * b) third-party components licensed under terms that may be different from 
 *   those of the LGPL.  Information about such licenses can be found in the 
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.api.controller;

import inf.unibz.it.obda.domain.DataSource;


public interface DatasourcesControllerListener {
	public void datasourceAdded(DataSource source);
	public void datasourceDeleted(DataSource source);
	public void datasourceUpdated(String oldname, DataSource currendata);
	public void alldatasourcesDeleted();
	
	//TODO remove this method, no more concept of "Current datasource"
	public void currentDatasourceChange(DataSource previousdatasource, DataSource currentsource);
}
