package org.jmodelica.ide.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.jastadd.ed.core.ICompiler;
import org.jastadd.ed.core.model.GlobalRootRegistry;
import org.jastadd.ed.core.model.IASTChangeListener;
import org.jastadd.ed.core.model.node.IGlobalRootNode;
import org.jastadd.ed.core.model.node.ILocalRootNode;
import org.jmodelica.modelica.compiler.ASTNode;
import org.jmodelica.modelica.compiler.InstNode;
import org.jmodelica.modelica.compiler.InstProgramRoot;
import org.jmodelica.modelica.compiler.LibNode;
import org.jmodelica.modelica.compiler.Program;
import org.jmodelica.modelica.compiler.SourceRoot;

public class ModelicaASTRegistry extends GlobalRootRegistry {
	private static ModelicaASTRegistry registry;

	private ModelicaASTRegistry() {
	}

	public static synchronized ModelicaASTRegistry getInstance() {
		if (registry == null)
			registry = new ModelicaASTRegistry();
		return registry;
	}

	/**
	 * For textual editor FMU_Compile
	 */
	public IGlobalRootNode lookupFileGlobalRoot(String fileName) {
		for (IGlobalRootNode gn : fProjectASTMap.values()) {
			for (ILocalRootNode ln : gn.lookupAllFileNodes()) {
				if (ln.getFile().getName().equals(fileName)) {
					return gn;
				}
			}
		}
		return null;
	}

	/**
	 * For textual editor FMU_Compile
	 */
	public ILocalRootNode lookupFileLocalRoot(String fileName) {
		for (IGlobalRootNode gn : fProjectASTMap.values()) {
			for (ILocalRootNode ln : gn.lookupAllFileNodes()) {
				if (ln.getFile().getName().equals(fileName)) {
					return ln;
				}
			}
		}
		return null;
	}

	/**
	 * @Override public boolean doUpdate(IFile file, ILocalRootNode newNode) {
	 *           boolean res = super.doUpdate(file, newNode);
	 *           ChangePropagationController.getInstance().handleNotifications(
	 *           ASTChangeEvent.POST_UPDATE, file, new Stack<String>()); return
	 *           res; }
	 */

	public boolean hasProject(IProject project) {
		return fProjectASTMap.containsKey(project);
	}

	public void addListener(IFile file, ASTNode<?> node,
			ListenerObject listObj) {
		Stack<String> nodePath = new Stack<String>();
		if (node != null) {
			synchronized (node.state()) {
				nodePath = createPath(node);
			}
		}
		ChangePropagationController.getInstance().addListener(listObj, file, nodePath);
	}

	public void removeListener(IFile file, ASTNode<?> node,
			IASTChangeListener listener) {
		Stack<String> nodePath = new Stack<String>();
		if (node != null) {
			synchronized (node.state()) {
				nodePath = createPath(node);
			}
		}
		ChangePropagationController.getInstance().removeListener(listener,
				file, nodePath);
	}

	/**
	 * Tries to resolve the node of the given path in Instance AST from the
	 * given InstProgramRoot. Needed if Instance outline should have expandable
	 * nodes
	 * 
	 * @param nodePath
	 * @param root
	 * @return
	 */
	public InstNode resolveInstanceASTPath(Stack<String> nodePath,
			InstProgramRoot root) {
		return resolveInstNodePath(nodePath, root);
	}

	private InstNode resolveInstNodePath(Stack<String> nodePath, InstNode root) {
		if (nodePath.size() == 0)
			return root;
		// printPath(nodePath);
		String sought = nodePath.pop();
		if (sought.split(":")[0].equals("List")) {
			InstNode node = searchClassesAndComponents(nodePath.peek(), root);
			if (node != null) {
				System.out.println("found " + nodePath.pop()
						+ " among classes/components");
			}
			return resolveInstNodePath(nodePath, node);
		}
		return null;
	}

	private InstNode searchClassesAndComponents(String peek, InstNode root) {
		ArrayList<InstNode> found = new ArrayList<InstNode>();
		found.addAll(root.instClassDecls());
		found.addAll(root.instComponentDecls());
		for (InstNode n : found)
			if (createIdentifier(n).equals(peek))
				return n;
		return null;
	}

	/**
	 * Tries to resolve the node of the given path in Source AST from the given
	 * SourceRoot.
	 * 
	 * @param nodePath
	 * @param root
	 * @return Found node, or SourceRoot if path was empty.
	 */
	public ASTNode<?> resolveSourceASTPath(Stack<String> nodePath,
			SourceRoot root) {
		// System.out.println("ModelicaASTReg: resolve src nodepath, nodepathsize:"+nodePath.size());
		Stack<String> copy = new Stack<String>();
		copy.setSize(nodePath.size());
		Collections.copy(copy, nodePath);
		return resolveSrcNodePath(copy, root);
	}

	private ASTNode<?> resolveSrcNodePath(Stack<String> nodePath,
			ASTNode<?> root) {
		long time = System.currentTimeMillis();
		// printPath(nodePath);
		String sought = "";
		String next = "";
		ASTNode<?> current = root;
		ASTNode<?> previous = null;
		while (!nodePath.isEmpty() && current != null) {
			next = "";
			previous = current;
			sought = nodePath.pop();
			if (!nodePath.isEmpty())
				next = nodePath.peek();
			current = findSrcChild(sought, next, current, previous);
		}
		if (current == null) {
			System.err
					.println("ModelicaASTRegistry failed to resolve src nodepath...");
		}
		System.out.println("ModelicaASTReg: Successful ResolveSrcPath() took: "
				+ (System.currentTimeMillis() - time) + "ms");
		return current;
	}

	/**private void printPath(Stack<String> nodePath) {
		String priint = "";
		for (int i = 0; i < nodePath.size(); i++)
			priint = nodePath.get(i) + " " + priint;
		System.out.println("Trying to resolve path: " + priint);
	}*/

	/**
	 * Bit messy, due to structure of MSL
	 * 
	 * @param sought
	 * @param next
	 * @param current
	 * @param previous
	 * @return
	 */
	private ASTNode<?> findSrcChild(String sought, String next,
			ASTNode<?> current, ASTNode<?> previous) {
		for (int i = 0; i < current.getNumChild(); i++) {
			ASTNode<?> currChild = current.getChild(i);
			// System.out.println("Sought: " + sought + " Current: "
			// + createIdentifier(currChild));
			if (sought.equals(createIdentifier(currChild)))
				return currChild;
		}
		if (!next.equals("") && next.split(":")[0].equals("LibNode")) {
			// System.out.println("Found libnode, trying Program liblist...");
			if (previous instanceof Program) {
				Program p = (Program) previous;
				if (createIdentifier(p.getLibNodeList()).equals(sought)) {
					return p.getLibNodeList();
				}
			}

		}
		if (sought.split(":")[0].equals("StoredDefinition")
				&& current instanceof LibNode) {
			return ((LibNode) current).getStoredDefinition();
		}
		return null;
	}

	public Stack<String> createPath(ASTNode<?> node) {
		Stack<String> nodePath = new Stack<String>();
		ASTNode<?> tmp = node;
		if (node instanceof InstNode) {
			while (tmp != null && !(tmp instanceof InstProgramRoot)) {
				nodePath.add(createIdentifier(tmp));
				tmp = tmp.getParent();
			}
		} else {
			while (tmp != null && !(tmp instanceof SourceRoot)) {
				nodePath.add(createIdentifier(tmp));
				tmp = tmp.getParent();
			}
		}
		return nodePath;
	}

	/**
	 * Currently creates identifier by concatenating
	 * "ASTNode.getName():ASTNode.outlineId()"
	 * 
	 * @param node
	 * @return
	 */
	private String createIdentifier(ASTNode<?> node) {
		StringBuilder sb = new StringBuilder();
		sb.append(node.getNodeName());
		sb.append(":");
		sb.append(node.outlineId());
		return sb.toString();
	}

	@Override
	protected ICompiler createCompiler() {
		return new ModelicaEclipseCompiler();
	}
}