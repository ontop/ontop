package inf.unibz.it.sql.parser;

public class SQLSelection {

	private String term = null;
	private String tablename= null;
	private String alias = null;
	
	public SQLSelection(String tn, String t, String a){
		tablename = tn;
		term = t;
		alias = a;
	}

	public String getName() {
		return term;
	}

	public String getSelectedVariable() {
		return tablename;
	}

	public String getAlias() {
		return alias;
	}
	
	public String toString(){
		String aux = "";
		if(tablename != null){
			aux = aux + tablename+".";
		}
		aux = aux + term;
		if(alias != null){
			aux = aux + " as " + alias;
		}
		return aux;
	}
}
