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
package it.unibz.krdb.obda.model;

import java.io.Serializable;




public interface OBDAMappingAxiom extends Cloneable, Serializable {

	public void setSourceQuery(OBDAQuery query);

	public OBDAQuery getSourceQuery();

	public void setTargetQuery(OBDAQuery query);

	public OBDAQuery getTargetQuery();

	public Object clone();

	public void setId(String id);

	public String getId();


}