package sesameWrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.OWLOntology;

import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestDBConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBClassicStore;
import it.unibz.krdb.obda.owlrefplatform.questdb.QuestDBVirtualStore;

public class SesameVirtualRepo extends SesameAbstractRepo {

	private QuestDBVirtualStore virtualStore;
	
	
	public SesameVirtualRepo(String name, String tboxFile, String obdaFile) throws Exception
	{
		super();
		
		setupRemoteDB();
		
		URI obdaURI = (new File(obdaFile)).toURI();
		this.virtualStore = new QuestDBVirtualStore(name, (new File(tboxFile)).toURI(), obdaURI);
	
		
	}
	
	private void setupRemoteDB() throws Exception
	{
		try {
			//setup virtual mode - remote db
			String driver = "org.postgresql.Driver";
			String url = "jdbc:postgresql://obdalin.inf.unibz.it/quest-junit-db";
			String username = "obda";
			String password = "obda09";

			Connection conn = DriverManager.getConnection(url, username, password);
			Statement st = conn.createStatement();

			FileReader reader = new FileReader("/home/timi/workspace/obdalib-parent/quest-owlapi3/src/test/resources/test/stockexchange-create-postgres.sql");
			BufferedReader in = new BufferedReader(reader);
			StringBuilder bf = new StringBuilder();
			String line = in.readLine();
			while (line != null) {
				bf.append(line);
				line = in.readLine();
			}

			st.executeUpdate(bf.toString());
			//end of setup remote db
			}
			catch(Exception e)
			{
				throw e;
			}
			
	}
	
	@Override
	public OWLOntology getOntology()
	{
		return virtualStore.getOntology();
	}
	
	@Override
	public OBDAModel getOBDAModel()
	{
		return virtualStore.getOBDAModel();
	}
	

	public void doo()
	{
		virtualStore.doo();
	}
	
	public void initialize() throws RepositoryException
	{
		super.initialize();
		try {
			virtualStore.getConnection();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public QuestDBConnection getQuestConnection() throws OBDAException
	{
		return virtualStore.getConnection();
	}
	
	@Override
	public boolean isWritable() throws RepositoryException {
		//Checks whether this repository is writable, i.e. 
		//if the data contained in this repository can be changed. 
		//The writability of the repository is determined by the writability 
		//of the Sail that this repository operates on. 
		return false;
	}
	
	
	public  String getType()
	{
		return QuestConstants.VIRTUAL;
	}
	


}
