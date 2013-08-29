/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.LUBM.rdf_generator;

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
