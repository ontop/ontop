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
package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Query;

import java.util.Enumeration;

public abstract class MappingValidator {


	protected Query sourceQuery = null;
	protected Query targetQuery = null;
	protected OBDAModel apic = null;

	public MappingValidator (OBDAModel apic, Query sq, Query tg){
		this.apic = apic;
		sourceQuery = sq;
		targetQuery = tg;
	}


	/***
	 * Returns the set of errors found while validating this mapping if any.
	 *
	 * TODO fix api
	 * @return
	 */
	public abstract Enumeration<String> validate();
}
