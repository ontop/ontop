package it.unibz.inf.ontop.owlrefplatform.core.resultset;

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


import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.LazyOntopBindingSet;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.text.*;
import java.util.*;

public class QuestTupleResultSet implements TupleResultSet {

	private final ResultSet rs;
	private final List<String> signature;

	private final Map<String, Integer> columnMap;
	private final Map<String, String> bnodeMap;

	private int bnodeCounter = 0;

	@Nullable
	private final IRIDictionary iriDictionary;
	
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
	 */
	public QuestTupleResultSet(ResultSet set, List<String> signature,
                               DBMetadata dbMetadata, Optional<IRIDictionary> iriDictionary) {
		this.rs = set;
		this.iriDictionary = iriDictionary.orElse(null);
		this.signature = signature;
		
		columnMap = new HashMap<>(signature.size() * 2);
		bnodeMap = new HashMap<>(1000);

		for (int j = 1; j <= signature.size(); j++) {
			columnMap.put(signature.get(j - 1), j);
		}

		String vendor =  dbMetadata.getDriverName();
		isOracle = vendor.contains("Oracle");
		isMsSQL = vendor.contains("SQL Server");

		if (isOracle) {
			String version = dbMetadata.getDriverVersion();
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
    public int getColumnCount() {
		return signature.size();
	}

	@Override
    public int getFetchSize() throws OntopConnectionException {
		try {
			return rs.getFetchSize();
		} catch (Exception e) {
			throw new OntopConnectionException(e.getMessage());
		}
	}

    @Override
    public OntopBindingSet next() {
        return new LazyOntopBindingSet(rs, signature, isMsSQL, isOracle, dateFormat, iriDictionary, bnodeMap, columnMap);
    }

    @Override
    public List<String> getSignature() {
		return signature;
	}

	@Override
    public boolean hasNext() throws OntopConnectionException {
		try {
		    // FIXME(xiao): don't call rs.next() twice when calling this.hasNext() twice
			return rs.next();
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

	@Override
    public void close() throws OntopConnectionException {
		try {
			rs.close();
		} catch (Exception e) {
			throw new OntopConnectionException(e);
		}
	}

    public Object getRawObject(int column) throws OntopConnectionException {
        try {
            Object realValue = rs.getObject(column);
            return realValue;
        }
        catch (Exception e) {
            throw new OntopConnectionException(e);
        }
    }


}
