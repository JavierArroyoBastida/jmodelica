/*
    Copyright (C) 2009 Modelon AB

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, version 3 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.jmodelica.ide.editor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jastadd.plugin.ReconcilingStrategy;
import org.jastadd.plugin.compiler.ast.IFoldingNode;
import org.jastadd.plugin.registry.ASTRegistry;
import org.jastadd.plugin.registry.IASTRegistryListener;
import org.jmodelica.folding.CharacterPosition;
import org.jmodelica.folding.CharacterProjectionAnnotation;
import org.jmodelica.folding.CharacterProjectionSupport;
import org.jmodelica.folding.CharacterProjectionViewer;
import org.jmodelica.ide.IDEConstants;
import org.jmodelica.ide.ModelicaCompiler;
import org.jmodelica.ide.editor.actions.ErrorCheckAction;
import org.jmodelica.ide.editor.actions.FollowReference;
import org.jmodelica.ide.editor.actions.FormatRegionAction;
import org.jmodelica.ide.editor.actions.ToggleAnnotationsAction;
import org.jmodelica.ide.editor.actions.ToggleComment;
import org.jmodelica.ide.folding.AnnotationDrawer;
import org.jmodelica.ide.folding.IFilePosition;
import org.jmodelica.ide.helpers.EclipseCruftinessWorkaroundClass;
import org.jmodelica.ide.helpers.Util;
import org.jmodelica.ide.outline.InstanceOutlinePage;
import org.jmodelica.ide.outline.SourceOutlinePage;
import org.jmodelica.ide.scanners.generated.Modelica22PartitionScanner;
import org.jmodelica.modelica.compiler.ASTNode;

/**
 * Modelica source editor.
 */
public class Editor extends AbstractDecoratedTextEditor 
    implements IASTRegistryListener {


    /* OK, these constant re-declarations are really redundant, but damn
     * AbstractDecoratedTextEditorPreferenceConstants is a long class name o.O
     */
    private static final String CURRENT_LINE_COLOR_KEY = 
        AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
    private static final String CURRENT_LINE_KEY = 
        AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;
    
    // Content outlines
    private SourceOutlinePage fSourceOutlinePage;
    private InstanceOutlinePage fInstanceOutlinePage;

    // Reconciling strategies
    private ReconcilingStrategy fStrategy;
    private LocalReconcilingStrategy fLocalStrategy;

    // ASTRegistry listener fields
    private ASTRegistry fRegistry; 
    private ASTNode fRoot;
    private String fKey;
    private IProject fProject;
    
    private IDocumentPartitioner fPartitioner;
    
    // For folding
    private CharacterProjectionSupport projectionSupport;
    private AnnotationDrawer annotationDrawingStrategy;
    
    // Actions that needs to be altered
    private ErrorCheckAction errorCheckAction;
    private ToggleAnnotationsAction toggleAnnotationsAction;
    
    private ModelicaCompiler compiler;
    
    private boolean fCompiledLocal;
    private IFile fLocalFile;
    
    private boolean fIsLibrary;
    private String fPath;
    private FollowReference followReference;

    /**
     * Standard constructor.
     */
    public Editor() {
        fRegistry            = org.jastadd.plugin.Activator.getASTRegistry();
        fSourceOutlinePage   = new SourceOutlinePage(this); 
        fInstanceOutlinePage = new InstanceOutlinePage(this);
        followReference         = new FollowReference(this);
        fStrategy            = new ReconcilingStrategy();
        fLocalStrategy       = new LocalReconcilingStrategy(this);
        compiler             = new ModelicaCompiler();
    }

    /**
     * Create and configure a source viewer. Creates a 
     * {@link CharacterProjectionViewer} and configures it for folding and
     * brace matching.
     */
    @Override
    protected ISourceViewer createSourceViewer(Composite parent,
            IVerticalRuler ruler, int styles) {
        
        // Code from the super class implementation of this method
        fAnnotationAccess = getAnnotationAccess();
        fOverviewRuler = createOverviewRuler(getSharedColors());
        
        // Projection support
        CharacterProjectionViewer viewer = 
            new CharacterProjectionViewer(
                    parent, 
                    ruler, 
                    getOverviewRuler(), 
                    isOverviewRulerVisible(), 
                    styles);
        
        configureProjectionSupport(viewer);
        configureDecorationSupport(viewer);
        
        return viewer;
    }

    private void configureProjectionSupport(CharacterProjectionViewer viewer) {
        projectionSupport = 
            new CharacterProjectionSupport(
                    viewer, 
                    getAnnotationAccess(), 
                    getSharedColors());
        annotationDrawingStrategy = 
            new AnnotationDrawer(
                    projectionSupport.getAnnotationPainterDrawingStrategy());
        annotationDrawingStrategy.setCursorLineBackground(getCursorLineBackground());
        
        projectionSupport.setAnnotationPainterDrawingStrategy(
                annotationDrawingStrategy);
        projectionSupport.addSummarizableAnnotationType(
                ModelicaCompiler.ERROR_MARKER_ID);
        projectionSupport.install();
    }

    private void configureDecorationSupport(CharacterProjectionViewer viewer) {

        // Set default values for brace matching.
        IPreferenceStore preferenceStore = getPreferenceStore();
        preferenceStore.setDefault(
                IDEConstants.KEY_BRACE_MATCHING, 
                true);
        PreferenceConverter.setDefault(
                preferenceStore, 
                IDEConstants.KEY_BRACE_MATCHING_COLOR, 
                IDEConstants.BRACE_MATCHING_COLOR);

        // Configure brace matching and ensure decoration support has been created and configured.
        SourceViewerDecorationSupport decoration = 
            getSourceViewerDecorationSupport(viewer);
        decoration.setCharacterPairMatcher(
                new ModelicaCharacterPairMatcher(viewer));
        decoration.setMatchingCharacterPainterPreferenceKeys(
                IDEConstants.KEY_BRACE_MATCHING, 
                IDEConstants.KEY_BRACE_MATCHING_COLOR);
    }
    
    @Override
    protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
        if (Util.is(event.getProperty()).among(
                CURRENT_LINE_KEY, 
                CURRENT_LINE_COLOR_KEY)) 
        {
            annotationDrawingStrategy.setCursorLineBackground(
                    getCursorLineBackground());
        }
        super.handlePreferenceStoreChanged(event);
    }

    private Color getCursorLineBackground() {

        if (!getPreferenceStore().getBoolean(CURRENT_LINE_KEY))
            return null;
        
        RGB color = PreferenceConverter.getColor(
                getPreferenceStore(), 
                CURRENT_LINE_COLOR_KEY);
        
        return new Color(Display.getCurrent(), color);
    }

    /**
     * Sets source viewer configuration to a {@link ViewerConfiguration} 
     * and creates control.
     */
    @Override
    public void createPartControl(Composite parent) {
        
        // Set the source viewer configuration before the call to 
        // createPartControl to set viewer configuration.
        super.setSourceViewerConfiguration(new ViewerConfiguration(this));
        super.createPartControl(parent);
        
        update();
    }
    
    /**
     * Can return an {@link IContentOutlinePage}.
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {
        
        if (IContentOutlinePage.class.equals(required)) 
            return fSourceOutlinePage;
        
        return super.getAdapter(required);
    }

    /**
     * Gets the source outline page associated with this editor.
     * @return  the source outline page
     * @see IContentOutlinePage
     */
    public SourceOutlinePage getSourceOutlinePage() {
        return fSourceOutlinePage;
    }

    /**
     * Gets the instance outline page associated with this editor.
     * @return  the instance outline page
     * @see IContentOutlinePage
     */
    public IContentOutlinePage getInstanceOutlinePage() {
        return fInstanceOutlinePage;
    }

    /**
     * Updates editor to match input change.
     * @see AbstractTextEditor#doSetInput(IEditorInput)
     */
    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        
        super.doSetInput(input);

        // resetAST sets fRoot
        resetAST(input instanceof IFileEditorInput  
                 ? (IFileEditorInput)input
                 : null); 
        
        if (fRoot == null) {
            compileLocal(input);
        } else {
            fCompiledLocal = false;
        }
        
    }

    @Override   
    protected void createActions() {
        super.createActions();
        
        setAction(IDEConstants.ACTION_EXPAND_ALL_ID, 
                new ExpandAllAction());
        setAction(IDEConstants.ACTION_COLLAPSE_ALL_ID, 
                new CollapseAllAction());
        setAction(IDEConstants.ACTION_ERROR_CHECK_ID, 
                errorCheckAction = new ErrorCheckAction());
        setAction(IDEConstants.ACTION_TOGGLE_ANNOTATIONS_ID, 
                toggleAnnotationsAction = new ToggleAnnotationsAction(this));
        setAction(IDEConstants.ACTION_FORMAT_REGION_ID, 
                new FormatRegionAction(this));
        setAction(IDEConstants.ACTION_TOGGLE_COMMENT_ID, 
                new ToggleComment(this));
        setAction(IDEConstants.ACTION_COMPLETE_ID, 
                followReference);
        updateErrorCheckAction();
    }
    
    @Override
    protected void rulerContextMenuAboutToShow(IMenuManager menu) {
        super.rulerContextMenuAboutToShow(menu);
        addAction(menu, IDEConstants.ACTION_EXPAND_ALL_ID);
        addAction(menu, IDEConstants.ACTION_COLLAPSE_ALL_ID);
    }

    /**
     * If edited file is compiled locally, this method is called
     * by reconciler to compile AST and update editor.
     * @param document currently edited document
     */
    public void recompileLocal(IDocument document) {
        
        fRoot = (ASTNode)
            compiler.compileToAST(document, 
                                  null,
                                  null, 
                                  fLocalFile);
        
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                update();
            }
        });
    }
    
    /**
     * Compile file locally. Try to get file references from input.
     * @param input input object (typically {@link IFileEditorInput}
     *  or {@link IURIEditorInput})
     */
    private void compileLocal(IEditorInput input) {

        try {
            fLocalFile = input instanceof IFileEditorInput
                    ? ((IFileEditorInput)input).getFile()
                    : null;
            fPath = getPathOfInput(input);
            if (fPath != null) {
                if (fLocalFile == null) 
                    fLocalFile = EclipseCruftinessWorkaroundClass.getFileForPath(fPath);
                fRoot = compiler.compileFile(fLocalFile, fPath);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        fCompiledLocal = true;
        update();
    }
 
    /**
     * Check if file is compiled locally.
     * @return true iff. file is compiled locally
     */
    public boolean isCompiledLocally() {
        return fCompiledLocal;
    }

    private String getPathOfInput(IEditorInput input) {
        String path = null;
            if (input instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput) input).getFile();
                path = file.getRawLocation().toOSString();
            } else if (input instanceof IURIEditorInput) {
                URI uri = ((IURIEditorInput) input).getURI();
                try {
                    path = new File(uri).getCanonicalPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return path;
    }

    // ASTRegistry listener methods
    /**
     * Is called when AST is updated. 
     */
    public void childASTChanged(IProject project, String key) {
        if (project == fProject && key.equals(fKey)) {
            fRoot = (ASTNode) fRegistry.lookupAST(fKey, fProject);
            update();
        }
    }

    public void projectASTChanged(IProject project) {
        if (project == fProject) {
            fRoot = (ASTNode) fRegistry.lookupAST(fKey, fProject);
            update();
        }
    }

    /**
     * Update to the AST corresponding to the file input
     * @param fInput The new file input
     */
    private void resetAST(IFileEditorInput fInput) {
        // Reset 
        fRegistry.removeListener(this);
        fRoot = null;
        fProject = null;
        fKey = null;
        fIsLibrary = false;
        fPath = null;
        IFile file = null;
        
        // Update
        if (fInput != null) {
            file = fInput.getFile();
            fIsLibrary = Util.isInLibrary(file);
            fPath = file.getRawLocation().toOSString();
            if (fIsLibrary)
                fKey = Util.getLibraryPath(file);
            else
                fKey = fPath;
            fProject = file.getProject();
            fRoot = (ASTNode) fRegistry.lookupAST(fKey, fProject);
            fRegistry.addListener(this, fProject, fKey);
            update();
        }
        
        fStrategy.setFile(file);
    }   
    
    /**
     * Updates the outline and the view
     */
    private void update() {
        
        if (getSourceViewer() != null && 
            fRoot != null && 
           !fRoot.isError()) 
        {
            IDocument document = getDocument();
            if (document != null) 
                setupDocumentPartitioner(document);
        
            // Update outline
            fSourceOutlinePage.updateAST(fRoot);
            fInstanceOutlinePage.updateAST(fRoot);
            followReference.updateAST(fRoot);
            
            // Update folding
            updateProjectionAnnotations();
            updateErrorCheckAction();
        }
    }
    
    private void setupDocumentPartitioner(IDocument document) {
        
        IDocumentPartitioner wanted = getDocumentPartitioner();
        IDocumentPartitioner current = document.getDocumentPartitioner();
        
        if (wanted != current) {
            if (current != null)
                current.disconnect();
            wanted.connect(document);
            document.setDocumentPartitioner(wanted);
        }
    }

    private IDocumentPartitioner getDocumentPartitioner() {
        if (fPartitioner == null) {
            Modelica22PartitionScanner scanner = 
                new Modelica22PartitionScanner();
            fPartitioner = new FastPartitioner(scanner, 
                    Modelica22PartitionScanner.LEGAL_PARTITIONS);
        }
        return fPartitioner;
    }

    /**
     * Update projection annotations
     */
    @SuppressWarnings("unchecked")
    private void updateProjectionAnnotations() {
        
        CharacterProjectionViewer viewer = 
            (CharacterProjectionViewer)getSourceViewer();
        
        if (viewer == null) 
            return;
        
        // Enable projection
        viewer.enableProjection();

        ProjectionAnnotationModel model = viewer.getProjectionAnnotationModel(); 
        if (model == null) 
            return;

        // Collect old annotations
        Collection<Annotation> oldAnnotations =
            Util.fromIterator(model.getAnnotationIterator());
                    
        // Collect new annotations
        HashMap<Annotation,Position> newAnnotations = 
            new HashMap<Annotation,Position>();

        IFoldingNode node = fRoot;
        ITextSelection sel = getSelection();

        for (Position pos : node.foldingPositions(this.getDocument())) {
            
            if (fIsLibrary && 
                    !((IFilePosition) pos).getFileName().equals(fPath))
                continue;
            
            ProjectionAnnotation annotation;
            if (pos instanceof CharacterPosition) {
                annotation = new CharacterProjectionAnnotation();
                if (!toggleAnnotationsAction.isVisible() &&
                    !pos.overlapsWith(sel.getOffset(), sel.getLength())) 
                {
                    annotation.markCollapsed();
                }
            } else {
                annotation = new ProjectionAnnotation();
            }
            
            newAnnotations.put(annotation, pos);
        }


        // TODO: Only replace annotations that have changed, so that the 
        // state of the others are kept.

        // Update annotations
        model.modifyAnnotations( 
                oldAnnotations.toArray(new Annotation[] {}),
                newAnnotations, 
                null);
    }

    @Override
    protected void handleCursorPositionChanged() {
        super.handleCursorPositionChanged();
        updateErrorCheckAction();
    }
    
    /**
     * Sets error check action to check the class 
     * currently containing selection.
     */
    private void updateErrorCheckAction() {
        try {
            ITextSelection sel = getSelection(); 
            errorCheckAction.setCurClass(
                 fRoot.containingClass(sel.getOffset(), sel.getLength()));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean selectNode(ASTNode node) {
        
        String path = getPathOfInput(getEditorInput());
        if (!path.equals(node.containingFileName()))
            return false;
        
        ASTNode sel = node.getSelectionNode();
        if (sel.getOffset() >= 0 && sel.getLength() >= 0) 
            selectAndReveal(sel.getOffset(), sel.getLength());

        return true;
    }

    private class DoOperationAction extends Action {
        private int action;

        public DoOperationAction(String text, int action) {
            super(text);
            this.action = action;
        }
        
        @Override
        public void run() {
            ISourceViewer sourceViewer = getSourceViewer();
            if (sourceViewer instanceof ITextOperationTarget) {
                ((ITextOperationTarget) sourceViewer).doOperation(action);
            }
        }
    }

    private class ExpandAllAction extends DoOperationAction {
        public ExpandAllAction() {
            super("&Expand All", CharacterProjectionViewer.EXPAND_ALL);
        }
    }

    private class CollapseAllAction extends DoOperationAction {
        public CollapseAllAction() {
            super("&Collapse All", CharacterProjectionViewer.COLLAPSE_ALL);
        }
    }

    public IDocument getDocument() {
        return getSourceViewer() == null ? null : getSourceViewer().getDocument();
    }
    
    public ISourceViewer publicGetSourceViewer() {
        return getSourceViewer();
    }

    public ITextSelection getSelection() {
        return (ITextSelection)getSelectionProvider().getSelection();
    }
    
    public IReconcilingStrategy getStrategy() {
        return fCompiledLocal ? fLocalStrategy : fStrategy;
    }
}