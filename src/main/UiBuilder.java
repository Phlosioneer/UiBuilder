
package main;

import java.io.File;
import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
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
import org.eclipse.wb.swt.SWTResourceManager;

public class UiBuilder {

	public static enum ToolType {
		Select, Place
	}

	private Menu menuBar;
	private ToolType selectedTool;
	private ArrayList<Editor> editors;

	///////////////////////////
	// Begin auto-generated fields

	protected Shell shlUibuilderUntitled;
	private MenuItem menuUndo;
	private MenuItem menuRedo;
	private ObjectListTab objectTab;
	private TabFolder documentTabs;
	private MenuItem menuDeselect;
	private MenuItem menuDelete;
	private MenuItem menuSelect;
	private MenuItem menuPlace;
	private ToolItem toolbarPlace;
	private ToolItem toolbarSelect;

	public UiBuilder() {
		selectedTool = ToolType.Place;
		editors = new ArrayList<>();
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
		} finally {
			SWTResourceManager.dispose();
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
		shlUibuilderUntitled.addShellListener(ShellListener.shellClosedAdapter(this::onExit));
		shlUibuilderUntitled.setMinimumSize(new Point(400, 400));
		shlUibuilderUntitled.setSize(942, 516);
		shlUibuilderUntitled.setText("UiBuilder");
		shlUibuilderUntitled.setLayout(new FormLayout());

		menuBar = new Menu(shlUibuilderUntitled, SWT.BAR);
		shlUibuilderUntitled.setMenuBar(menuBar);

		MenuItem mntmNewSubmenu = new MenuItem(menuBar, SWT.CASCADE);
		mntmNewSubmenu.setText("&File");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem menuNew = new MenuItem(menu_1, SWT.NONE);
		menuNew.setText("&New");
		menuNew.setAccelerator(SWT.CONTROL | 'N');

		MenuItem menuOpen = new MenuItem(menu_1, SWT.NONE);
		menuOpen.setText("&Open...");
		menuOpen.setAccelerator(SWT.CONTROL | 'O');

		MenuItem menuSave = new MenuItem(menu_1, SWT.NONE);
		menuSave.setText("&Save");
		menuSave.setAccelerator(SWT.CONTROL | 'S');

		MenuItem menuSaveAs = new MenuItem(menu_1, SWT.NONE);
		menuSaveAs.setText("Save &As...");
		menuSaveAs.setAccelerator(SWT.CONTROL | SWT.SHIFT | 'S');

		MenuItem menuClose = new MenuItem(menu_1, SWT.NONE);
		menuClose.setText("&Close");
		menuClose.setAccelerator(SWT.CONTROL | 'W');

		MenuItem menuCloseAll = new MenuItem(menu_1, SWT.NONE);
		menuCloseAll.setText("Close All");
		menuCloseAll.setAccelerator(SWT.CONTROL | SWT.SHIFT | 'W');

		new MenuItem(menu_1, SWT.SEPARATOR);

		MenuItem menuExit = new MenuItem(menu_1, SWT.NONE);
		menuExit.setText("E&xit");
		menuExit.setAccelerator(SWT.CONTROL | 'Q');

		MenuItem mntmEdit = new MenuItem(menuBar, SWT.CASCADE);
		mntmEdit.setText("&Edit");

		Menu menu_2 = new Menu(mntmEdit);
		mntmEdit.setMenu(menu_2);

		menuUndo = new MenuItem(menu_2, SWT.NONE);
		menuUndo.setText("&Undo");
		menuUndo.setAccelerator(SWT.CONTROL | 'Z');

		menuRedo = new MenuItem(menu_2, SWT.NONE);
		menuRedo.setText("&Redo");
		menuRedo.setAccelerator(SWT.CONTROL | SWT.SHIFT | 'Z');

		menuDeselect = new MenuItem(menu_2, SWT.NONE);
		menuDeselect.setText("&Deselect");
		menuDeselect.setAccelerator(SWT.CONTROL | 'D');

		menuDelete = new MenuItem(menu_2, SWT.NONE);
		menuDelete.setText("D&elete");
		menuDelete.setAccelerator(SWT.DEL);

		MenuItem mntmTools = new MenuItem(menuBar, SWT.CASCADE);
		mntmTools.setText("&Tools");

		Menu menu_3 = new Menu(mntmTools);
		mntmTools.setMenu(menu_3);

		menuSelect = new MenuItem(menu_3, SWT.RADIO);
		menuSelect.setText("&Select");

		menuPlace = new MenuItem(menu_3, SWT.RADIO);
		menuPlace.setText("&Place");
		menuPlace.setSelection(true);

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

		ObjectInfoTab infoTab = new ObjectInfoTab(tabFolder);
		tbtmInfo.setControl(infoTab);

		TabItem tbtmProperties = new TabItem(tabFolder, SWT.NONE);
		tbtmProperties.setText("Properties");

		objectTab = new ObjectListTab(tabFolder, SWT.NONE);
		tbtmObjects.setControl(objectTab);

		ToolBar toolBar = new ToolBar(canvasContainer, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		toolbarSelect = new ToolItem(toolBar, SWT.RADIO);
		toolbarSelect.setText("Select");

		toolbarPlace = new ToolItem(toolBar, SWT.RADIO);
		toolbarPlace.setSelection(true);
		toolbarPlace.setText("Place");

		documentTabs = new TabFolder(canvasContainer, SWT.NONE);
		documentTabs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		/////////////////////////////////
		// End autogenerated code

		menuNew.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onNew));
		menuOpen.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onOpen));
		menuSave.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSave));
		menuSaveAs.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSaveAs));
		menuExit.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onExit));
		menuClose.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onClose));
		menuCloseAll.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onCloseAll));

		menuUndo.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onUndo));
		menuRedo.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onRedo));

		var onSelect = SelectionListener.widgetSelectedAdapter(e->setSelectedTool(ToolType.Select));
		var onPlace = SelectionListener.widgetSelectedAdapter(e->setSelectedTool(ToolType.Place));
		menuSelect.addSelectionListener(onSelect);
		toolbarSelect.addSelectionListener(onSelect);
		menuPlace.addSelectionListener(onPlace);
		toolbarPlace.addSelectionListener(onPlace);

		DocumentManager.setShouldCloseDocument(this::closeWithoutSaving);

		// Create a new tab whenever a document is created.
		var creationListener = DocumentManager.addCreationListener(this::createTab);
		var closeListener = DocumentManager.addCloseListener(this::destroyTab);

		// Setup the first tab.
		createTab(DocumentManager.getCurrentDocument());

		// Keep the tab folder and the document manager in sync.
		documentTabs.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onTabSelected));
		var selectionListener = DocumentManager.addSelectionListener(this::onDocumentSelected);

		var docSelectionListener = DocumentManager.addCurrentDocumentSelectionListener(rect->updateCurrentDocumentMenus());
		var docUndoListener = DocumentManager.addCurrentDocumentUndoActionListener(action->updateCurrentDocumentMenus());

		// Cleanup listeners (though this shouldn't be necessary).
		shlUibuilderUntitled.addDisposeListener(e-> {
			DocumentManager.removeCreationListener(creationListener);
			DocumentManager.removeCloseListener(closeListener);
			DocumentManager.removeSelectionListener(selectionListener);
			DocumentManager.removeCurrentDocumentListener(docSelectionListener);
			DocumentManager.removeCurrentDocumentListener(docUndoListener);
		});
	}

	private void createTab(Document document) {
		assert (document != null);
		Editor editor = new Editor(documentTabs, document);
		editors.add(editor);
		editor.setCurrentTool(selectedTool);
	}

	private void destroyTab(Document document) {
		assert (document != null);
		for (var tab : editors) {
			if (tab.getDocument() == document) {
				editors.remove(tab);
				tab.dispose();
				return;
			}
		}
		throw new RuntimeException("Unable to find an editor for document:" + document);
	}

	private void onTabSelected(SelectionEvent e) {
		assert (documentTabs.getSelection().length == 1);
		var tab = documentTabs.getSelection()[0];
		var editor = (Editor) tab.getData(Editor.TAB_ITEM_DATA_NAME);
		DocumentManager.setCurrentDocument(editor.getDocument());
		updateCurrentDocumentMenus();
	}

	private void onDocumentSelected(Document document) {
		for (var editor : editors) {
			if (editor.getDocument() == document) {
				documentTabs.setSelection(editor.getTabItem());
				break;
			}
		}
		updateCurrentDocumentMenus();
	}

	private void updateCurrentDocumentMenus() {
		var document = DocumentManager.getCurrentDocument();
		menuUndo.setEnabled(document.getUndoStack().canUndo());
		menuRedo.setEnabled(document.getUndoStack().canRedo());
		menuDelete.setEnabled(document.getSelectedRectangle() != null);
		menuDeselect.setEnabled(document.getSelectedRectangle() != null);
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

	private void onExit(ShellEvent e) {
		e.doit = DocumentManager.closeAllDocuments();
	}

	private void onClose(SelectionEvent e) {
		DocumentManager.closeDocument(DocumentManager.getCurrentDocument());
	}

	private void onCloseAll(SelectionEvent e) {
		DocumentManager.closeAllDocuments();
	}

	private void onUndo(SelectionEvent e) {
		DocumentManager.getCurrentDocument().getUndoStack().undo();
	}

	private void onRedo(SelectionEvent e) {
		DocumentManager.getCurrentDocument().getUndoStack().redo();
	}

	private void setSelectedTool(ToolType newToolType) {
		if (newToolType == selectedTool) {
			return;
		}
		selectedTool = newToolType;
		switch (selectedTool) {
			case Select:
				toolbarSelect.setSelection(true);
				menuSelect.setSelection(true);
				break;
			case Place:
				toolbarPlace.setSelection(true);
				menuPlace.setSelection(true);
				break;
			default:
				throw new RuntimeException();
		}
		for (var editor : editors) {
			editor.setCurrentTool(selectedTool);
		}
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
			dialog.setText(shlUibuilderUntitled.getText());
			return dialog.open() == SWT.YES;
		} else {
			return true;
		}
	}
}
