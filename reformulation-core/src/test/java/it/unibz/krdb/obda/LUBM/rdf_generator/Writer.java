package it.unibz.krdb.obda.LUBM.rdf_generator;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

public interface Writer {
    /**
     * Called when starting data generation.
     */
    public void start();

    /**
     * Called when finish data generation.
     */
    public void end();

    /**
     * Starts file writing.
     *
     * @param fileName File name.
     */
    public void startFile(String fileName);

    /**
     * Finishes the current file.
     */
    public void endFile();

    /**
     * Starts a section for the specified instance.
     *
     * @param classType Type of the instance.
     * @param id        Id of the instance.
     */
    public void startSection(int classType, String id);

    /**
     * Starts a section for the specified instance identified by an rdf:about attribute.
     *
     * @param classType Type of the instance.
     * @param id        Id of the instance.
     */
    public void startAboutSection(int classType, String id);

    /**
     * Finishes the current section.
     *
     * @param classType Type of the current instance.
     */
    public void endSection(int classType);

    /**
     * Adds the specified property statement for the current element.
     *
     * @param property   Type of the property.
     * @param value      Property value.
     * @param isResource Indicates if the property value is an rdf resource (True),
     *                   or it is literal (False).
     */
    public void addProperty(int property, String value, boolean isResource);

    /**
     * Adds a property statement for the current element whose value is an individual.
     *
     * @param property   Type of the property.
     * @param valueClass Type of the individual.
     * @param valueId    Id of the individual.
     */
    public void addProperty(int property, int valueClass, String valueId);
}
