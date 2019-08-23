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
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryFactory;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;

public class OntopRepositoryFactory implements RepositoryFactory {

    public static final String REPOSITORY_TYPE = "obda:OntopRepository";

    @Override
    public String getRepositoryType() {
        return REPOSITORY_TYPE;
    }

    @Override
    public OntopRepositoryConfig getConfig() {
        return new OntopRepositoryConfig();
    }


    @Override
    public OntopRepository getRepository(RepositoryImplConfig config)
            throws RepositoryConfigException {
        if (!(config instanceof OntopRepositoryConfig)) {
            throw new RepositoryConfigException("The given repository config is not of the right type " +
                    "(OntopRepositoryConfig was expected).");
        }
        /*
         * Construction is delegated to the config object (for validation purposes).
         * See OntopRepositoryConfig for further explanations about this unusual pattern.
         */
        return ((OntopRepositoryConfig) config).buildRepository();
    }
}
