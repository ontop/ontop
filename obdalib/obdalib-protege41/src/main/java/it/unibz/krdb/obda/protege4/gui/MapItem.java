package it.unibz.krdb.obda.protege4.gui;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Predicate;

public class MapItem {

	private PredicateItem predicateItem;
	private String targetMapping = "";
	private Predicate dataType;

	public MapItem(PredicateItem predicate) {
		this.predicateItem = predicate;
	}

	public PrefixManager getPrefixManager() {
		return predicateItem.getPrefixManager();
	}

	public String getName() {
		if (predicateItem == null) {
			return "";
		} else {
			return predicateItem.getFullName();
		}
	}
	
	public Predicate getSourcePredicate() {
		return predicateItem.getSource();
	}

	public void setTargetMapping(String columnOrUriTemplate) {
		targetMapping = columnOrUriTemplate;
	}

	public String getTargetMapping() {
		return targetMapping;
	}
	
	public void setDataType(Predicate type) {
		dataType = type;
	}

	public Predicate getDataType() {
		return dataType;
	}
	
	public boolean isSubjectMap() {
		// A null predicate is assumed to be a class predicate
		return (predicateItem == null || predicateItem.isClassPredicate());
	}

	public boolean isObjectMap() {
		return predicateItem.isDataPropertyPredicate();
	}

	public boolean isRefObjectMap() {
		return predicateItem.isObjectPropertyPredicate();
	}
	
	public boolean isValid() {
		if (predicateItem == null) {
			return false;
		}
		if (targetMapping.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		MapItem other = (MapItem) obj;
		return this.getName() == other.getName();
	}

	@Override
	public String toString() {
		if (predicateItem == null) {
			return "";
		} else {
			return predicateItem.getQualifiedName();
		}
	}
}