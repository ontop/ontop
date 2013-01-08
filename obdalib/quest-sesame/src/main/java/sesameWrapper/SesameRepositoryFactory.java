package sesameWrapper;
import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;

public class SesameRepositoryFactory implements RepositoryFactory{

	public static final String REPOSITORY_TYPE = "obda:QuestRepository";
   
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public SesameRepositoryConfig getConfig() {
		return new SesameRepositoryConfig();
	}
	
	
	public SesameAbstractRepo getRepository(RepositoryImplConfig config)
			throws RepositoryConfigException {
		// TODO Auto-generated method stub
		
		if (config instanceof SesameRepositoryConfig)
		{
			try{
			if (!config.getType().isEmpty())
			{
				config.validate();
					String name = ((SesameRepositoryConfig) config).getName();
					String owlfile = ((SesameRepositoryConfig) config).getOwlFile();
					boolean existential = ((SesameRepositoryConfig) config).getExistential();
					String rewriting = ((SesameRepositoryConfig) config).getRewriting();
					
					if (((SesameRepositoryConfig) config).getQuestType().equals("quest-inmemory"))
					{
						return new SesameClassicInMemoryRepo(name, owlfile, existential, rewriting);
					}
					else if (((SesameRepositoryConfig) config).getQuestType().equals("quest-remote"))
					{
						return new SesameClassicJDBCRepo(name, owlfile);
					}
					else if (((SesameRepositoryConfig) config).getQuestType().equals("quest-virtual")) 
					{
						String obdafile = ((SesameRepositoryConfig) config).getObdaFile();
						return new SesameVirtualRepo(name, owlfile, obdafile, existential, rewriting);
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
