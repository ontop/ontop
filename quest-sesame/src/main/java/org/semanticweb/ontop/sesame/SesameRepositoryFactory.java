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
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;

public class SesameRepositoryFactory implements RepositoryFactory {

    public static final String REPOSITORY_TYPE = "obda:OntopRepository";

    public String getRepositoryType() {
        return REPOSITORY_TYPE;
    }

    public SesameRepositoryConfig getConfig() {
        return new SesameRepositoryConfig();
    }


    /**
     *
     */
    public SesameAbstractRepo getRepository(RepositoryImplConfig config)
            throws RepositoryConfigException {
        if (!(config instanceof SesameRepositoryConfig)) {
            throw new RepositoryConfigException("The given repository config is not of the right type " +
                    "(SesameRepositoryConfig was expected).");
        }
        /**
         * Construction is delegated to the config object (for validation purposes).
         * See SesameRepositoryConfig for further explanations about this unusual pattern.
         */
        return ((SesameRepositoryConfig) config).buildRepository();
    }
}
