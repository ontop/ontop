package inf.unibz.it.obda.constraints.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.AbstractConstraintAssertionController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
import inf.unibz.it.obda.domain.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class RDBMSForeignKeyConstraintController extends
AbstractConstraintAssertionController<RDBMSForeignKeyConstraint> {

	private DataSource currentDataSource = null;
	private HashMap<String, HashSet<RDBMSForeignKeyConstraint>> fkconstraints= null;
	
	public RDBMSForeignKeyConstraintController (){
		fkconstraints = new HashMap<String, HashSet<RDBMSForeignKeyConstraint>>();
		
	}
	
	@Override
	public AssertionController<RDBMSForeignKeyConstraint> getInstance() {
		return new RDBMSForeignKeyConstraintController();
	}

	@Override
	public Collection<String> getAttributes() {
		ArrayList<String> attributes = new ArrayList<String>();
		attributes.add("datasource_uri");
		return attributes;
	}

	@Override
	public String getElementTag() {
		return "RDBMSUniquenessConstraint";
	}
	
	@Override
	public void datasourcParametersUpdated() {}
	
	/**
	 * Adds the given assertion and fires an assertion added event to all
	 * listeners.
	 */
	@Override
	public void addAssertion(RDBMSForeignKeyConstraint u) {
		if(u == null){
			return;
		}
		HashSet<RDBMSForeignKeyConstraint> aux = fkconstraints.get(currentDataSource.getName());
		if(aux != null && aux.add(u)){
			fkconstraints.put(currentDataSource.getName(), aux);
			fireAssertionAdded(u);
		}
	}
	
	/**
	 * Removes the given assertion and fires an assertion removed event to all
	 * listeners.
	 */
	@Override
	public void removeAssertion(RDBMSForeignKeyConstraint u) {
		HashSet<RDBMSForeignKeyConstraint> aux = fkconstraints.get(currentDataSource.getName());
		if(aux != null&&aux.remove(u)){
			fkconstraints.put(currentDataSource.getName(), aux);
			fireAssertionRemoved(u);
		}
	}

	/**
	 * Is executed when the data source listener gets an all data source
	 * deleted event. All assertion are removed. Fires a assertion removed
	 * event to remove also the currently shown assertion from the UI.
	 */
	public void alldatasourcesDeleted() {
		HashSet<RDBMSForeignKeyConstraint> list = fkconstraints.get(currentDataSource.getName());
		Iterator<RDBMSForeignKeyConstraint> it = list.iterator();
		while(it.hasNext()){
			fireAssertionRemoved(it.next());
		}
		fkconstraints = new HashMap<String, HashSet<RDBMSForeignKeyConstraint>>();
		
	}

	/**
	 * Is executed when the listener gets a datasource removed event.
	 * The method removes the assertion of the old data source from the
	 * UI and shows the assertions associated to the new data soruce
	 */
	public void currentDatasourceChange(DataSource previousdatasource,
			DataSource currentsource) {
		currentDataSource = currentsource;
		if(previousdatasource != currentsource){
			if(previousdatasource != null){
				HashSet<RDBMSForeignKeyConstraint> list = fkconstraints.get(previousdatasource.getName());
				Iterator<RDBMSForeignKeyConstraint> it = list.iterator();
				while(it.hasNext()){
					fireAssertionRemoved(it.next());
				}
			}
			if(currentsource != null){
				HashSet<RDBMSForeignKeyConstraint> list1 = fkconstraints.get(currentsource.getName());
				Iterator<RDBMSForeignKeyConstraint> it1 = list1.iterator();
				while(it1.hasNext()){
					fireAssertionAdded(it1.next());
				}
			}
		}
	}

	/**
	 * Add a new data source the Map. Is executed when the listener
	 * gets a data source added event.
	 */
	public void datasourceAdded(DataSource source) {
		
		fkconstraints.put(source.getName(), new HashSet<RDBMSForeignKeyConstraint>());
	}

	/**
	 * Removes all assertions from the controller associated to the
	 * given data source. Is executed when the data source listener gets
	 * a data source delete event.
	 */
	public void datasourceDeleted(DataSource source) {
		
		if(currentDataSource == source){
			HashSet<RDBMSForeignKeyConstraint> list = fkconstraints.get(currentDataSource.getName());
			Iterator<RDBMSForeignKeyConstraint> it = list.iterator();
			while(it.hasNext()){
				fireAssertionRemoved(it.next());
			}
		}
		fkconstraints.remove(source.getName());
		
	}

	/**
	 * Updates the name of the data source to the given new one.
	 * The method is executed when the data source listener gets a 
	 * data source updated event.
	 */
	public void datasourceUpdated(String oldname, DataSource currendata) {
		HashSet<RDBMSForeignKeyConstraint> aux = fkconstraints.get(oldname);
		fkconstraints.remove(oldname);
		fkconstraints.put(currendata.getName(), aux);
		currentDataSource = currendata;
		
	}

	/**
	 * Returns all inclusion dependency assertions associated to the
	 * currently selected data source.
	 */
	@Override
	public HashSet<RDBMSForeignKeyConstraint> getDependenciesForCurrentDataSource() {
		// TODO Auto-generated method stub
		if(currentDataSource !=null){
			return fkconstraints.get(currentDataSource.getName());
		}else{
			return new HashSet<RDBMSForeignKeyConstraint>();
		}
	}
	
	/**
	 * Returns all inclusion dependency assertion in this controller.
	 */
	@Override
	public Collection<RDBMSForeignKeyConstraint> getAssertions() {
		
		Vector<RDBMSForeignKeyConstraint> assertions = new Vector<RDBMSForeignKeyConstraint>();
		Set<String>keys = fkconstraints.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			HashSet<RDBMSForeignKeyConstraint> aux = fkconstraints.get(it.next());
			assertions.addAll(aux);
		}
		
		return assertions;
	}

	/**
	 * Adds a inclusion dependency assertion to the controller without updating 
	 * the UI.
	 * @param a the RDBMSInclusionDependency to add
	 */
	public boolean insertAssertion(RDBMSForeignKeyConstraint a) {
		HashSet<RDBMSForeignKeyConstraint> aux = fkconstraints.get(currentDataSource.getName());
		if(aux != null){
			if(aux.add(a)){
				fkconstraints.put(currentDataSource.getName(), aux);
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
	public HashSet<RDBMSForeignKeyConstraint> getAssertionsForDataSource(String uri) {
	
		 return fkconstraints.get(uri); 
	}

}
