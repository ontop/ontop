/*******************************************************************************
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.model;

import inf.unibz.it.obda.io.DataManager;
import inf.unibz.it.obda.io.PrefixManager;
import inf.unibz.it.obda.io.SimplePrefixManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OBDAModel {

	protected URI					currentOntologyURI	= null;

	protected DatasourcesController	dscontroller		= null;

	private MappingController		mapcontroller		= null;

	protected QueryController		queryController		= null;

	private PrefixManager			prefman				= null;

	protected final Logger			log					= LoggerFactory.getLogger(this.getClass());

	public OBDAModel() {

		dscontroller = new DatasourcesController();
		mapcontroller = new MappingController(dscontroller, this);
		queryController = new QueryController();

		setPrefixManager(new SimplePrefixManager());
		log.debug("OBDA Lib initialized");

	}

	public QueryController getQueryController() {
		return queryController;
	}

	public DatasourcesController getDatasourcesController() {
		return dscontroller;
	}

	public MappingController getMappingController() {
		return this.mapcontroller;
	}

	public String getVersion() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String implementationVersion = attributes.getValue("Implementation-Version");
			return implementationVersion;
		} catch (IOException e) {
			return "";
		}
	}

	public String getBuiltDate() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String builtDate = attributes.getValue("Built-Date");
			return builtDate;
		} catch (IOException e) {
			return "";
		}
	}

	public String getBuiltBy() {
		try {
			InputStream stream = getClass().getResourceAsStream("/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(stream);
			Attributes attributes = manifest.getMainAttributes();
			String builtBy = attributes.getValue("Built-By");
			return builtBy;
		} catch (IOException e) {
			return "";
		}
	}

	public void setPrefixManager(PrefixManager prefman) {
		this.prefman = prefman;
	}

	public PrefixManager getPrefixManager() {
		return prefman;
	}
}
