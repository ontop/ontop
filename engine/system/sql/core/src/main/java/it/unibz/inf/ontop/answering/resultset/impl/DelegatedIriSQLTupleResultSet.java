package it.unibz.inf.ontop.answering.resultset.impl;

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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of TupleResultSet for SQL queries, in the case where the IRI construction is delegated to the source
 * engine rather than post-processed.
 */
public class DelegatedIriSQLTupleResultSet extends AbstractSQLTupleResultSet implements TupleResultSet {

    private final ImmutableMap<String, Integer> columnMap;
    protected final JDBC2ConstantConverter ontopConstantRetriever;

    public DelegatedIriSQLTupleResultSet(ResultSet rs, ImmutableList<String> signature, DBMetadata dbMetadata,
                                         Optional<IRIDictionary> iriDictionary, TermFactory termFactory,
                                         TypeFactory typeFactory, RDF rdfFactory) {
        super(rs, signature);
        this.columnMap = buildColumnMap();
        this.ontopConstantRetriever = new JDBC2ConstantConverter(dbMetadata, iriDictionary, termFactory, typeFactory,
                rdfFactory);
    }

    @Override
    protected DelegatedIriSQLBindingSet readCurrentRow() throws OntopConnectionException {
        SQLRowReader rowReader = new SQLRowReader();
        try {
            final List<MainTypeLangValues> cells = rowReader.read(rs, getColumnCount());
            return new DelegatedIriSQLBindingSet(cells, signature, columnMap, ontopConstantRetriever);
        } catch (SQLException e) {
            throw new OntopConnectionException(e);
        }
    }

    private ImmutableMap<String, Integer> buildColumnMap() {
        AtomicInteger i = new AtomicInteger(0);
        return signature.stream().sequential()
                .collect(ImmutableCollectors.toMap(
                        v -> v,
                        v -> i.incrementAndGet()
                ));
    }
}
