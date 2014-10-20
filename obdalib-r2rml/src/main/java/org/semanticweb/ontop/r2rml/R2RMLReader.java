package org.semanticweb.ontop.r2rml;

/*
 * #%L
 * ontop-obdalib-sesame
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
/**
 * @author timea bagosi
 * Class responsible to construct an OBDA model from an R2RML mapping file or graph.
 *
 * Please do not call it directly. Use the R2RMLMappingParser instead.
 */
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.injection.OBDACoreModule;
import org.semanticweb.ontop.injection.OBDAProperties;
import org.semanticweb.ontop.io.PrefixManager;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.openrdf.model.Model;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class R2RMLReader{

	private final R2RMLManager manager;
	private final NativeQueryLanguageComponentFactory nativeQLFactory;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(R2RMLReader.class);

	private OBDAModel obdaModel;
	private final Model m ;
	
	public R2RMLReader(Model m,
                       NativeQueryLanguageComponentFactory nativeQLFactory) {
		this.manager = new R2RMLManager(m);
		this.m = m;
        this.obdaModel = null;
        this.nativeQLFactory = nativeQLFactory;
	}
	
	public R2RMLReader(String file, NativeQueryLanguageComponentFactory nativeQLFactory)
	{
		this(new File(file), nativeQLFactory);
	}

	public R2RMLReader(File file, NativeQueryLanguageComponentFactory nativeQLFactory)
	{
        this.nativeQLFactory = nativeQLFactory;
		this.manager = new R2RMLManager(file);
		this.m = manager.getModel();
        this.obdaModel = null;
	}
		
	/**
	 * the method that gives the obda model based on the given graph
	 * @param sourceUri - the uri of the datasource of the model
	 * @return the read obda model
     *
	 */
    @Deprecated
	public OBDAModel readModel(URI sourceUri) throws DuplicateMappingException {
        Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = new HashMap<>(obdaModel.getMappings());
        List<OBDAMappingAxiom> sourceMappings;
        if (mappingIndex.containsKey(sourceUri))
            sourceMappings = new ArrayList<>(mappingIndex.get(sourceUri));
        else
            sourceMappings = new ArrayList<>();

        sourceMappings.addAll(manager.getMappings(m));
        mappingIndex.put(sourceUri, ImmutableList.copyOf(sourceMappings));

        /**
         *
         */
        if (obdaModel == null) {
            LOG.warn("R2RML reader: adds mappings without having declared data sources");
            PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
            obdaModel = nativeQLFactory.create(ImmutableSet.<OBDADataSource>of(), mappingIndex, prefixManager);
        }
        else {
            obdaModel = obdaModel.newModel(obdaModel.getSources(), mappingIndex);
        }
		return obdaModel;
	}
	
	/**
	 * the method that gives the obda model based on the given graph
	 * @param dataSource - the datasource of the model
	 * @return the read obda model
	 */
    @Deprecated
	public OBDAModel readModel(OBDADataSource dataSource) throws DuplicateMappingException {
        URI sourceUri = dataSource.getSourceID();

        /**
         * Model initialization
         */
        if (obdaModel == null) {
            ImmutableList<OBDAMappingAxiom> sourceMappings = ImmutableList.copyOf(manager.getMappings(m));
            PrefixManager prefixManager = nativeQLFactory.create(new HashMap<String, String>());
            Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = ImmutableMap.of(sourceUri, sourceMappings);
            obdaModel = nativeQLFactory.create(ImmutableSet.of(dataSource), mappingIndex, prefixManager);
        }
        /**
         * Model "update"
         */
        else {
            /**
             * Duplicate case
             */
            if (obdaModel.containsSource(sourceUri)) {
                LOG.warn("Source %s already present. New mappings ignored", sourceUri);
                return obdaModel;
            }
            Set<OBDADataSource> dataSources = new HashSet<>(obdaModel.getSources());
            dataSources.add(dataSource);

            ImmutableList<OBDAMappingAxiom> sourceMappings = ImmutableList.copyOf(manager.getMappings(m));
            Map<URI, ImmutableList<OBDAMappingAxiom>> mappingIndex = ImmutableMap.of(sourceUri, sourceMappings);
            obdaModel = obdaModel.newModel(dataSources, mappingIndex);
        }

		return obdaModel;
	}
	
	/**
	 * method to read the mappings from the graph
	 * @return list of obdaMappingAxioms
	 */
	public ImmutableList<OBDAMappingAxiom> readMappings(){
		return manager.getMappings(m);
	}
}
