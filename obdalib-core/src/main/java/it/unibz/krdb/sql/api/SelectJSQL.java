package it.unibz.krdb.sql.api;

import java.io.Serializable;

import net.sf.jsqlparser.statement.select.SubSelect;

public class SelectJSQL implements Serializable{
		

	private static final long serialVersionUID = 6565489073454036936L;
		/**
		 * Class SelectJSQL used to store the information about the subselect in the query. We distinguish between givenName and Name.
		 * Since with Name we don't want to consider columns.
		 */
		
		private String body;
		private String alias;
		
		
		public SelectJSQL(String subSelect, String alias) {
			setAlias(alias);
			setBody(subSelect);

		}
		
		public SelectJSQL(SubSelect sSelect){
			setAlias(sSelect.getAlias());
			setBody(sSelect.getSelectBody().toString());

		}

		
		public void setAlias(String alias) {
			if (alias == null) {
				return;
			}
			this.alias = alias;
		}

		public String getAlias() {
			return alias;
		}
		
		public void setBody(String string) {
			if (string == null) {
				return;
			}
			this.body = string;
		}
		
		public String getBody() {
			return body;
		}
		
		@Override
		public String toString() {

			return body;
		}

		/**
		 * Called from the MappingParser:getTables. 
		 * Needed to remove duplicates from the list of tables
		 */
		@Override
		public boolean equals(Object t){
			if(t instanceof SelectJSQL){
				SelectJSQL tp = (SelectJSQL) t;
				return this.body.equals(tp.getBody())
						&& ((this.alias == null && tp.getAlias() == null)
								|| this.alias.equals(tp.getAlias())
								);
			}
			return false;
		}

		
	}
