
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.wb.swt.SWTResourceManager;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class UiBuilder {

	public static enum ToolType {
		Select, Place
	}

	///////////////////////////
	// Begin auto-generated fields

	protected Shell shell;
	private Text customProperties;
	private Text nameField;
	private Text xField;
	private Text yField;
	private Text widthField;
	private Text heightField;
	private TabItem infoTab;
	private TabItem propertiesTab;
	private List objectList;
	private MenuItem menuUndo;
	private MenuItem menuRedo;

	//////////////////////////////
	// End auto-generated fields

	private UndoStack stack;
	private ArrayList<Rectangle> objects;
	private Path currentFile;
	private Menu menuBar;
	private boolean hasUnsavedChanges;
	private ToolType selectedTool;

	public UiBuilder() {
		stack = new UndoStack();
		objects = new ArrayList<>();
		currentFile = null;
		hasUnsavedChanges = false;
		selectedTool = ToolType.Select;
	}

	private void populateList() {
		// TODO: Also repaint canvas

		objectList.deselectAll();
		var descriptions = new String[objects.size()];
		for (int i = 0; i < descriptions.length; i++) {
			descriptions[i] = objects.get(i).toString();
		}
		objectList.setItems(descriptions);
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
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window. (auto-generated)
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(942, 516);
		shell.setText("SWT Application");
		shell.setLayout(new FormLayout());

		menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);

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

		Canvas canvas = new Canvas(shell, SWT.NONE);
		FormData fd_canvas = new FormData();
		fd_canvas.bottom = new FormAttachment(100, -10);
		fd_canvas.right = new FormAttachment(100, -273);
		fd_canvas.left = new FormAttachment(0, 10);
		canvas.setLayoutData(fd_canvas);

		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		fd_canvas.top = new FormAttachment(toolBar, 6);
		FormData fd_toolBar = new FormData();
		fd_toolBar.right = new FormAttachment(100, -273);
		fd_toolBar.left = new FormAttachment(0, 10);
		fd_toolBar.top = new FormAttachment(0);
		toolBar.setLayoutData(fd_toolBar);

		ToolItem toolbarSelect = new ToolItem(toolBar, SWT.NONE);
		toolbarSelect.setText("Select");

		ToolItem toolbarPlace = new ToolItem(toolBar, SWT.NONE);
		toolbarPlace.setText("Place");

		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		FormData fd_tabFolder = new FormData();
		fd_tabFolder.bottom = new FormAttachment(canvas, 0, SWT.BOTTOM);
		fd_tabFolder.right = new FormAttachment(100, -10);
		fd_tabFolder.top = new FormAttachment(toolBar, 0, SWT.TOP);
		fd_tabFolder.left = new FormAttachment(canvas, 6);
		tabFolder.setLayoutData(fd_tabFolder);

		TabItem tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_1.setText("Objects");

		objectList = new List(tabFolder, SWT.BORDER | SWT.V_SCROLL);
		tbtmNewItem_1.setControl(objectList);

		infoTab = new TabItem(tabFolder, SWT.NONE);
		infoTab.setText("Info");

		Composite composite = new Composite(tabFolder, SWT.NONE);
		infoTab.setControl(composite);

		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setBounds(10, 13, 55, 15);
		lblNewLabel.setText("Name");

		nameField = new Text(composite, SWT.BORDER);
		nameField.setBounds(163, 10, 76, 21);

		xField = new Text(composite, SWT.BORDER);
		xField.setBounds(163, 37, 76, 21);

		yField = new Text(composite, SWT.BORDER);
		yField.setBounds(163, 64, 76, 21);

		widthField = new Text(composite, SWT.BORDER);
		widthField.setBounds(163, 91, 76, 21);

		heightField = new Text(composite, SWT.BORDER);
		heightField.setBounds(163, 118, 76, 21);

		Label lblNewLabel_1 = new Label(composite, SWT.NONE);
		lblNewLabel_1.setBounds(10, 40, 55, 15);
		lblNewLabel_1.setText("x");

		Label lblNewLabel_2 = new Label(composite, SWT.NONE);
		lblNewLabel_2.setBounds(10, 67, 55, 15);
		lblNewLabel_2.setText("y");

		Label lblNewLabel_3 = new Label(composite, SWT.NONE);
		lblNewLabel_3.setBounds(10, 94, 55, 15);
		lblNewLabel_3.setText("width");

		Label lblNewLabel_4 = new Label(composite, SWT.NONE);
		lblNewLabel_4.setBounds(10, 121, 55, 15);
		lblNewLabel_4.setText("height");

		Button deleteButton = new Button(composite, SWT.NONE);
		deleteButton.setBounds(10, 384, 75, 25);
		deleteButton.setText("Delete");

		propertiesTab = new TabItem(tabFolder, SWT.NONE);
		propertiesTab.setText("Properties");

		customProperties = new Text(tabFolder, SWT.BORDER | SWT.MULTI);
		customProperties.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		propertiesTab.setControl(customProperties);

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

		deleteButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onDelete));

		toolbarPlace.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSelectTool));
		toolbarSelect.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSelectTool));

		objectList.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::onSelectionChange));
		populateList();
	}

	private void onDelete(SelectionEvent e) {
		// TODO: Delete it, using an undoable action.
		undoStackChanged();
		setHasUnsavedChanges(true);
	}

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
		shell.close();
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

	private void onSelectionChange(SelectionEvent e) {
		// TODO: Populate all the text fields.
	}

	private FileDialog makeDialog(int type) {
		var dialog = new FileDialog(shell, type);
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
			var dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
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
			shell.setText("UiBuilder - " + unsavedIndicator + "Untitled");
		} else {
			shell.setText("UiBuilder - " + unsavedIndicator + currentFile.getFileName());
		}
	}

	private void setHasUnsavedChanges(boolean newValue) {
		if (newValue != hasUnsavedChanges) {
			hasUnsavedChanges = newValue;
			updateTitle();
		}
	}
}
