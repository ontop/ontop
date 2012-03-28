package sesameWrapper;
import java.io.File;
import java.net.URI;

import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.OWLOntology;

import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBClassicStore;

public abstract class SesameClassicRepo extends SesameAbstractRepo{

	protected QuestDBClassicStore classicStore;

	public SesameClassicRepo() 
	{
		super();
		
	}
	
	protected void createStore(String name, String tboxFile, QuestPreferences config) throws Exception 
	{
		if (config.getProperty(QuestPreferences.ABOX_MODE) != QuestConstants.CLASSIC)
			throw new RepositoryException("Must be in classic mode!");
		
		this.classicStore = new QuestDBClassicStore(name, (new File(tboxFile)).toURI(), config);
	}
	
	
	
	
	
	public void initialize() throws RepositoryException
	{
		super.initialize();
		try {
			classicStore.getConnection();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public QuestDBConnection getQuestConnection() throws OBDAException
	{
		return classicStore.getConnection();
	}
	
	@Override
	public boolean isWritable() throws RepositoryException {
		//Checks whether this repository is writable, i.e. 
		//if the data contained in this repository can be changed. 
		//The writability of the repository is determined by the writability 
		//of the Sail that this repository operates on. 
		return true;
	}
	
	public  String getType()
	{
		return QuestConstants.CLASSIC;
	}
	
}
