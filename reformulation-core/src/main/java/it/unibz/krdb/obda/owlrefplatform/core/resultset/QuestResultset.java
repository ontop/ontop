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
import it.unibz.krdb.obda.model.impl.QuestTypeMapper;
import it.unibz.krdb.obda.owlrefplatform.core.QuestStatement;
import it.unibz.krdb.obda.owlrefplatform.core.abox.SemanticIndexURIMap;

import java.net.URISyntaxException;
import java.sql.*;
import java.sql.ResultSet;
import java.text.*;
import java.util.HashMap;
import java.util.List;

public class QuestResultset implements TupleResultSet {

	private boolean isSemIndex = false;
	private ResultSet set = null;
	QuestStatement st;
	private List<String> signature;
	private DecimalFormat formatter = new DecimalFormat("0.0###E0");

	private HashMap<String, Integer> columnMap;

	private HashMap<String, String> bnodeMap;

	// private LinkedHashSet<String> uriRef = new LinkedHashSet<String>();
	private int bnodeCounter = 0;

	private OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
	private SemanticIndexURIMap uriMap;
	
	private String vendor;
	private boolean isOracle;
    private boolean isMsSQL;

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
	public QuestResultset(ResultSet set, List<String> signature, QuestStatement st) throws OBDAException {
		this.set = set;
		this.st = st;
		this.isSemIndex = st.questInstance.isSemIdx();
		if (isSemIndex) 
			uriMap = st.questInstance.getSemanticIndexRepository().getUriMap();
		else
			uriMap = null;
		this.signature = signature;
		
		columnMap = new HashMap<String, Integer>(signature.size() * 2);
		bnodeMap = new HashMap<String, String>(1000);

		for (int j = 1; j <= signature.size(); j++) {
			columnMap.put(signature.get(j - 1), j);
		}

		DecimalFormatSymbols symbol = DecimalFormatSymbols.getInstance();
		symbol.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbol);

			 vendor =  st.questInstance.getMetaData().getDriverName();
			 isOracle = vendor.contains("Oracle");
             isMsSQL = vendor.contains("SQL Server");
						


	}

	public int getColumnCount() throws OBDAException {
		return signature.size();
	}

	public int getFetchSize() throws OBDAException {
		try {
			return set.getFetchSize();
		} catch (SQLException e) {
			throw new OBDAException(e.getMessage());
		}
	}

	public List<String> getSignature() throws OBDAException {
		return signature;
	}

	public boolean nextRow() throws OBDAException {
		try {
			return set.next();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	public void close() throws OBDAException {
		try {
			set.close();
		} catch (SQLException e) {
			throw new OBDAException(e);
		}
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
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
		String realValue = "";

		try {
			realValue = set.getString(column);
			COL_TYPE type = getQuestType( set.getInt(column - 2));

			if (type == COL_TYPE.NULL || realValue == null) {
				return null;
			} else {
				if (type == COL_TYPE.OBJECT) {
					if (isSemIndex) {
						try {
							Integer id = Integer.parseInt(realValue);
							realValue = uriMap.getURI(id);
						} catch (NumberFormatException e) {
							/*
							 * If its not a number, then it has to be a URI, so
							 * we leave realValue as is.
							 */
						}
					}

					result = fac.getConstantURI(realValue.trim());

				} else if (type == COL_TYPE.BNODE) {
					String rawLabel = set.getString(column);
					String scopedLabel = this.bnodeMap.get(rawLabel);
					if (scopedLabel == null) {
						scopedLabel = "b" + bnodeCounter;
						bnodeCounter += 1;
						bnodeMap.put(rawLabel, scopedLabel);
					}
					result = fac.getConstantBNode(scopedLabel);
				} else {
					/*
					 * The constant is a literal, we need to find if its
					 * rdfs:Literal or a normal literal and construct it
					 * properly.
					 */
					if (type == COL_TYPE.LITERAL) {
						String value = set.getString(column);
						String language = set.getString(column - 1);
						if (language == null || language.trim().equals("")) {
							result = fac.getConstantLiteral(value);
						} else {
							result = fac.getConstantLiteral(value, language);
						}
					} else if (type == COL_TYPE.BOOLEAN) {
						boolean value = set.getBoolean(column);
						if (value) {
							result = fac.getConstantLiteral("true", type);
						} else {
							result = fac.getConstantLiteral("false", type);
						}
					} else if (type == COL_TYPE.DOUBLE) {
						double d = set.getDouble(column);
						// format name into correct double representation

						String s = formatter.format(d);
						result = fac.getConstantLiteral(s, type);

					} else if (type == COL_TYPE.DATETIME) {

                        /** set.getTimestamp() gives problem with MySQL and Oracle drivers we need to specify the dateformat
                         MySQL DateFormat ("MMM DD YYYY HH:mmaa");
                         Oracle DateFormat "dd-MMM-yy HH.mm.ss.SSSSSS aa" For oracle driver v.11 and less
                         Oracle "dd-MMM-yy HH:mm:ss,SSSSSS" FOR ORACLE DRIVER 12.1.0.2
                         To overcome the problem we create a new Timestamp */

                    try {


                        Timestamp value = set.getTimestamp(column);
                        result = fac.getConstantLiteral(value.toString().replace(' ', 'T'), type);

                    }
                    catch (Exception e){

                        if (isMsSQL) {
                            String value = set.getString(column);

                            DateFormat df = new SimpleDateFormat("MMM DD YYYY HH:mmaa");
                            java.util.Date date;
                            try {
                                date = df.parse(value);
                                Timestamp ts = new Timestamp(date.getTime());
                                result = fac.getConstantLiteral(ts.toString().replace(' ', 'T'), type);

                            } catch (ParseException pe) {

                                throw new RuntimeException(pe);
                            }
                        } else {
                            if (isOracle) {

                                String value = set.getString(column);
                                //TODO Oracle driver - this date format depends on the version of the driver
                                DateFormat df = new SimpleDateFormat("dd-MMM-yy HH.mm.ss.SSSSSS aa"); // For oracle driver v.11 and less
//							DateFormat df = new SimpleDateFormat("dd-MMM-yy HH:mm:ss,SSSSSS"); // THIS WORKS FOR ORACLE DRIVER 12.1.0.2
                                java.util.Date date;
                                try {
                                    date = df.parse(value);
                                } catch (ParseException pe) {
                                    throw new RuntimeException(pe);
                                }

                                Timestamp ts = new Timestamp(date.getTime());
                                result = fac.getConstantLiteral(ts.toString().replace(' ', 'T'), type);
                            }
                            else{
                                throw new RuntimeException(e);
                            }
                        }

                        }


                    } else if (type == COL_TYPE.DATE) {
						if (!isOracle) {
							Date value = set.getDate(column);
							result = fac.getConstantLiteral(value.toString(), type);
						} else {
							String value = set.getString(column);
							DateFormat df = new SimpleDateFormat("dd-MMM-yy");
							java.util.Date date;
							try {
								date = df.parse(value);
							} catch (ParseException e) {
								throw new RuntimeException(e);
							}
							result = fac.getConstantLiteral(value.toString(), type);
						}
						
						
					} else if (type == COL_TYPE.TIME) {
						Time value = set.getTime(column);						
						result = fac.getConstantLiteral(value.toString().replace(' ', 'T'), type);
					} else {
						result = fac.getConstantLiteral(realValue, type);
					}
				}
			}
		} catch (IllegalArgumentException e) {
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
			} else {
				OBDAException ex = new OBDAException("Quest couldn't parse the data value to Java object: " + realValue + "\n"
						+ "Please review the mapping rules to have the datatype assigned properly.");
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
		} catch (SQLException e) {
			throw new OBDAException(e);
		}

		return result;
	}

	// @Override
	// public ValueConstant getLiteral(int column) throws OBDAException {
	// return getLiteral(signature.get(column - 1));
	// }
	//
	// @Override
	// public BNode getBNode(int column) throws OBDAException {
	// return getBNode(signature.get(column - 1));
	// }

	@Override
	public Constant getConstant(String name) throws OBDAException {
		Integer columnIndex = columnMap.get(name);
		return getConstant(columnIndex);
	}

    /**
     * Numbers codetype are defined see also  #getTypeColumnForSELECT SQLGenerator
     */

	private COL_TYPE getQuestType(int typeCode) {

        COL_TYPE questType = fac.getDatatypeFactory().getQuestTypeMapper().getQuestType(typeCode);

        if (questType == null)
        	throw new RuntimeException("typeCode unknown: " + typeCode);
        
        return questType;
	}

	// @Override
	// public URI getURI(String name) throws OBDAException {
	// String result = "";
	// try {
	// result = set.getString(name);
	//
	// return URI.create(result);// .replace(' ', '_'));
	//
	// } catch (SQLException e) {
	// throw new OBDAException(e);
	// }
	// }
	//
	// @Override
	// public IRI getIRI(String name) throws OBDAException {
	// int id = -1;
	// try {
	// String result = set.getString(name);
	// if (isSemIndex) {
	// try {
	// id = Integer.parseInt(result);
	// } catch (NumberFormatException e) {
	// // its not a number - its a URI
	// IRI iri = irif.create(result);
	// return iri;
	// }
	//
	// result = uriMap.get(id);
	// }
	//
	// IRI iri = irif.create(result);
	// return iri;
	// } catch (Exception e) {
	// throw new OBDAException(e);
	// }
	// }
	//
	// @Override
	// public ValueConstant getLiteral(String name) throws OBDAException {
	// Constant result;
	//
	// result = getConstant(name);
	//
	// return (ValueConstant) result;
	// }
	//
	// @Override
	// public BNode getBNode(String name) throws OBDAException {
	// Constant result;
	// result = getConstant(name);
	// return (BNode) result;
	// }
}
