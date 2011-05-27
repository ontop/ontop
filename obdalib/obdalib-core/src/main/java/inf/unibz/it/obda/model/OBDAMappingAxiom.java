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
package inf.unibz.it.obda.model;




public interface OBDAMappingAxiom extends Cloneable {

	public void setSourceQuery(Query query);

	public Query getSourceQuery();

	public void setTargetQuery(Query query);

	public Query getTargetQuery();

	public Object clone() throws CloneNotSupportedException;

	public void setId(String id);

	public String getId();


}