package inf.unibz.it.obda.dependencies.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertionController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.domain.DataSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;



/** The disjointness dependency controller manages the insertion, 
* deletion and update of disjointness assertion. 
* 
* @author Manfred Gerstgrasser
* 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
*/

public class RDBMSInclusionDependencyController extends
	AbstractDependencyAssertionController<RDBMSInclusionDependency> {

	private DataSource currentDataSource = null;
	private HashMap<URI, HashSet<RDBMSInclusionDependency>> inclusionDependencies= null;
	
	/**
	 * Creates a new instance of the RDBMSInclusionDependencyController.
	 */
	public RDBMSInclusionDependencyController (){
		inclusionDependencies = new HashMap<URI, HashSet<RDBMSInclusionDependency>>();
		
	}
	
	/**
	 * Returns a new instance of the RDBMSInclusionDependencyController.
	 */
	@Override
	public AssertionController<RDBMSInclusionDependency> getInstance() {
		return new RDBMSInclusionDependencyController();
	}

	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("datasource_uri");
		return attributes;
	}

	/**
	 * Returns the tag name, with which the assertions are stored in
	 * the obda file
	 */
	public String getElementTag() {
		
		return "RDBMSInclusionDependencies";
	}
	
	/**
	 * Adds the given assertion and fires an assertion added event to all
	 * listeners.
	 */
	@Override
	public void addAssertion(RDBMSInclusionDependency a) {
		if(a == null){
			return;
		}
		HashSet<RDBMSInclusionDependency> aux = inclusionDependencies.get(currentDataSource.getSourceID());
		if(aux != null && aux.add(a)){
			inclusionDependencies.put(currentDataSource.getSourceID(), aux);
			fireAssertionAdded(a);
		}
	}
	
	/**
	 * Removes the given assertion and fires an assertion removed event to all
	 * listeners.
	 */
	@Override
	public void removeAssertion(RDBMSInclusionDependency a) {
		HashSet<RDBMSInclusionDependency> aux = inclusionDependencies.get(currentDataSource.getSourceID());
		if(aux != null&&aux.remove(a)){
			inclusionDependencies.put(currentDataSource.getSourceID(), aux);
			fireAssertionRemoved(a);
		}
	}

	/**
	 * Is executed when the data source listener gets an all data source
	 * deleted event. All assertion are removed. Fires a assertion removed
	 * event to remove also the currently shown assertion from the UI.
	 */
	public void alldatasourcesDeleted() {
		HashSet<RDBMSInclusionDependency> list = inclusionDependencies.get(currentDataSource.getSourceID());
		Iterator<RDBMSInclusionDependency> it = list.iterator();
		while(it.hasNext()){
			fireAssertionRemoved(it.next());
		}
		inclusionDependencies = new HashMap<URI, HashSet<RDBMSInclusionDependency>>();
		
	}

  public void changeDatasource(DataSource oldSource, DataSource newSource) {
    
    currentDataSource = newSource;
    if (oldSource != newSource) {
      if (oldSource != null) {
        HashSet<RDBMSInclusionDependency> list = 
            inclusionDependencies.get(oldSource.getSourceID());
        Iterator<RDBMSInclusionDependency> it = list.iterator();
        while (it.hasNext()) {
          fireAssertionRemoved(it.next());
        }
      }
      if (newSource != null) {
        HashSet<RDBMSInclusionDependency> list1 = 
            inclusionDependencies.get(newSource.getSourceID());
        Iterator<RDBMSInclusionDependency> it1 = list1.iterator();
        while (it1.hasNext()) {
          fireAssertionAdded(it1.next());
        }
      }
    }
  }
	
	/**
	 * Is executed when the listener gets a datasource removed event.
	 * The method removes the assertion of the old data source from the
	 * UI and shows the assertions associated to the new data soruce
	 */
	@Deprecated
	public void currentDatasourceChange(DataSource previousdatasource,
			DataSource currentsource) {
//  TODO Remove this abstract method from the DatasourcesControllerListener
//		currentDataSource = currentsource;
//		if(previousdatasource != currentsource){
//			if(previousdatasource != null){
//				HashSet<RDBMSInclusionDependency> list = inclusionDependencies.get(previousdatasource.getSourceID());
//				Iterator<RDBMSInclusionDependency> it = list.iterator();
//				while(it.hasNext()){
//					fireAssertionRemoved(it.next());
//				}
//			}
//			if(currentsource != null){
//				HashSet<RDBMSInclusionDependency> list1 = inclusionDependencies.get(currentsource.getSourceID());
//				Iterator<RDBMSInclusionDependency> it1 = list1.iterator();
//				while(it1.hasNext()){
//					fireAssertionAdded(it1.next());
//				}
//			}
//		}
	}

	/**
	 * Add a new data source the Map. Is executed when the listener
	 * gets a data source added event.
	 */
	public void datasourceAdded(DataSource source) {
		
		inclusionDependencies.put(source.getSourceID(), new HashSet<RDBMSInclusionDependency>());
	}

	/**
	 * Removes all assertions from the controller associated to the
	 * given data source. Is executed when the data source listener gets
	 * a data source delete event.
	 */
	public void datasourceDeleted(DataSource source) {
		
		if(currentDataSource == source){
			HashSet<RDBMSInclusionDependency> list = inclusionDependencies.get(currentDataSource.getSourceID());
			Iterator<RDBMSInclusionDependency> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		inclusionDependencies.remove(source.getSourceID());
		
	}

	/**
	 * Updates the name of the data source to the given new one.
	 * The method is executed when the data source listener gets a 
	 * data source updated event.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		HashSet<RDBMSInclusionDependency> aux = inclusionDependencies.get(oldname);
		inclusionDependencies.remove(oldname);
		inclusionDependencies.put(currendata.getSourceID(), aux);
		currentDataSource = currendata;
		
	}

	/**
	 * Returns all inclusion dependency assertions associated to the
	 * currently selected data source.
	 */
	@Override
	public HashSet<RDBMSInclusionDependency> getDependenciesForCurrentDataSource() {
		// TODO Auto-generated method stub
		if(currentDataSource !=null){
			return inclusionDependencies.get(currentDataSource.getSourceID());
		}else{
			return new HashSet<RDBMSInclusionDependency>();
		}
	}
	
	/**
	 * Returns all inclusion dependency assertion in this controller.
	 */
	@Override
	public Collection<RDBMSInclusionDependency> getAssertions() {
		
		Vector<RDBMSInclusionDependency> assertions = new Vector<RDBMSInclusionDependency>();
		Set<URI>keys = inclusionDependencies.keySet();
		Iterator<URI> it = keys.iterator();
		while(it.hasNext()){
			HashSet<RDBMSInclusionDependency> aux = inclusionDependencies.get(it.next());
			assertions.addAll(aux);
		}
		
		return assertions;
	}

	/**
	 * Adds a inclusion dependency assertion to the controller without updating 
	 * the UI.
	 * @param a the RDBMSInclusionDependency to add
	 */
	public boolean insertAssertion(RDBMSInclusionDependency a) {
		HashSet<RDBMSInclusionDependency> aux = inclusionDependencies.get(currentDataSource.getSourceID());
		if(aux != null){
			if(aux.add(a)){
				inclusionDependencies.put(currentDataSource.getSourceID(), aux);
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	

	/**
	 * Returns all RDBMSDisjointnessDependency for the given data source uri
	 */
	@Override
	public HashSet<RDBMSInclusionDependency> getAssertionsForDataSource(URI uri) {
	
		 return inclusionDependencies.get(uri); 
	}
	
	@Override
	public void datasourcParametersUpdated() {}
}
