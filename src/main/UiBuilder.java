
package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
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
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class UiBuilder {

	public static enum ToolType {
		Select, Place
	}

	private UndoStack stack;
	private ArrayList<Rectangle> objects;
	private Path currentFile;
	private Menu menuBar;
	private boolean hasUnsavedChanges;
	private ToolType selectedTool;

	///////////////////////////
	// Begin auto-generated fields

	protected Shell shlUibuilderUntitled;
	private MenuItem menuUndo;
	private MenuItem menuRedo;
	private ObjectListTab objectTab;
	private Preview preview;

	public UiBuilder() {
		stack = new UndoStack();
		objects = new ArrayList<>();
		currentFile = null;
		hasUnsavedChanges = false;
		selectedTool = ToolType.Select;
	}

	private void populateList() {
		var descriptions = new String[objects.size()];
		for (int i = 0; i < descriptions.length; i++) {
			descriptions[i] = objects.get(i).toString();
		}
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
		canvasContainer.setLayout(new FillLayout(SWT.HORIZONTAL));
		FormData fd_canvasContainer = new FormData();
		fd_canvasContainer.bottom = new FormAttachment(100, -10);
		fd_canvasContainer.right = new FormAttachment(100, -273);
		fd_canvasContainer.left = new FormAttachment(0, 10);
		canvasContainer.setLayoutData(fd_canvasContainer);

		TabFolder tabFolder = new TabFolder(shlUibuilderUntitled, SWT.NONE);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.bottom = new FormAttachment(100);
		fd_tabFolder.right = new FormAttachment(100);
		fd_tabFolder.left = new FormAttachment(canvasContainer, 6);
		tabFolder.setLayoutData(fd_tabFolder);

		ToolBar toolBar = new ToolBar(shlUibuilderUntitled, SWT.FLAT | SWT.RIGHT);
		fd_tabFolder.top = new FormAttachment(toolBar, 0, SWT.TOP);

		TabItem tbtmObjects = new TabItem(tabFolder, SWT.NONE);
		tbtmObjects.setText("Objects");

		TabItem tbtmInfo = new TabItem(tabFolder, SWT.NONE);
		tbtmInfo.setText("Info");

		TabItem tbtmProperties = new TabItem(tabFolder, SWT.NONE);
		tbtmProperties.setText("Properties");
		fd_canvasContainer.top = new FormAttachment(toolBar, 6);
		FormData fd_toolBar = new FormData();
		fd_toolBar.right = new FormAttachment(100, -273);
		fd_toolBar.left = new FormAttachment(0, 10);
		fd_toolBar.top = new FormAttachment(0);
		toolBar.setLayoutData(fd_toolBar);

		ToolItem toolbarSelect = new ToolItem(toolBar, SWT.NONE);
		toolbarSelect.setText("Select");

		ToolItem toolbarPlace = new ToolItem(toolBar, SWT.NONE);
		toolbarPlace.setText("Place");

		/////////////////////////////////
		// End autogenerated code

		objectTab = new ObjectListTab(tabFolder, SWT.NONE);
		tbtmObjects.setControl(objectTab);

		preview = new Preview(canvasContainer);

		menuNew.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onNew));
		menuOpen.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onOpen));
		menuSave.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSave));
		menuSaveAs.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSaveAs));
		menuExit.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onExit));

		menuUndo.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onUndo));
		menuRedo.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onRedo));

		menuSelect.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSelectTool));
		menuPlace.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onPlaceTool));

		toolbarPlace.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSelectTool));
		toolbarSelect.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSelectTool));
		populateList();

		preview.addSelectionListener(this::onSelectionChange);
		preview.addCreationListener(this::onRectCreated);

		objectTab.addRectangleSelectionListener(this::onSelectionChange);

	}

	private void setHasUnsavedChanges(boolean newValue) {
		if (newValue != hasUnsavedChanges) {
			hasUnsavedChanges = newValue;
			updateTitle();
		}
	}

	////////////////////////////////////////
	// Model Handlers

	private void onSelectionChange(Rectangle rect) {
		preview.setSelection(rect);
		objectTab.setSelectedRectangle(rect);
	}

	private void onRectCreated(int x, int y, int width, int height) {
		var previewSize = preview.getSize();
		double relX = (double) x / (double) previewSize.x;
		double relY = (double) y / (double) previewSize.y;
		double relWidth = (double) width / (double) previewSize.x;
		double relHeight = (double) height / (double) previewSize.y;

		var rect = new Rectangle(relX, relY, relWidth, relHeight);
		objects.add(rect);
		preview.setData(objects);
		objectTab.setData(objects);
		preview.setSelection(rect);
		objectTab.setSelectedRectangle(rect);
	}

	////////////////////////////////////////
	// UI Handlers

	private void onNew(SelectionEvent e) {
		if (!continueWithoutSaving()) {
			return;
		}
		stack.clear();
		objects.clear();
		populateList();
		currentFile = null;
		selectedTool = ToolType.Select;
		setHasUnsavedChanges(false);
		undoStackChanged();
	}

	private void onOpen(SelectionEvent e) {
		var dialog = makeDialog(SWT.OPEN);
		String choice = dialog.open();
		if (choice != null) {
			var file = new File(choice);
			currentFile = file.toPath();

			var gson = new Gson();
			var type = new TypeToken<ArrayList<Rectangle>>() {}.getType();
			try (var input = new BufferedReader(new FileReader(file))) {
				objects = gson.fromJson(input, type);
			} catch (JsonIOException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException("JsonIOException handler not yet written in onOpen of UiBuilder.", e1);
			} catch (JsonSyntaxException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException("JsonSyntaxException handler not yet written in onOpen of UiBuilder.", e1);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				throw new RuntimeException("FileNotFoundException handler not yet written in onOpen of UiBuilder.", e1);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				throw new RuntimeException("IOException handler not yet written in onOpen of UiBuilder.", e2);
			}

			populateList();
			selectedTool = ToolType.Select;
			stack.clear();
			setHasUnsavedChanges(false);
			undoStackChanged();
			// TODO: Repaint canvas
		}
	}

	private void onSave(SelectionEvent e) {
		if (currentFile == null) {
			onSaveAs(e);
		}

		var gson = new Gson();
		var type = new TypeToken<ArrayList<Rectangle>>() {}.getType();
		var outputString = gson.toJson(objects, type);
		try (var output = new FileWriter(currentFile.toFile())) {
			output.write(outputString);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			currentFile = null;
			throw new RuntimeException("IOException handler not yet written in onSave of UiBuilder.", e1);
		}
		setHasUnsavedChanges(false);
	}

	private void onSaveAs(SelectionEvent e) {
		var dialog = makeDialog(SWT.SAVE);
		String choice = dialog.open();
		if (choice != null) {
			currentFile = new File(choice).toPath();
			onSave(e);
			updateTitle();
		}
	}

	private void onExit(SelectionEvent e) {
		if (!continueWithoutSaving()) {
			return;
		}
		shlUibuilderUntitled.close();
	}

	private void onUndo(SelectionEvent e) {
		stack.undo();
		undoStackChanged();
		setHasUnsavedChanges(true);
	}

	private void onRedo(SelectionEvent e) {
		stack.redo();
		undoStackChanged();
		setHasUnsavedChanges(true);
	}

	private void undoStackChanged() {
		menuUndo.setEnabled(stack.canUndo());
		menuRedo.setEnabled(stack.canRedo());
		populateList();
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
		if (currentFile != null) {
			dialog.setFilterPath(currentFile.getParent().toAbsolutePath().toString());
		}
		return dialog;
	}

	private boolean continueWithoutSaving() {
		if (hasUnsavedChanges) {
			var dialog = new MessageBox(shlUibuilderUntitled, SWT.ICON_WARNING | SWT.YES | SWT.NO);
			dialog.setMessage("You have unsaved changes. Continue without saving?");
			return dialog.open() == SWT.YES;
		} else {
			return true;
		}
	}

	private void updateTitle() {
		String unsavedIndicator;
		if (hasUnsavedChanges) {
			unsavedIndicator = "*";
		} else {
			unsavedIndicator = "";
		}

		if (currentFile == null) {
			shlUibuilderUntitled.setText("UiBuilder - " + unsavedIndicator + "Untitled");
		} else {
			shlUibuilderUntitled.setText("UiBuilder - " + unsavedIndicator + currentFile.getFileName());
		}
	}
}
