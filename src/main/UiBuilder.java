
package main;

import java.io.File;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class UiBuilder {

	public static enum ToolType {
		Select, Place
	}

	private Menu menuBar;
	private ToolType selectedTool;

	///////////////////////////
	// Begin auto-generated fields

	protected Shell shlUibuilderUntitled;
	private MenuItem menuUndo;
	private MenuItem menuRedo;
	private ObjectListTab objectTab;
	private TabFolder documentTabs;

	public UiBuilder() {
		selectedTool = ToolType.Select;

		// TODO: Event for undo/redo actions
		DocumentManager.addSelectionListener(_document-> {
			updateSavedIndicators();
		});
	}

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UiBuilder window = new UiBuilder();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlUibuilderUntitled.open();
		shlUibuilderUntitled.layout();
		while (!shlUibuilderUntitled.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window. (auto-generated)
	 */
	protected void createContents() {
		shlUibuilderUntitled = new Shell();
		shlUibuilderUntitled.setMinimumSize(new Point(400, 400));
		shlUibuilderUntitled.setSize(942, 516);
		shlUibuilderUntitled.setText("UiBuilder - Untitled");
		shlUibuilderUntitled.setLayout(new FormLayout());

		menuBar = new Menu(shlUibuilderUntitled, SWT.BAR);
		shlUibuilderUntitled.setMenuBar(menuBar);

		MenuItem mntmNewSubmenu = new MenuItem(menuBar, SWT.CASCADE);
		mntmNewSubmenu.setText("File");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem menuNew = new MenuItem(menu_1, SWT.NONE);
		menuNew.setText("New");

		MenuItem menuOpen = new MenuItem(menu_1, SWT.NONE);
		menuOpen.setText("Open...");

		MenuItem menuSave = new MenuItem(menu_1, SWT.NONE);
		menuSave.setText("Save");

		MenuItem menuSaveAs = new MenuItem(menu_1, SWT.NONE);
		menuSaveAs.setText("Save As...");

		new MenuItem(menu_1, SWT.SEPARATOR);

		MenuItem menuExit = new MenuItem(menu_1, SWT.NONE);
		menuExit.setText("Exit");

		MenuItem mntmEdit = new MenuItem(menuBar, SWT.CASCADE);
		mntmEdit.setText("Edit");

		Menu menu_2 = new Menu(mntmEdit);
		mntmEdit.setMenu(menu_2);

		menuUndo = new MenuItem(menu_2, SWT.NONE);
		menuUndo.setText("Undo");

		menuRedo = new MenuItem(menu_2, SWT.NONE);
		menuRedo.setText("Redo");

		MenuItem mntmTools = new MenuItem(menuBar, SWT.CASCADE);
		mntmTools.setText("Tools");

		Menu menu_3 = new Menu(mntmTools);
		mntmTools.setMenu(menu_3);

		MenuItem menuSelect = new MenuItem(menu_3, SWT.NONE);
		menuSelect.setText("Select");

		MenuItem menuPlace = new MenuItem(menu_3, SWT.NONE);
		menuPlace.setText("Place");

		Composite canvasContainer = new Composite(shlUibuilderUntitled, SWT.NONE);
		GridLayout gl_canvasContainer = new GridLayout(1, false);
		gl_canvasContainer.marginWidth = 0;
		gl_canvasContainer.marginHeight = 0;
		gl_canvasContainer.verticalSpacing = 0;
		gl_canvasContainer.horizontalSpacing = 0;
		canvasContainer.setLayout(gl_canvasContainer);
		FormData fd_canvasContainer = new FormData();
		fd_canvasContainer.bottom = new FormAttachment(100);
		fd_canvasContainer.right = new FormAttachment(100, -273);
		fd_canvasContainer.left = new FormAttachment(0);
		canvasContainer.setLayoutData(fd_canvasContainer);

		TabFolder tabFolder = new TabFolder(shlUibuilderUntitled, SWT.NONE);
		fd_canvasContainer.top = new FormAttachment(tabFolder, 0, SWT.TOP);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.left = new FormAttachment(canvasContainer, 6);
		fd_tabFolder.right = new FormAttachment(100);
		fd_tabFolder.top = new FormAttachment(0);
		fd_tabFolder.bottom = new FormAttachment(100);
		tabFolder.setLayoutData(fd_tabFolder);

		TabItem tbtmObjects = new TabItem(tabFolder, SWT.NONE);
		tbtmObjects.setText("Objects");

		TabItem tbtmInfo = new TabItem(tabFolder, SWT.NONE);
		tbtmInfo.setText("Info");

		TabItem tbtmProperties = new TabItem(tabFolder, SWT.NONE);
		tbtmProperties.setText("Properties");

		objectTab = new ObjectListTab(tabFolder, SWT.NONE);
		tbtmObjects.setControl(objectTab);

		ToolBar toolBar = new ToolBar(canvasContainer, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		ToolItem tltmSelect = new ToolItem(toolBar, SWT.NONE);
		tltmSelect.setText("Select");

		ToolItem tltmPlace = new ToolItem(toolBar, SWT.NONE);
		tltmPlace.setText("Place");

		documentTabs = new TabFolder(canvasContainer, SWT.NONE);
		documentTabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		/////////////////////////////////
		// End autogenerated code

		menuNew.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onNew));
		menuOpen.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onOpen));
		menuSave.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSave));
		menuSaveAs.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSaveAs));
		menuExit.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onExit));

		menuUndo.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onUndo));
		menuRedo.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onRedo));

		menuSelect.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSelectTool));
		menuPlace.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onPlaceTool));

		createTab(DocumentManager.getCurrentDocument());

		DocumentManager.setShouldCloseDocument(this::closeWithoutSaving);
	}

	// TODO: Create new tabs whenever a document is created.
	private void createTab(Document document) {
		TabItem tbtmUntitled = new TabItem(documentTabs, SWT.NONE);
		tbtmUntitled.setText(document.getFileName());

		Preview preview = new Preview(documentTabs, document);
		tbtmUntitled.setControl(preview);
		preview.setLayout(new FormLayout());
	}

	private void onNew(SelectionEvent e) {
		DocumentManager.newDocument();
	}

	private void onOpen(SelectionEvent e) {
		var dialog = makeDialog(SWT.OPEN);
		String choice = dialog.open();
		if (choice != null) {
			DocumentManager.openDocument(new File(choice));
		}
	}

	private void onSave(SelectionEvent e) {
		if (DocumentManager.getCurrentDocument().getFilePath() == null) {
			onSaveAs(e);
		}
		DocumentManager.saveDocument(DocumentManager.getCurrentDocument());
		updateSavedIndicators();
	}

	private void onSaveAs(SelectionEvent e) {
		var dialog = makeDialog(SWT.SAVE);
		String choice = dialog.open();
		if (choice != null) {
			DocumentManager.getCurrentDocument().setFile(new File(choice));
			onSave(e);
		}
	}

	private void onExit(SelectionEvent e) {
		if (DocumentManager.closeAllDocuments()) {
			shlUibuilderUntitled.close();
		}
	}

	private void onClose(SelectionEvent e) {
		DocumentManager.closeDocument(DocumentManager.getCurrentDocument());
	}

	private void onCloseAll(SelectionEvent e) {
		DocumentManager.closeAllDocuments();
	}

	private void onUndo(SelectionEvent e) {
		DocumentManager.getCurrentDocument().getUndoStack().undo();
		updateSavedIndicators();
	}

	private void onRedo(SelectionEvent e) {
		DocumentManager.getCurrentDocument().getUndoStack().redo();
		updateSavedIndicators();
	}

	private void onSelectTool(SelectionEvent e) {
		selectedTool = ToolType.Select;
	}

	private void onPlaceTool(SelectionEvent e) {
		selectedTool = ToolType.Place;
	}

	private FileDialog makeDialog(int type) {
		var dialog = new FileDialog(shlUibuilderUntitled, type);
		String wildcard;
		if (SWT.getPlatform().startsWith("win")) {
			wildcard = "*.*";
		} else {
			wildcard = "*";
		}
		dialog.setFilterExtensions(new String[]{
			"*.json", wildcard
		});
		var currentFile = DocumentManager.getCurrentDocument();
		if (currentFile.getFilePath() != null) {
			dialog.setFilterPath(currentFile.getFilePath().getParent().toAbsolutePath().toString());
		}
		return dialog;
	}

	private boolean closeWithoutSaving(Document document) {
		if (document.hasUnsavedChanges()) {
			// Select the document first.
			DocumentManager.setCurrentDocument(document);

			// Prompt the user to confirm.
			var dialog = new MessageBox(shlUibuilderUntitled, SWT.ICON_WARNING | SWT.YES | SWT.NO);
			dialog.setMessage("You have unsaved changes. Continue without saving?");
			return dialog.open() == SWT.YES;
		} else {
			return true;
		}
	}

	private void updateSavedIndicators() {
		var currentFile = DocumentManager.getCurrentDocument();

		String unsavedIndicator;
		if (currentFile.hasUnsavedChanges()) {
			unsavedIndicator = "*";
		} else {
			unsavedIndicator = "";
		}

		shlUibuilderUntitled.setText("UiBuilder - " + unsavedIndicator + currentFile.getFileName());

		menuUndo.setEnabled(currentFile.getUndoStack().canUndo());
		menuRedo.setEnabled(currentFile.getUndoStack().canRedo());
	}
}
