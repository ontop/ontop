package sesameWrapper;

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

import static sesameWrapper.SesameRepositoryConfig.*;

public class SesameRepositoryFactory implements RepositoryFactory{

	public static final String REPOSITORY_TYPE = "obda:OntopRepository";
   
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public SesameRepositoryConfig getConfig() {
		return new SesameRepositoryConfig();
	}
	
	
	public SesameAbstractRepo getRepository(RepositoryImplConfig config)
			throws RepositoryConfigException {
		
		if (config instanceof SesameRepositoryConfig)
		{
			try{
			if (!config.getType().isEmpty())
			{
				config.validate();
					String name = ((SesameRepositoryConfig) config).getName();
					String owlFileName = ((SesameRepositoryConfig) config).getOwlFile().getAbsolutePath();
					boolean existential = ((SesameRepositoryConfig) config).getExistential();
					String rewriting = ((SesameRepositoryConfig) config).getRewriting();
					
					if (((SesameRepositoryConfig) config).getQuestType().equals(IN_MEMORY_QUEST_TYPE))
					{
						return new SesameClassicInMemoryRepo(name, owlFileName, existential, rewriting);
					}
					else if (((SesameRepositoryConfig) config).getQuestType().equals(REMOTE_QUEST_TYPE))
					{
						return new SesameClassicJDBCRepo(name, owlFileName);
					}
					else if (((SesameRepositoryConfig) config).getQuestType().equals(VIRTUAL_QUEST_TYPE))
					{
						String obdaFileName = ((SesameRepositoryConfig) config).getObdaFile().getAbsolutePath();
						return new SesameVirtualRepo(name, owlFileName, obdaFileName, existential, rewriting);
					}
			}}
			catch(Exception e)
			{e.printStackTrace();
			throw new RepositoryConfigException("Could not create Sesame Repo!");
			}
		}
		else 
		{
	        throw new RepositoryConfigException("Invalid configuration class: " + config.getClass());
		}
		return null;
	}

}
