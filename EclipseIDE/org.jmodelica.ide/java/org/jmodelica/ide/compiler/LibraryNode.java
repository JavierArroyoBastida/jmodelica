package org.jmodelica.ide.compiler;

import java.util.ArrayList;

import org.jastadd.ed.core.model.IASTChangeListener;

public class LibraryNode {
	private ArrayList<LibraryNode> children = new ArrayList<LibraryNode>();
	private String id;
	private ArrayList<ListenerObject> listeners = new ArrayList<ListenerObject>();

	public LibraryNode(String id) {
		this.id = id;
	}

	public void addChild(LibraryNode node) {
		this.children.add(node);
	}

	public String getId() {
		return id;
	}

	public ArrayList<LibraryNode> getChildren() {
		return children;
	}

	public ArrayList<ListenerObject> getListeners() {
		return listeners;
	}

	public void addListener(ListenerObject list) {
		listeners.add(list);
	}

	public void removeListener(IASTChangeListener listener) {
		for (ListenerObject obj : listeners) {
			if (obj.equals(listener))
				listeners.remove(listener);
		}
	}
}