package sesameWrapper;
import org.openrdf.repository.RepositoryException;

import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBVirtualStore;

public class SesameVirtualRepo extends SesameAbstractRepo {

	private QuestDBVirtualStore virtualStore;
	
	public SesameVirtualRepo() throws RepositoryException
	{
		super();
	}
	
	@Override
	public boolean isWritable() throws RepositoryException {
		//Checks whether this repository is writable, i.e. 
		//if the data contained in this repository can be changed. 
		//The writability of the repository is determined by the writability 
		//of the Sail that this repository operates on. 
		return false;
	}


}
