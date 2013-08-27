package it.unibz.krdb.obda.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAQuery;
import it.unibz.krdb.obda.model.OBDARDBMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.api.ColumnReference;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.DerivedColumn;
import it.unibz.krdb.sql.api.IValueExpression;
import it.unibz.krdb.sql.api.Projection;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.ReferenceValueExpression;
import it.unibz.krdb.sql.api.RelationalAlgebra;
import it.unibz.krdb.sql.api.Selection;
import it.unibz.krdb.sql.api.StringLiteral;

public class MetaMappingExpander {

	private DBMetadata metadata;
	private Connection connection;
	private SQLQueryTranslator translator;
	private List<OBDAMappingAxiom> expandedMappings;

	public MetaMappingExpander(Connection connection, DBMetadata metadata) {
		this.connection = connection;
		this.metadata = metadata;
		translator = new SQLQueryTranslator(metadata);
		expandedMappings = new ArrayList<OBDAMappingAxiom>();

	}

	public List<OBDAMappingAxiom> expand(ArrayList<OBDAMappingAxiom> mappings) {
		
		for (OBDAMappingAxiom mapping : mappings) {

			CQIE targetQuery = (CQIE) mapping.getTargetQuery();
			List<Function> body = targetQuery.getBody();

			OBDASQLQuery sourceQuery = (OBDASQLQuery)mapping.getSourceQuery();

			Function firstBodyAtom = body.get(0);
			
			if (!firstBodyAtom.getFunctionSymbol().equals(OBDAVocabulary.QUEST_TRIPLE_PRED)){
				/**
				 * for normal mappings, we do not need to expand.
				 */
				expandedMappings.add(mapping);
			} else {
				/*
				 * q(X, Class) :- triple(X, a, Class).
				 */
				
				// Construct the SQL query tree from the source query
				QueryTree sourceQueryTree = translator.contructQueryTree(sourceQuery.toString());
				
				ArrayList<DerivedColumn> columnList = sourceQueryTree.getProjection().getColumnList();
				
				Projection distinctClassesProject = new Projection();
				
				distinctClassesProject.setType(Projection.SELECT_DISTINCT);
				/**
				 * 'Class' is at position 1 of 'q(X, Class)'.
				 */
				distinctClassesProject.add(columnList.get(1));
				
				RelationalAlgebra ra = sourceQueryTree.value().clone();
				ra.setProjection(distinctClassesProject);
				
				QueryTree distinctQueryTree = new QueryTree(ra, sourceQueryTree.left(), sourceQueryTree.right());
				
				String distinctClassesSQL = distinctQueryTree.toString();

				Set<String> classes = new HashSet<String>();
				
				Statement st;
				try {
					st = connection.createStatement();
					ResultSet rs = st.executeQuery(distinctClassesSQL);
					while(rs.next()){
						String cls = rs.getString(1);
						classes.add(cls);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				
				OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();  
				
				for(String cls : classes) {
					Function newTargetHead = dfac.getFunction(targetQuery.getHead().getFunctionSymbol(), targetQuery.getHead().getTerm(0));
					Function newTargetBody = dfac.getFunction(dfac.getPredicate(cls,1), targetQuery.getBody().get(0).getTerm(0));
					CQIE newTargetQuery = dfac.getCQIE(newTargetHead, newTargetBody);
					
					Selection selection = sourceQueryTree.getSelection();
					Selection newSelection;
					if(selection != null){
						newSelection = selection.clone();
					} else {
						newSelection = new Selection();
					}
					
					IValueExpression columnRefExpression = columnList.get(1).getValueExpression();
					StringLiteral clsStringLiteral = new StringLiteral(cls);
					try {
						newSelection.addCondition(new ComparisonPredicate(columnRefExpression, clsStringLiteral, ComparisonPredicate.Operator.EQ));
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					ra = sourceQueryTree.value().clone();
					ra.setSelection(newSelection);
					
					Projection valueProject = new Projection();
					
					/**
					 * 'X' is at position 0 of 'q(X, Class)'.
					 */
					valueProject.add(columnList.get(0));
					
					ra = sourceQueryTree.value().clone();
					ra.setProjection(valueProject);
					ra.setSelection(newSelection);
					
					QueryTree newTargetQueryTree = new QueryTree(ra, sourceQueryTree.left(), sourceQueryTree.right());
					
					// TODO we may want a proper id
					OBDARDBMappingAxiom newMapping = dfac.getRDBMSMappingAxiom(newTargetQueryTree.toString(), newTargetQuery);
					
					System.err.println(newMapping);
					
					expandedMappings.add(newMapping);
					
				}
				
			}

		}
		
	
		return expandedMappings;
	}

}
