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

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.QuestCoreSettings;
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

    /** <tt>http://inf.unibz.it/krdb/obda/quest#owlFile/tt> */
    public final static IRI OWLFILE;

    /** <tt>http://inf.unibz.it/krdb/obda/quest#obdaFile</tt> */
    public final static IRI OBDAFILE;
    
    public final static IRI EXISTENTIAL;
    
    static {
        ValueFactory factory = ValueFactoryImpl.getInstance();
        //NAME = factory.createIRI(NAMESPACE, "repo_name");
        //REPO_ID = factory.createIRI("http://www.openrdf.org/config/repository#repositoryID");
        OWLFILE = factory.createIRI(NAMESPACE, "owlfile");
        OBDAFILE = factory.createIRI(NAMESPACE, "obdafile");
        EXISTENTIAL = factory.createIRI(NAMESPACE, "existential");
    }

    private String name;
    private File owlFile;
    private File obdaFile;
    private boolean existential;

    /**
     * The repository has to be built by this class
     * so as to fit the validation and repository instantiation
     * workflow of Sesame.
     */
    private OntopVirtualRepository repository;

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
            if (obdaFile == null) {
                throw new RepositoryConfigException(String.format("No mapping file specified for repository creation "));
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
    public OntopVirtualRepository buildRepository() throws RepositoryConfigException {
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
                p.setProperty(QuestCoreSettings.EXISTENTIAL_REASONING, "true");
            } else {
                p.setProperty(QuestCoreSettings.EXISTENTIAL_REASONING, "false");
            }

            configuration = QuestConfiguration.defaultBuilder()
                    // TODO: consider also r2rml
                    .nativeOntopMappingFile(obdaFile)
                    .ontologyFile(owlFile)
                    .properties(p)
                    .build();
            repository = new OntopVirtualRepository(configuration);

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

            objectLiteral(graph.filter(implNode, OWLFILE, null))
                    .ifPresent(lit -> this.owlFile = new File(lit.getLabel()));

            objectLiteral(graph.filter(implNode, OBDAFILE, null))
                    .ifPresent(lit -> this.obdaFile = new File(lit.getLabel()));

            objectLiteral(graph.filter(implNode, EXISTENTIAL, null))
                    .ifPresent(lit -> this.existential = lit.booleanValue());

        } catch (Exception e) {
            throw new RepositoryConfigException(e.getMessage(), e);
        }
    }

}
