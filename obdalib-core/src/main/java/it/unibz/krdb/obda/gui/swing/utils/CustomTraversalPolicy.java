/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.gui.swing.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Vector;

public class CustomTraversalPolicy extends FocusTraversalPolicy {
	Vector<Component> order;

	public CustomTraversalPolicy(Vector<Component> order) {
		this.order = new Vector<Component>(order.size());
		this.order.addAll(order);
	}
	
	public void setComponent(Component element, int index) {
		order.set(index, element);
	}

	public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
		int idx = (order.indexOf(aComponent) + 1) % order.size();
		return order.get(idx);
	}

	public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
		int idx = order.indexOf(aComponent) - 1;
		if (idx < 0) {
			idx = order.size() - 1;
		}
		return order.get(idx);
	}

	public Component getDefaultComponent(Container focusCycleRoot) {
		return order.get(0);
	}

	public Component getLastComponent(Container focusCycleRoot) {
		return order.lastElement();
	}

	public Component getFirstComponent(Container focusCycleRoot) {
		return order.get(0);
	}
}
