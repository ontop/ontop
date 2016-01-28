package it.unibz.krdb.obda.owlrefplatform.core.resultset;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;
import it.unibz.krdb.sql.DBMetadata;

import java.net.URISyntaxException;
import java.sql.*;
import java.sql.ResultSet;
import java.text.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QuestTupleResultSet implements TupleResultSet {

	private final ResultSet rs;
	private final QuestStatement st;
	private final List<String> signature;
	
	private static final DecimalFormat formatter = new DecimalFormat("0.0###E0");

	static {
		DecimalFormatSymbols symbol = DecimalFormatSymbols.getInstance();
		symbol.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbol);
	}
	
	private final Map<String, Integer> columnMap;
	private final Map<String, String> bnodeMap;

	private int bnodeCounter = 0;

	private final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private final SemanticIndexURIMap uriMap;
	
	private final boolean isOracle;
    private final boolean isMsSQL;
	
	private final DateFormat dateFormat;



	/***
	 * Constructs an OBDA statement from an SQL statement, a signature described
	 * by terms and a statement. The statement is maintained only as a reference
	 * for closing operations.
	 * 
	 * @param set
	 * @param signature
	 *            A list of terms that determines the type of the columns of
	 *            this results set.
	 * @param st
	 * @throws OBDAException
	 */
	public QuestTupleResultSet(ResultSet set, List<String> signature, QuestStatement st) throws OBDAException {
		this.rs = set;
		this.st = st;
		this.uriMap = st.questInstance.getUriMap();
		this.signature = signature;
		
		columnMap = new HashMap<>(signature.size() * 2);
		bnodeMap = new HashMap<>(1000);

		for (int j = 1; j <= signature.size(); j++) {
			columnMap.put(signature.get(j - 1), j);
		}

		DBMetadata metadata = st.questInstance.getMetaData();
		String vendor =  metadata.getDriverName();
		isOracle = vendor.contains("Oracle");
		isMsSQL = vendor.contains("SQL Server");

		if (isOracle) {
			String version = metadata.getDriverVersion();
			int versionInt = Integer.parseInt(version.substring(0, version.indexOf(".")));

			if (versionInt >= 12) 
				dateFormat = new SimpleDateFormat("dd-MMM-yy HH:mm:ss,SSSSSS" , Locale.ENGLISH); // THIS WORKS FOR ORACLE DRIVER 12.1.0.2
			else 
				dateFormat = new SimpleDateFormat("dd-MMM-yy HH.mm.ss.SSSSSS aa" , Locale.ENGLISH); // For oracle driver v.11 and less
		}
		else if (isMsSQL) {
			dateFormat = new SimpleDateFormat("MMM dd yyyy hh:mmaa", Locale.ENGLISH );
		}
		else
			dateFormat = null;
	}

	@Override
    public int getColumnCount() throws OBDAException {
		return signature.size();
	}

	@Override
    public int getFetchSize() throws OBDAException {
		try {
			return rs.getFetchSize();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	@Override
    public List<String> getSignature() throws OBDAException {
		return signature;
	}

	@Override
    public boolean nextRow() throws OBDAException {
		try {
			return rs.next();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	@Override
    public void close() throws OBDAException {
		try {
			rs.close();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

	public Object getRawObject(int column) throws OBDAException {
		try {
			Object realValue = rs.getObject(column);
			return realValue;
		} 
		catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	/***
	 * Returns the constant at column "column" recall that columns start at index 1.
	 */
	@Override
	public Constant getConstant(int column) throws OBDAException {
		column = column * 3; // recall that the real SQL result set has 3
								// columns per value. From each group of 3 the actual value is the
								// 3rd column, the 2nd is the language, the 1st is the type code (an integer)

		Constant result = null;
		String value = "";

		try {
			value = rs.getString(column);
		    
			if (value == null) {
				return null;
			} 
			else {
				int t = rs.getInt(column - 2);
			    COL_TYPE type = COL_TYPE.getQuestType(t);
			    if (type == null)
			    	throw new RuntimeException("typeCode unknown: " + t);
			    
				switch (type) {
				case NULL:
					result = null;
					break;
					
				case OBJECT:
					if (uriMap != null) {
						try {
							Integer id = Integer.parseInt(value);
							value = uriMap.getURI(id);
						} 
						catch (NumberFormatException e) {
							 // If its not a number, then it has to be a URI, so
							 // we leave realValue as it is.
						}
					}
					result = fac.getConstantURI(value.trim());
					break;
					
				case BNODE:
					String scopedLabel = this.bnodeMap.get(value);
					if (scopedLabel == null) {
						scopedLabel = "b" + bnodeCounter;
						bnodeCounter += 1;
						bnodeMap.put(value, scopedLabel);
					}
					result = fac.getConstantBNode(scopedLabel);
					break;
					
				case LITERAL:
					// The constant is a literal, we need to find if its
					// rdfs:Literal or a normal literal and construct it
					// properly.
					String language = rs.getString(column - 1);
					if (language == null || language.trim().equals("")) 
						result = fac.getConstantLiteral(value);
					else 
						result = fac.getConstantLiteral(value, language);
					break;
					
				case BOOLEAN:
					boolean bvalue = rs.getBoolean(column);
					result = fac.getBooleanConstant(bvalue);
					break;
				
				case DOUBLE:
					double d = rs.getDouble(column);
					String s = formatter.format(d); // format name into correct double representation
					result = fac.getConstantLiteral(s, COL_TYPE.DOUBLE);
					break;
					
				case DATETIME:
                    /** set.getTimestamp() gives problem with MySQL and Oracle drivers we need to specify the dateformat
                    MySQL DateFormat ("MMM DD YYYY HH:mmaa");
                    Oracle DateFormat "dd-MMM-yy HH.mm.ss.SSSSSS aa" For oracle driver v.11 and less
                    Oracle "dd-MMM-yy HH:mm:ss,SSSSSS" FOR ORACLE DRIVER 12.1.0.2
                    To overcome the problem we create a new Timestamp */
                    try {
                        Timestamp tsvalue = rs.getTimestamp(column);
                        result = fac.getConstantLiteral(tsvalue.toString().replace(' ', 'T'), COL_TYPE.DATETIME);
                    }
                    catch (Exception e) {
                        if (isMsSQL || isOracle) {
                            try {
                            	java.util.Date date = dateFormat.parse(value);
                                Timestamp ts = new Timestamp(date.getTime());
                                result = fac.getConstantLiteral(ts.toString().replace(' ', 'T'), COL_TYPE.DATETIME);
                            } 
                            catch (ParseException pe) {
                                throw new RuntimeException(pe);
                            }
                        } 
                        else
                            throw new RuntimeException(e);
                    }
                    break;
               
				case DATETIME_STAMP:    
					if (!isOracle) {
						result = fac.getConstantLiteral(value.replaceFirst(" ", "T").replaceAll(" ", ""), COL_TYPE.DATETIME_STAMP);					
					}
					else {
						/* oracle has the type timestamptz. The format returned by getString is not a valid xml format
						we need to transform it. We first take the information about the timezone value, that is lost
						during the conversion in java.util.Date and then we proceed with the conversion. */
						try {
							int indexTimezone = value.lastIndexOf(" ");
							String timezone = value.substring(indexTimezone+1);
							String datetime = value.substring(0, indexTimezone);
							
							java.util.Date date = dateFormat.parse(datetime);
							Timestamp ts = new Timestamp(date.getTime());
							result = fac.getConstantLiteral(ts.toString().replaceFirst(" ", "T").replaceAll(" ", "")+timezone, COL_TYPE.DATETIME_STAMP);
						} 
						catch (ParseException pe) {
							throw new RuntimeException(pe);
						}
					}
					break;
					
				case DATE:
					if (!isOracle) {
						Date dvalue = rs.getDate(column);
						result = fac.getConstantLiteral(dvalue.toString(), COL_TYPE.DATE);
					} 
					else {
						try {
							DateFormat df = new SimpleDateFormat("dd-MMM-yy" ,  Locale.ENGLISH);
							java.util.Date date = df.parse(value);
						} 
						catch (ParseException e) {
							throw new RuntimeException(e);
						}
						result = fac.getConstantLiteral(value.toString(), COL_TYPE.DATE);
					}
					break;
					
				case TIME:
					Time tvalue = rs.getTime(column);						
					result = fac.getConstantLiteral(tvalue.toString().replace(' ', 'T'), COL_TYPE.TIME);
					break;
				
				default:
					result = fac.getConstantLiteral(value, type);
				}
			}
		} 
		catch (IllegalArgumentException e) {
			Throwable cause = e.getCause();
			if (cause instanceof URISyntaxException) {
				OBDAException ex = new OBDAException(
						"Error creating an object's URI. This is often due to mapping with URI templates that refer to "
								+ "columns in which illegal values may appear, e.g., white spaces and special characters.\n"
								+ "To avoid this error do not use these columns for URI templates in your mappings, or process "
								+ "them using SQL functions (e.g., string replacement) in the SQL queries of your mappings.\n\n"
								+ "Note that this last option can be bad for performance, future versions of Quest will allow to "
								+ "string manipulation functions in URI templates to avoid these performance problems.\n\n"
								+ "Detailed message: " + cause.getMessage());
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			} 
			else {
				OBDAException ex = new OBDAException("Quest couldn't parse the data value to Java object: " + value + "\n"
						+ "Please review the mapping rules to have the datatype assigned properly.");
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
		} 
		catch (SQLException e) {
			throw new OBDAException(e);
		}

		return result;
	}


	@Override
	public Constant getConstant(String name) throws OBDAException {
		Integer columnIndex = columnMap.get(name);
		return getConstant(columnIndex);
	}
}
