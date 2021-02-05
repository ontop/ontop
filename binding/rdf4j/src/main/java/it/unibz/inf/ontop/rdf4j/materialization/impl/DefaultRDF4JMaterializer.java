package it.unibz.inf.ontop.rdf4j.materialization.impl;

/*
 * #%L
 * ontop-quest-sesame
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
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.resultset.MaterializedGraphResultSet;
import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopQueryAnsweringException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;
import it.unibz.inf.ontop.materialization.impl.DefaultOntopRDFMaterializer;
import it.unibz.inf.ontop.rdf4j.materialization.RDF4JMaterializer;
import it.unibz.inf.ontop.rdf4j.query.MaterializationGraphQuery;
import it.unibz.inf.ontop.rdf4j.utils.RDF4JHelper;

import org.apache.commons.rdf.api.IRI;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;

import javax.annotation.Nonnull;
import java.security.SecureRandom;

public class DefaultRDF4JMaterializer implements RDF4JMaterializer {

    private final OntopRDFMaterializer materializer;

    public DefaultRDF4JMaterializer(OntopSystemConfiguration configuration, MaterializationParams materializationParams) throws OBDASpecificationException {
        materializer = new DefaultOntopRDFMaterializer(configuration, materializationParams);
    }

    /**
     * Materializes the saturated RDF graph with the default options
     */
    public DefaultRDF4JMaterializer(OntopSystemConfiguration configuration) throws OBDASpecificationException {
        this(configuration, MaterializationParams.defaultBuilder().build());
    }

    @Override
    public MaterializationGraphQuery materialize() throws RepositoryException {
        try {
            MaterializedGraphResultSet graphResultSet = materializer.materialize();

            return new DefaultMaterializedGraphQuery(graphResultSet);

        } catch (OBDASpecificationException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public MaterializationGraphQuery materialize(@Nonnull ImmutableSet<IRI> selectedVocabulary)
            throws RepositoryException {
        try {
            MaterializedGraphResultSet graphResultSet = materializer.materialize(selectedVocabulary);

            return new DefaultMaterializedGraphQuery(graphResultSet);

        } catch (OBDASpecificationException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public ImmutableSet<IRI> getClasses() {
        return materializer.getClasses();
    }

    @Override
    public ImmutableSet<IRI> getProperties() {
        return materializer.getProperties();
    }

    /**
     * Specialized graph query
     */
    private static class DefaultMaterializedGraphQuery implements MaterializationGraphQuery {


        private static final String NOT_AVAILABLE_OPTION = "This option is not available for materialization";
        private static final String NOT_AVAILABLE_INFO = "This information is not available for materialization";
        private final MaterializedGraphResultSet graphResultSet;
        private boolean hasStarted;


        DefaultMaterializedGraphQuery(MaterializedGraphResultSet graphResultSet) {
            this.graphResultSet = graphResultSet;
            this.hasStarted = false;
        }

        @Override
        public GraphQueryResult evaluate() throws QueryEvaluationException {
            if (hasStarted)
                throw new QueryEvaluationException("A materialization GraphQuery can only be evaluated once");
            hasStarted = true;

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[20];
            random.nextBytes(salt);

            return new IteratingGraphQueryResult(ImmutableMap.of(), new GraphMaterializationIteration(graphResultSet, salt));
        }

        @Override
        public void evaluate(RDFHandler handler) throws QueryEvaluationException, RDFHandlerException {
            try (GraphQueryResult result = evaluate()) {
                handler.startRDF();
                while (result.hasNext())
                    handler.handleStatement(result.next());
                handler.endRDF();
            }
        }

        @Override
        public void setMaxQueryTime(int maxQueryTime) {
            throw new UnsupportedOperationException(NOT_AVAILABLE_OPTION);
        }

        @Override
        public int getMaxQueryTime() {
            throw new UnsupportedOperationException(NOT_AVAILABLE_INFO);
        }

        @Override
        public void setBinding(String name, Value value) {
            throw new UnsupportedOperationException(NOT_AVAILABLE_OPTION);
        }

        @Override
        public void removeBinding(String name) {
            throw new UnsupportedOperationException(NOT_AVAILABLE_OPTION);
        }

        @Override
        public void clearBindings() {
            throw new UnsupportedOperationException(NOT_AVAILABLE_OPTION);
        }

        @Override
        public BindingSet getBindings() {
            throw new UnsupportedOperationException(NOT_AVAILABLE_INFO);
        }

        @Override
        public void setDataset(Dataset dataset) {
            throw new UnsupportedOperationException(NOT_AVAILABLE_OPTION);
        }

        @Override
        public Dataset getDataset() {
            throw new UnsupportedOperationException(NOT_AVAILABLE_INFO);
        }

        @Override
        public void setIncludeInferred(boolean includeInferred) {
            throw new UnsupportedOperationException(NOT_AVAILABLE_OPTION);
        }

        @Override
        public boolean getIncludeInferred() {
            throw new UnsupportedOperationException(NOT_AVAILABLE_INFO);
        }

        @Override
        public void setMaxExecutionTime(int maxExecTime) {
            throw new UnsupportedOperationException(NOT_AVAILABLE_OPTION);
        }

        @Override
        public int getMaxExecutionTime() {
            throw new UnsupportedOperationException(NOT_AVAILABLE_INFO);
        }

        @Override
        public long getTripleCountSoFar() {
            return graphResultSet.getTripleCountSoFar();
        }

        @Override
        public boolean hasEncounteredProblemsSoFar() {
            return graphResultSet.hasEncounteredProblemsSoFar();
        }

        @Override
        public ImmutableList<IRI> getPossiblyIncompleteRDFPropertiesAndClassesSoFar() {
            return graphResultSet.getPossiblyIncompleteRDFPropertiesAndClassesSoFar();
        }

        @Override
        public ImmutableSet<IRI> getSelectedVocabulary() {
            return graphResultSet.getSelectedVocabulary();
        }
    }

    private static class GraphMaterializationIteration implements CloseableIteration<Statement, QueryEvaluationException> {

        private final MaterializedGraphResultSet graphResultSet;
        private final byte[] salt;

        GraphMaterializationIteration(MaterializedGraphResultSet graphResultSet, byte[] salt) {
            this.graphResultSet = graphResultSet;
            this.salt = salt;
        }

        @Override
        public void close() throws QueryEvaluationException {
            try {
                this.graphResultSet.close();
            } catch (OntopConnectionException e) {
                throw new QueryEvaluationException(e);
            }
        }

        @Override
        public boolean hasNext() throws QueryEvaluationException {
            try {
                return graphResultSet.hasNext();
            } catch (OntopConnectionException | OntopQueryAnsweringException e) {
                throw new QueryEvaluationException(e);
            }
        }

        @Override
        public Statement next() throws QueryEvaluationException {
            try {
                return RDF4JHelper.createStatement(graphResultSet.next(), salt);
            } catch (OntopQueryAnsweringException | OntopConnectionException e) {
                throw new QueryEvaluationException(e);
            }
        }

        @Override
        public void remove() throws QueryEvaluationException {
            throw new UnsupportedOperationException("TODO: support remove()");
        }
    }


}
