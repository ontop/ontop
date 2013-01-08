package it.unibz.krdb.obda.protege4.gui;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Predicate;

import java.util.regex.Pattern;

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
		if (hasValidVariableString(targetMapping)) {
			return true;
		}
		if (hasValidURITemplateString(targetMapping)) {
			return true;
		}
		return false;
	}

	private boolean hasValidVariableString(String input) {
		return (input.startsWith("$") || input.startsWith("?"));
	}

	private boolean hasValidURITemplateString(String input) {
		try {
			// Validate 1 : check if the URI template contains <"..."> patterns
			String pattern = "<\"[^\"]*\">";
			if (!input.matches(pattern)) {
				throw new Exception("Invalid URI template string");
			}
			
			// Validate 2 : check if the prefix syntax is correct
			int start = input.indexOf("&");
			int end = input.indexOf(";");

			// Extract the whole prefix placeholder, e.g., "&ex;Book" --> "&ex;"
			String prefixPlaceHolder = input.substring(start, end + 1);

			// Extract the prefix name, e.g., "&ex;" --> "ex:"
			String prefix = prefixPlaceHolder.substring(1, prefixPlaceHolder.length() - 1);
			if (!prefix.equals(":")) {
				prefix = prefix + ":"; // add a colon
			}
			
			// Validate 3 : check if the prefix is known by the system
			String uri = getPrefixManager().getURIDefinition(prefix);
			if (uri == null) {
				throw new Exception("The prefix name is unknown: " + prefix);
			}
			
			// Validate 4 : check if the placeholders have proper enclosing elements
			while (input.contains("{") && input.contains("}")) {
				start = input.indexOf("{");
				end = input.indexOf("}");

				// Extract the whole placeholder, e.g., "{?var}"
				String placeHolder = Pattern.quote(input.substring(start, end+1));
				input = input.replaceFirst(placeHolder, "[]");
				
				// Validate 5 : extract the variable name only, e.g., "{?var}" --> "var"
				String variableName = placeHolder.substring(4, placeHolder.length()-3);
				if (variableName.equals("")) {
					throw new Exception("Variable name must have at least 1 character");
				}
			}
			if (input.contains("{") || input.contains("}")) {
				throw new Exception("Missing enclosing character for the placeholder");
			}
		} catch (Exception e) {
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