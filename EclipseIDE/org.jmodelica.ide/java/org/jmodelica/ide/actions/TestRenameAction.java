package org.jmodelica.ide.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.jastadd.ed.core.model.node.IASTNode;
import org.jmodelica.ide.compiler.JobObject;
import org.jmodelica.ide.compiler.ModelicaASTRegistryJobBucket;
import org.jmodelica.modelica.compiler.ASTNode;

public class TestRenameAction extends Action {

	protected TreeViewer viewer;
	protected IFile file;

	public TestRenameAction(TreeViewer viewer, IFile file) {
		super("TestRenameAction");
		this.viewer = viewer;
		this.file = file;
	}

	@Override
	public void run() {
		TreeItem[] selection = viewer.getTree().getSelection();
		System.out.println("TestAction: A test RENAME action was run!");
		for (int i = 0; i < selection.length; i++) {
			String s = ((IASTNode) selection[i].getData()).toString();
			System.out.println("TestRenameAction: Selection contains node: " + s);
			System.out.println("Printing node path to root:");
			printNodePathToRoot((ASTNode<?>) selection[i].getData());
		}
		ASTNode<?> selectedInstNode = (ASTNode<?>) selection[0].getData();
		JobObject job = new JobObject(JobObject.RENAME_NODE, file,
				selectedInstNode);
		ModelicaASTRegistryJobBucket.getInstance().addJob(job);
	}

	public void printNodePathToRoot(ASTNode<?> node) {
		String path = "";
		ArrayList<ASTNode<?>> visited = new ArrayList<ASTNode<?>>();
		while (node != null && !visited.contains(node)) {
			visited.add(node);
			String newPath = node.getNodeName() + " " + node.outlineId();
			if (path.equals("")) {
				path = newPath;
			} else {
				path = newPath + " /// " + path;
			}
			node = node.getParent();
		}
		System.out.println("PATH OF NODE WAS: " + path);
	}
}