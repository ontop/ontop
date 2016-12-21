package it.unibz.inf.ontop.sesame;

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
/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */


import java.io.File;
import java.util.Properties;

import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;
import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.GraphUtilException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfigBase;

import static org.eclipse.rdf4j.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;



public class SesameRepositoryConfig extends RepositoryImplConfigBase {

    public static final String NAMESPACE = "http://inf.unibz.it/krdb/obda/quest#";

    /** <tt>http://inf.unibz.it/krdb/obda/quest#quest_type</tt> */
    public final static URI QUEST_TYPE;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#name</tt> */
    public final static URI NAME;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#owlFile/tt> */
    public final static URI OWLFILE;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#obdaFile</tt> */
    public final static URI OBDAFILE;
    
    public final static URI EXISTENTIAL;
    
    public final static URI REWRITING;

    public final static String VIRTUAL_QUEST_TYPE = "ontop-virtual";
    public final static String REMOTE_QUEST_TYPE = "ontop-remote";
    public final static String IN_MEMORY_QUEST_TYPE = "ontop-inmemory";
    
    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        QUEST_TYPE = factory.createURI(NAMESPACE, "quest_type");
        NAME = factory.createURI(NAMESPACE, "repo_name");
        OWLFILE = factory.createURI(NAMESPACE, "owlfile");
        OBDAFILE = factory.createURI(NAMESPACE, "obdafile");
        EXISTENTIAL = factory.createURI(NAMESPACE, "existential");
        REWRITING = factory.createURI(NAMESPACE, "rewriting");
    }
    
	private String quest_type;
    private String name;
    private File owlFile;
    private File obdaFile;
    private boolean existential;
    private String rewriting;

    /**
     * The repository has to be built by this class
     * so as to fit the validation and repository instantiation
     * workflow of Sesame.
     */
    private SesameAbstractRepo repository;

    /**
     * Creates a new RepositoryConfigImpl.
     */
    public SesameRepositoryConfig() {
    	super(SesameRepositoryFactory.REPOSITORY_TYPE);
        repository = null;
    }
    
    public String getName()
    {
    	return name;
    }

    public void setName(String name)
    {
    	this.name = name;
    }

    /**
     * In-depth validation that requires building the repository for validating
     * the OWL and mapping files.
     */
    @Override
    public void validate()
        throws RepositoryConfigException
    {
        buildRepository();
    }

    /**
     * Checks that the fields are not missing, and that files exist and are accessible.
     */
    private void validateFields() throws RepositoryConfigException {
        if (quest_type == null || quest_type.isEmpty()) {
            throw new RepositoryConfigException("No type specified for repository implementation.");
        }
        try {
            /**
             * Ontology file
             */
            if (owlFile == null) {
                throw new RepositoryConfigException("No Owl file specified for repository creation!");
            }
            if ((!owlFile.exists())) {
                throw new RepositoryConfigException(String.format("The Owl file %s does not exist!",
                        owlFile.getAbsolutePath()));
            }
            if (!owlFile.canRead()) {
                throw new RepositoryConfigException(String.format("The Owl file %s is not accessible!",
                        owlFile.getAbsolutePath()));
            }

            /**
             * Mapping file
             */
            if (quest_type.equals(VIRTUAL_QUEST_TYPE)) {
                if (obdaFile == null) {
                    throw new RepositoryConfigException(String.format("No mapping file specified for repository creation " +
                            "in %s mode!", quest_type));
                }
                if (!obdaFile.exists()) {
                    throw new RepositoryConfigException(String.format("The mapping file %s does not exist!",
                            obdaFile.getAbsolutePath()));
                }
                if (!obdaFile.canRead()) {
                    throw new RepositoryConfigException(String.format("The mapping file %s is not accessible!",
                            obdaFile.getAbsolutePath()));
                }
            }
        }
        /**
         * Sometimes thrown when there is no access right to the files.
         */
        catch (SecurityException e) {
            throw new RepositoryConfigException(e.getMessage());
        }
    }

    /**
     * This method has two roles:
     *   - Validating in depth the configuration : basic field validation + consistency of
     *     the OWL and mapping files (the latter is done when initializing the repository).
     *   - Building the repository (as an ordinary factory).
     *
     * This method is usually called two times:
     *   - At validation time (see validate() ).
     *   - At the repository construction time (see SesameRepositoryFactory.getRepository() ).
     *
     * However, the repository is only build once and then kept in cache.
     */
    public SesameAbstractRepo buildRepository() throws RepositoryConfigException {
        /**
         * Cache (computed only once)
         */
        if (repository != null)
            return repository;

        /**
         * Common validation.
         * May throw a RepositoryConfigException
         */
        validateFields();

        try {
            /**
             * Creates the repository according to the Quest type.
             */
            QuestConfiguration configuration;
            Properties p = new Properties();
            if (existential) {
                p.setProperty(QuestCorePreferences.REWRITE, "true");
            } else {
                p.setProperty(QuestCorePreferences.REWRITE, "false");
            }
            if (rewriting.equals("TreeWitness")) {
                p.setProperty(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
            } else if (rewriting.equals("Default")) {
                p.setProperty(QuestCorePreferences.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
            }

            switch (quest_type) {
                case IN_MEMORY_QUEST_TYPE:
                    p.setProperty(QuestCorePreferences.ABOX_MODE, QuestConstants.CLASSIC);
                    p.setProperty(QuestCorePreferences.OPTIMIZE_EQUIVALENCES, "true");
                    p.setProperty(QuestCorePreferences.OBTAIN_FROM_MAPPINGS, "false");
                    p.setProperty(QuestCorePreferences.OBTAIN_FROM_ONTOLOGY, "false");
                    p.setProperty(QuestCorePreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
                    p.setProperty(QuestCorePreferences.STORAGE_LOCATION, QuestConstants.INMEMORY);

                    configuration = QuestConfiguration.defaultBuilder()
                            .ontologyFile(owlFile)
                            .properties(p)
                            .build();
                    repository = new SesameClassicRepo(name, configuration);

                    break;
                case REMOTE_QUEST_TYPE:
                    // TODO: rewriting not considered in the ported code. Should we consider it?
                    p = new Properties();

                    p.setProperty(QuestCorePreferences.ABOX_MODE, QuestConstants.CLASSIC);
                    p.setProperty(QuestCorePreferences.OPTIMIZE_EQUIVALENCES, "true");
                    // TODO: no mappings, so this option looks inconsistent
                    p.setProperty(QuestCorePreferences.OBTAIN_FROM_MAPPINGS, "true");
                    p.setProperty(QuestCorePreferences.OBTAIN_FROM_ONTOLOGY, "false");
                    p.setProperty(QuestCorePreferences.DBTYPE, QuestConstants.SEMANTIC_INDEX);
                    p.setProperty(QuestCorePreferences.STORAGE_LOCATION, QuestConstants.JDBC);
                    p.setProperty(OBDAProperties.JDBC_DRIVER, "org.h2.Driver");
                    p.setProperty(OBDAProperties.JDBC_URL, "jdbc:h2:mem:stockclient1");
                    p.setProperty(OBDAProperties.DB_USER, "sa");
                    p.setProperty(OBDAProperties.DB_PASSWORD, "");

                    configuration = QuestConfiguration.defaultBuilder()
                            .ontologyFile(owlFile)
                            .properties(p)
                            .build();
                    repository = new SesameClassicRepo(name, configuration);
                    break;
                case VIRTUAL_QUEST_TYPE:
                    configuration = QuestConfiguration.defaultBuilder()
                            // TODO: consider also r2rml
                            .nativeOntopMappingFile(obdaFile)
                            .ontologyFile(owlFile)
                            .properties(p)
                            .build();
                    repository = new SesameVirtualRepo(name, configuration);
                    break;
                default:
                    throw new RepositoryConfigException("Unknown mode: " + quest_type);
            }
        }
        /**
         * Problem during the repository instantiation.
         *   --> Exception is re-thrown as a RepositoryConfigException.
         */
        catch(Exception e)
        {   e.printStackTrace();
            throw new RepositoryConfigException("Could not create Sesame Repo! Reason: " + e.getMessage());
        }
        return repository;
    }


    @Override
    public Resource export(Graph graph) {
    	Resource implNode = super.export(graph);
    	
        ValueFactory vf = graph.getValueFactory();

        if (quest_type != null) {
            graph.add(implNode, QUEST_TYPE, vf.createLiteral(quest_type));
        }
        if (name != null) {
            graph.add(implNode, NAME, vf.createLiteral(name));
        }
        if (owlFile != null) {
            graph.add(implNode, OWLFILE, vf.createLiteral(owlFile.getAbsolutePath()));
        }
        if (obdaFile != null) {
            graph.add(implNode, OBDAFILE, vf.createLiteral(obdaFile.getAbsolutePath()));
        }

        graph.add(implNode, EXISTENTIAL, vf.createLiteral(existential));

        if (rewriting != null) {
            graph.add(implNode, REWRITING, vf.createLiteral(rewriting));
        }
      
        return implNode;
    }

    @Override
    public void parse(Graph graph, Resource implNode)
            throws RepositoryConfigException
    {
    	super.parse(graph, implNode);
            try {
                Literal typeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, REPOSITORYTYPE);
                if (typeLit != null) {
                    setType(typeLit.getLabel());
                }
                Literal qtypeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, QUEST_TYPE);
                if (qtypeLit != null) {
                    this.quest_type = qtypeLit.getLabel();
                }
                Literal name = GraphUtil.getOptionalObjectLiteral(graph, implNode, NAME);
                if (name != null) {
                    setName(name.getLabel());
                }
                Literal owlfile = GraphUtil.getOptionalObjectLiteral(graph, implNode, OWLFILE);
                if (owlfile != null) {
                    this.owlFile = new File(owlfile.getLabel());
                }
                Literal obdafile = GraphUtil.getOptionalObjectLiteral(graph, implNode, OBDAFILE);
                if (obdafile != null) {
                    this.obdaFile = new File(obdafile.getLabel());
                }
                Literal existl = GraphUtil.getOptionalObjectLiteral(graph, implNode, EXISTENTIAL);
                if (existl != null) {
                    this.existential = existl.booleanValue();
                }
                Literal rewr = GraphUtil.getOptionalObjectLiteral(graph, implNode, REWRITING);
                if (rewr != null) {
                    this.rewriting = rewr.getLabel();
                }
                
            }
            catch (GraphUtilException e) {
                throw new RepositoryConfigException(e.getMessage(), e);
            }
    }

}
