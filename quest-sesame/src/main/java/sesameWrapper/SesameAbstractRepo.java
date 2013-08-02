/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package sesameWrapper;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

public abstract class SesameAbstractRepo implements
		org.openrdf.repository.Repository {

	private RepositoryConnection repoConnection;
	private Map<String, String> namespaces;
	boolean isinitialized = false;
	
	public SesameAbstractRepo() {
		namespaces = new HashMap<String, String>();
	}

	public RepositoryConnection getConnection() throws RepositoryException {
		try {
			this.repoConnection = new RepositoryConnection(this,
					getQuestConnection());
		} catch (OBDAException e) {
			System.out.println("Error creating repo connecion!");
			e.printStackTrace();
		}
		return repoConnection;

	}

	public File getDataDir() {
		// TODO Auto-generated method stub
		// Get the directory where data and logging for this repository is
		// stored.
		return null;
	}

	public ValueFactory getValueFactory() {
		// Gets a ValueFactory for this Repository.
		return ValueFactoryImpl.getInstance();
	}

	public void initialize() throws RepositoryException {
		// Initializes this repository.
		isinitialized = true;
	}
	
	public boolean isInitialized() {
		return isinitialized;
	}

	public abstract QuestDBConnection getQuestConnection() throws OBDAException;

	public boolean isWritable() throws RepositoryException {
		// Checks whether this repository is writable, i.e.
		// if the data contained in this repository can be changed.
		// The writability of the repository is determined by the writability
		// of the Sail that this repository operates on.
		return false;
	}

	public void setDataDir(File arg0) {
		// Set the directory where data and logging for this repository is
		// stored.
	}

	public void shutDown() throws RepositoryException {
		// Shuts the repository down, releasing any resources that it keeps hold
		// of.
		// Once shut down, the repository can no longer be used until it is
		// re-initialized.
		isinitialized=false;
		if(repoConnection.isOpen())
			repoConnection.close();
		
	}
	

	public abstract String getType();
	
	public void setNamespace(String key, String value)
	{
		namespaces.put(key, value);
	}
	
	public String getNamespace(String key)
	{
		return namespaces.get(key);
	}
	
	public Map<String, String> getNamespaces()
	{
		return namespaces;
	}
	
	public void setNamespaces(Map<String, String> nsp)
	{
		this.namespaces = nsp;
	}

	public void removeNamespace(String key) 
	{
		namespaces.remove(key);
	}

}
