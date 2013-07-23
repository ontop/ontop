/**
 * by Yuanbo Guo
 * Semantic Web and Agent Technology Lab, CSE Department, Lehigh University, USA
 * Copyright (C) 2004
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
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