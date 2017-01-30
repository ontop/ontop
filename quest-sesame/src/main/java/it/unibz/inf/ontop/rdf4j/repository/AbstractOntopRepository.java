package it.unibz.inf.ontop.rdf4j.repository;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractOntopRepository implements
		org.eclipse.rdf4j.repository.Repository, AutoCloseable {

	private Map<String, String> namespaces;
	
	public AbstractOntopRepository() {
		namespaces = new HashMap<>();
	}

	@Override
    public File getDataDir() {
		return null;
	}

	@Override
    public ValueFactory getValueFactory() {
		// Gets a ValueFactory for this Repository.
		return SimpleValueFactory.getInstance();
	}

	@Override
    public void setDataDir(File arg0) {
		// Set the directory where data and logging for this repository is
		// stored.
	}

    @Override
    public void close() throws Exception {
        this.shutDown();
    }
	
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
