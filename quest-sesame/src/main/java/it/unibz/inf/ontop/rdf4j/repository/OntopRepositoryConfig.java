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

import it.unibz.inf.ontop.injection.OBDASettings;
import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.repository.config.AbstractRepositoryImplConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;

import java.io.File;
import java.util.Properties;

import static org.eclipse.rdf4j.model.util.Models.objectLiteral;
import static org.eclipse.rdf4j.repository.config.RepositoryConfigSchema.REPOSITORYID;
import static org.eclipse.rdf4j.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;


public class OntopRepositoryConfig extends AbstractRepositoryImplConfig {

    public static final String NAMESPACE = "http://inf.unibz.it/krdb/obda/quest#";

    /** <tt>http://inf.unibz.it/krdb/obda/quest#quest_type</tt> */
    public final static IRI QUEST_TYPE;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#owlFile/tt> */
    public final static IRI OWLFILE;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#obdaFile</tt> */
    public final static IRI OBDAFILE;
    
    public final static IRI EXISTENTIAL;
    
    public final static IRI REWRITING;

    public final static String VIRTUAL_QUEST_TYPE = "ontop-virtual";

    @Deprecated
    public final static String REMOTE_QUEST_TYPE = "ontop-remote";

    public final static String IN_MEMORY_QUEST_TYPE = "ontop-inmemory";
    
    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        QUEST_TYPE = factory.createIRI(NAMESPACE, "quest_type");
        //NAME = factory.createIRI(NAMESPACE, "repo_name");
        //REPO_ID = factory.createIRI("http://www.openrdf.org/config/repository#repositoryID");
        OWLFILE = factory.createIRI(NAMESPACE, "owlfile");
        OBDAFILE = factory.createIRI(NAMESPACE, "obdafile");
        EXISTENTIAL = factory.createIRI(NAMESPACE, "existential");
        REWRITING = factory.createIRI(NAMESPACE, "rewriting");
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
    private AbstractOntopRepository repository;

    /**
     * Creates a new RepositoryConfigImpl.
     */
    public OntopRepositoryConfig() {
    	super(OntopRepositoryFactory.REPOSITORY_TYPE);
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
            /*
             * Ontology file
             */
            if (owlFile == null) {
                throw new RepositoryConfigException("No OWL file specified for repository creation!");
            }
            if ((!owlFile.exists())) {
                throw new RepositoryConfigException(String.format("The OWL file %s does not exist!",
                        owlFile.getAbsolutePath()));
            }
            if (!owlFile.canRead()) {
                throw new RepositoryConfigException(String.format("The OWL file %s is not accessible!",
                        owlFile.getAbsolutePath()));
            }

            /*
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
        /*
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
     *   - At the repository construction time (see OntopRepositoryFactory.getRepository() ).
     *
     * However, the repository is only build once and then kept in cache.
     */
    public AbstractOntopRepository buildRepository() throws RepositoryConfigException {
        /*
         * Cache (computed only once)
         */
        if (repository != null)
            return repository;

        /*
         * Common validation.
         * May throw a RepositoryConfigException
         */
        validateFields();

        try {
            /*
             * Creates the repository according to the Quest type.
             */
            QuestConfiguration configuration;
            Properties p = new Properties();
            if (existential) {
                p.setProperty(QuestCoreSettings.REWRITE, "true");
            } else {
                p.setProperty(QuestCoreSettings.REWRITE, "false");
            }
            if (rewriting.equals("TreeWitness")) {
                p.setProperty(QuestCoreSettings.REFORMULATION_TECHNIQUE, QuestConstants.TW);
            } else if (rewriting.equals("Default")) {
                p.setProperty(QuestCoreSettings.REFORMULATION_TECHNIQUE, QuestConstants.UCQBASED);
            }

            switch (quest_type) {
                case IN_MEMORY_QUEST_TYPE:
                    p.setProperty(QuestCoreSettings.ABOX_MODE, QuestConstants.CLASSIC);
                    p.setProperty(QuestCoreSettings.OPTIMIZE_EQUIVALENCES, "true");
                    p.setProperty(QuestCoreSettings.OBTAIN_FROM_MAPPINGS, "false");
                    p.setProperty(QuestCoreSettings.OBTAIN_FROM_ONTOLOGY, "false");
                    p.setProperty(QuestCoreSettings.DBTYPE, QuestConstants.SEMANTIC_INDEX);
                    p.setProperty(QuestCoreSettings.STORAGE_LOCATION, QuestConstants.INMEMORY);

                    configuration = QuestConfiguration.defaultBuilder()
                            .ontologyFile(owlFile)
                            .properties(p)
                            .build();
                    repository = new OntopClassicRepository(name, configuration);

                    break;
                case REMOTE_QUEST_TYPE:
                    // TODO: rewriting not considered in the ported code. Should we consider it?
                    p = new Properties();

                    p.setProperty(QuestCoreSettings.ABOX_MODE, QuestConstants.CLASSIC);
                    p.setProperty(QuestCoreSettings.OPTIMIZE_EQUIVALENCES, "true");
                    // TODO: no mappings, so this option looks inconsistent
                    p.setProperty(QuestCoreSettings.OBTAIN_FROM_MAPPINGS, "true");
                    p.setProperty(QuestCoreSettings.OBTAIN_FROM_ONTOLOGY, "false");
                    p.setProperty(QuestCoreSettings.DBTYPE, QuestConstants.SEMANTIC_INDEX);
                    p.setProperty(QuestCoreSettings.STORAGE_LOCATION, QuestConstants.JDBC);
                    p.setProperty(OBDASettings.JDBC_DRIVER, "org.h2.Driver");
                    p.setProperty(OBDASettings.JDBC_URL, "jdbc:h2:mem:stockclient1");
                    p.setProperty(OBDASettings.JDBC_USER, "sa");
                    p.setProperty(OBDASettings.JDBC_PASSWORD, "");

                    configuration = QuestConfiguration.defaultBuilder()
                            .ontologyFile(owlFile)
                            .properties(p)
                            .build();
                    repository = new OntopClassicRepository(name, configuration);
                    break;
                case VIRTUAL_QUEST_TYPE:
                    configuration = QuestConfiguration.defaultBuilder()
                            // TODO: consider also r2rml
                            .nativeOntopMappingFile(obdaFile)
                            .ontologyFile(owlFile)
                            .properties(p)
                            .build();
                    repository = new OntopVirtualRepository(name, configuration);
                    break;
                default:
                    throw new RepositoryConfigException("Unknown mode: " + quest_type);
            }
        }
        /*
         * Problem during the repository instantiation.
         *   --> Exception is re-thrown as a RepositoryConfigException.
         */
        catch(Exception e)
        {   e.printStackTrace();
            throw new RepositoryConfigException("Could not create RDF4J Repository! Reason: " + e.getMessage());
        }
        return repository;
    }


    @Override
    public Resource export(Model graph) {
    	Resource implNode = super.export(graph);
    	
        ValueFactory vf = SimpleValueFactory.getInstance();

        if (quest_type != null) {
            graph.add(implNode, QUEST_TYPE, vf.createLiteral(quest_type));
        }
//        if (name != null) {
//            graph.add(implNode, REPO_ID, vf.createLiteral(name));
//        }
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
    public void parse(Model graph, Resource implNode)
            throws RepositoryConfigException {
        super.parse(graph, implNode);
        try {
            // use repositoryID as name
            objectLiteral(graph.filter(null, REPOSITORYID, null))
                    .ifPresent(lit -> this.setName(lit.getLabel()));

            objectLiteral(graph.filter(implNode, REPOSITORYTYPE, null))
                    .ifPresent(lit -> setType(lit.getLabel()));

            objectLiteral(graph.filter(implNode, QUEST_TYPE, null))
                    .ifPresent(lit -> this.quest_type = lit.getLabel());

            objectLiteral(graph.filter(implNode, OWLFILE, null))
                    .ifPresent(lit -> this.owlFile = new File(lit.getLabel()));

            objectLiteral(graph.filter(implNode, OBDAFILE, null))
                    .ifPresent(lit -> this.obdaFile = new File(lit.getLabel()));

            objectLiteral(graph.filter(implNode, EXISTENTIAL, null))
                    .ifPresent(lit -> this.existential = lit.booleanValue());

            objectLiteral(graph.filter(implNode, REWRITING, null))
                    .ifPresent(lit -> this.rewriting = lit.getLabel());

        } catch (Exception e) {
            throw new RepositoryConfigException(e.getMessage(), e);
        }
    }

}
