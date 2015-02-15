package org.semanticweb.ontop.sesame;

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

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfigBase;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;



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

  
    
    public String getQuestType() {
        return quest_type;
    }

    public void setQuestType(String quest_type) {
        this.quest_type = quest_type;
    }
    
    public String getName()
    {
    	return name;
    }
    
    public void setName(String name)
    {
    	this.name = name;
    }
    
    public File getOwlFile()
    {
    	return owlFile;
    }

    public void setOwlFile(String fileName)
    {
    	this.owlFile = new File(fileName);
    }
    
    public File getObdaFile()
    {
    	return obdaFile;
    }
    
    public void setObdaFile(String fileName)
    {
    	this.obdaFile = new File(fileName);
    }

    public boolean getExistential()
    {
    	return existential;
    }
    
    public void setExistential(boolean ex)
    {
    	this.existential = ex;
    }
    
    public String getRewriting()
    {
    	return this.rewriting;
    }
    
    public void setRewriting(String rew)
    {
    	this.rewriting = rew;
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
            switch (quest_type) {
                case IN_MEMORY_QUEST_TYPE:
                    repository = new SesameClassicInMemoryRepo(name, owlFile.getAbsolutePath(), existential, rewriting);
                    break;
                case REMOTE_QUEST_TYPE:
                    repository = new SesameClassicJDBCRepo(name, owlFile.getAbsolutePath());
                    break;
                case VIRTUAL_QUEST_TYPE:
                    repository = new SesameVirtualRepo(name, owlFile.getAbsolutePath(), obdaFile.getAbsolutePath(),
                            existential, rewriting);
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
        if (existential == false || existential == true) {
        	graph.add(implNode, EXISTENTIAL, vf.createLiteral(existential));
        }
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
                    setQuestType(qtypeLit.getLabel());
                }
                Literal name = GraphUtil.getOptionalObjectLiteral(graph, implNode, NAME);
                if (name != null) {
                    setName(name.getLabel());
                }
                Literal owlfile = GraphUtil.getOptionalObjectLiteral(graph, implNode, OWLFILE);
                if (owlfile != null) {
                    setOwlFile(owlfile.getLabel());
                }
                Literal obdafile = GraphUtil.getOptionalObjectLiteral(graph, implNode, OBDAFILE);
                if (obdafile != null) {
                    setObdaFile(obdafile.getLabel());
                }
                Literal existl = GraphUtil.getOptionalObjectLiteral(graph, implNode, EXISTENTIAL);
                if (existl != null) {
                    setExistential(existl.booleanValue());
                }
                Literal rewr = GraphUtil.getOptionalObjectLiteral(graph, implNode, REWRITING);
                if (rewr != null) {
                    setRewriting(rewr.getLabel());
                }
                
            }
            catch (GraphUtilException e) {
                throw new RepositoryConfigException(e.getMessage(), e);
            }
    }

}
