package main;

import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import actions.ResizeRectangleAction;
import main.Document.TemporaryResizeListener;
import main.UiBuilder.ToolType;

public class Editor implements PaintListener, MouseListener, MouseMoveListener, TemporaryResizeListener {

	public static final String TAB_ITEM_DATA_NAME = "ParentEditor";

	// In pixels.
	// This MUST be divisible by 2.
	private static final int HANDLE_SIZE = 8;
	private static final int HANDLE_SPACING = 2;

	private final Color BLACK;
	private final Color GRAY;

	private int mouseDownX;
	private int mouseDownY;
	private int currentMouseX;
	private int currentMouseY;
	private ToolType currentTool;

	// TODO: Use an enum here.
	private boolean movingRect;
	private boolean makingNewRect;
	private boolean resizingRect;

	private Document document;
	private TabItem tab;
	private Canvas canvas;

	// North = negative y
	// South = positive y
	// West = negative x
	// East = positive x
	private Rectangle handleNW;
	private Rectangle handleNE;
	private Rectangle handleSW;
	private Rectangle handleSE;
	private Rectangle handleN;
	private Rectangle handleS;
	private Rectangle handleW;
	private Rectangle handleE;
	private Rectangle visibleHandleNW;
	private Rectangle visibleHandleNE;
	private Rectangle visibleHandleSW;
	private Rectangle visibleHandleSE;
	private ArrayList<Rectangle> allHandles;
	private Cursor[] handleCursors;

	private Rectangle heldHandle;

	public Editor(TabFolder parent, Document document) {
		assert (document != null);
		this.document = document;
		tab = new TabItem(parent, SWT.NONE);
		tab.setData(TAB_ITEM_DATA_NAME, this);
		makingNewRect = false;
		resizingRect = false;
		heldHandle = null;

		tab.setText(document.getFileName());
		document.getUndoStack().addListener(action->updateSavedIndicators());

		canvas = new Canvas(parent, SWT.NONE);
		tab.setControl(canvas);

		var actionListener = document.getUndoStack().addListener(action->canvas.redraw());
		var selectionListener = document.addSelectionListener(rect->canvas.redraw());
		var tempResizeListener = document.addTemporaryResizeListener(this);
		var saveListener = DocumentManager.addSaveListener(savedDoc-> {
			if (savedDoc == document) {
				updateSavedIndicators();
			}
		});

		tab.addDisposeListener(e-> {
			document.getUndoStack().removeListener(actionListener);
			document.removeSelectionListener(selectionListener);
			document.removeTemporaryResizeListener(tempResizeListener);
			DocumentManager.removeSaveListener(saveListener);
		});

		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addPaintListener(this);

		canvas.setBackground(new Color(tab.getDisplay(), 255, 255, 255));
		currentTool = ToolType.Place;

		BLACK = new Color(tab.getDisplay(), 0, 0, 0);
		GRAY = new Color(tab.getDisplay(), 150, 150, 150);

		// Setup the cursor icons for each handle.
		handleNE = new Rectangle(-1, -1, 0, 0);
		handleNW = new Rectangle(-1, -1, 0, 0);
		handleSE = new Rectangle(-1, -1, 0, 0);
		handleSW = new Rectangle(-1, -1, 0, 0);
		handleN = new Rectangle(-1, -1, 0, 0);
		handleS = new Rectangle(-1, -1, 0, 0);
		handleE = new Rectangle(-1, -1, 0, 0);
		handleW = new Rectangle(-1, -1, 0, 0);
		visibleHandleNE = new Rectangle(-1, -1, 0, 0);
		visibleHandleNW = new Rectangle(-1, -1, 0, 0);
		visibleHandleSE = new Rectangle(-1, -1, 0, 0);
		visibleHandleSW = new Rectangle(-1, -1, 0, 0);
		allHandles = new ArrayList<>();
		handleCursors = new Cursor[12];
		var display = canvas.getDisplay();
		allHandles.add(handleNW);
		handleCursors[0] = display.getSystemCursor(SWT.CURSOR_SIZENWSE);
		allHandles.add(handleNE);
		handleCursors[1] = display.getSystemCursor(SWT.CURSOR_SIZENESW);
		allHandles.add(handleSW);
		handleCursors[2] = display.getSystemCursor(SWT.CURSOR_SIZENESW);
		allHandles.add(handleSE);
		handleCursors[3] = display.getSystemCursor(SWT.CURSOR_SIZENWSE);
		allHandles.add(handleN);
		handleCursors[4] = display.getSystemCursor(SWT.CURSOR_SIZENS);
		allHandles.add(handleS);
		handleCursors[5] = display.getSystemCursor(SWT.CURSOR_SIZENS);
		allHandles.add(handleW);
		handleCursors[6] = display.getSystemCursor(SWT.CURSOR_SIZEWE);
		allHandles.add(handleE);
		handleCursors[7] = display.getSystemCursor(SWT.CURSOR_SIZEWE);
		allHandles.add(visibleHandleNW);
		handleCursors[8] = display.getSystemCursor(SWT.CURSOR_SIZENWSE);
		allHandles.add(visibleHandleNE);
		handleCursors[9] = display.getSystemCursor(SWT.CURSOR_SIZENESW);
		allHandles.add(visibleHandleSW);
		handleCursors[10] = display.getSystemCursor(SWT.CURSOR_SIZENESW);
		allHandles.add(visibleHandleSE);
		handleCursors[11] = display.getSystemCursor(SWT.CURSOR_SIZENWSE);
	}

	public void updateSavedIndicators() {
		if (document.hasUnsavedChanges()) {
			tab.setText('*' + document.getFileName());
		} else {
			tab.setText(document.getFileName());
		}
	}

	@Override
	public void paintControl(PaintEvent event) {
		updateHandles();
		var context = event.gc;
		context.setForeground(BLACK);
		var size = canvas.getSize();
		var selected = document.getSelectedRectangle();
		for (var rect : document.getRectangles()) {
			double rectX = rect.x;
			double rectY = rect.y;
			double rectWidth = rect.width;
			double rectHeight = rect.height;
			if (document.hasTempResize() && selected == rect) {
				rectX = document.getTempX();
				rectY = document.getTempY();
				rectWidth = document.getTempWidth();
				rectHeight = document.getTempHeight();

				// The temp dimensions might not be normalized.
				rectX = Math.min(rectX, rectX + rectWidth);
				rectY = Math.min(rectY, rectY + rectHeight);
				rectWidth = Math.abs(rectWidth);
				rectHeight = Math.abs(rectHeight);
			}
			int scaledX = (int) Math.round(rectX * size.x);
			int scaledY = (int) Math.round(rectY * size.y);
			int scaledWidth = (int) Math.round(rectWidth * size.x);
			int scaledHeight = (int) Math.round(rectHeight * size.y);
			context.drawRectangle(scaledX, scaledY, scaledWidth, scaledHeight);
		}

		if (makingNewRect && currentTool == ToolType.Place) {
			int width = currentMouseX - mouseDownX;
			int height = currentMouseY - mouseDownY;

			// Normalize the rectangle before drawing it.
			int x = Math.min(mouseDownX, mouseDownX + width);
			int y = Math.min(mouseDownY, mouseDownY + height);
			width = Math.abs(width);
			height = Math.abs(height);

			context.drawRectangle(x, y, width, height);
		}

		if (selected != null) {
			context.setBackground(GRAY);
			context.setForeground(BLACK);

			context.fillRectangle(visibleHandleNW);
			context.fillRectangle(visibleHandleNE);
			context.fillRectangle(visibleHandleSW);
			context.fillRectangle(visibleHandleSE);

			context.drawRectangle(visibleHandleNW);
			context.drawRectangle(visibleHandleNE);
			context.drawRectangle(visibleHandleSW);
			context.drawRectangle(visibleHandleSE);
		}
	}

	/**
	 * Updates all handle positions around the selected rectangle.
	 */
	private void updateHandles() {
		var selected = document.getSelectedRectangle();
		if (selected == null) {
			// Move them all off-screen.
			for (var handle : allHandles) {
				handle.x = -1;
				handle.y = -1;
				handle.width = 0;
				handle.height = 0;
			}
			return;
		}

		var size = canvas.getSize();

		// Check if a resize event is happening.
		double rectX = selected.x;
		double rectY = selected.y;
		double rectWidth = selected.width;
		double rectHeight = selected.height;
		if (document.hasTempResize()) {
			rectX = document.getTempX();
			rectY = document.getTempY();
			rectWidth = document.getTempWidth();
			rectHeight = document.getTempHeight();

			// Coordinates from temp resize might not be normalized.
			rectX = Math.min(rectX, rectX + rectWidth);
			rectY = Math.min(rectY, rectY + rectHeight);
			rectWidth = Math.abs(rectWidth);
			rectHeight = Math.abs(rectHeight);
		}

		// Convert to canvas coordinates.
		int roundedX = (int) Math.round(rectX * size.x);
		int roundedY = (int) Math.round(rectY * size.y);
		int roundedWidth = (int) Math.round(rectWidth * size.x);
		int roundedHeight = (int) Math.round(rectHeight * size.y);

		// All handles have the same dimensions unless modified below.
		for (var handle : allHandles) {
			handle.width = HANDLE_SIZE;
			handle.height = HANDLE_SIZE;
		}

		// Calculate positions for all visible handles. They're offset
		// from the selected rectangle by HANDLE_SPACING pixels.
		{
			int x1 = roundedX - HANDLE_SPACING - HANDLE_SIZE;
			int y1 = roundedY - HANDLE_SPACING - HANDLE_SIZE;
			int x2 = roundedX + roundedWidth + HANDLE_SPACING;
			int y2 = roundedY + roundedHeight + HANDLE_SPACING;

			visibleHandleNW.x = x1;
			visibleHandleSW.x = x1;
			visibleHandleNE.x = x2;
			visibleHandleSE.x = x2;

			visibleHandleNW.y = y1;
			visibleHandleNE.y = y1;
			visibleHandleSW.y = y2;
			visibleHandleSE.y = y2;
		}

		// Calculate the positions for all invisible handles. They're
		// offset by halfo of HANDLE_SIZE so that they're evenly divided
		// across the outline of the rectangle.
		{
			int x1 = roundedX - HANDLE_SIZE / 2;
			int y1 = roundedY - HANDLE_SIZE / 2;
			int x2 = roundedX + roundedWidth - HANDLE_SIZE / 2;
			int y2 = roundedY + roundedHeight - HANDLE_SIZE / 2;

			handleNW.x = x1;
			handleSW.x = x1;
			handleW.x = x1;
			handleNE.x = x2;
			handleSE.x = x2;
			handleE.x = x2;

			handleNW.y = y1;
			handleNE.y = y1;
			handleN.y = y1;
			handleSW.y = y2;
			handleSE.y = y2;
			handleS.y = y2;
		}

		// Calculate the widths and heights for the side handles, and
		// adjust their positions so that they don't overlap with the
		// corner handles.
		{
			handleN.x = roundedX + HANDLE_SIZE / 2;
			handleS.x = roundedX + HANDLE_SIZE / 2;
			handleN.width = roundedWidth - HANDLE_SIZE;
			handleS.width = roundedWidth - HANDLE_SIZE;

			handleW.y = roundedY + HANDLE_SIZE / 2;
			handleE.y = roundedY + HANDLE_SIZE / 2;
			handleW.height = roundedHeight - HANDLE_SIZE;
			handleE.height = roundedHeight - HANDLE_SIZE;
		}
	}

	@Override
	public void mouseMove(MouseEvent event) {
		var selected = document.getSelectedRectangle();
		if (makingNewRect) {
			// Determine which cursor to use.
			int cursorType;
			if (currentMouseX >= mouseDownX) {
				if (currentMouseY >= mouseDownY) {
					cursorType = SWT.CURSOR_SIZESE;
				} else {
					cursorType = SWT.CURSOR_SIZENE;
				}
			} else {
				if (currentMouseY >= mouseDownY) {
					cursorType = SWT.CURSOR_SIZESW;
				} else {
					cursorType = SWT.CURSOR_SIZENW;
				}
			}
			canvas.setCursor(canvas.getDisplay().getSystemCursor(cursorType));

			// Save the current mouse position.
			currentMouseX = event.x;
			currentMouseY = event.y;
			canvas.redraw();

		} else if (resizingRect) {
			// Save the current mouse position.
			currentMouseX = event.x;
			currentMouseY = event.y;

			// Calculate the new resize coordinates.
			updateTempResize();

			// Don't change the cursor icon.
			canvas.redraw();

		} else if (movingRect) {
			// Save the current mouse position.
			currentMouseX = event.x;
			currentMouseY = event.y;

			var size = canvas.getSize();
			double deltaX = (currentMouseX - mouseDownX) / (double) size.x;
			double deltaY = (currentMouseY - mouseDownY) / (double) size.y;

			double newX = selected.x + deltaX;
			double newY = selected.y + deltaY;

			newX = round(newX);
			newY = round(newY);
			document.setTempSize(this, newX, newY, selected.width, selected.height);

			// Don't change the cursor icon.
			canvas.redraw();
		} else if (selected != null) {
			boolean cursorSet = false;

			// If the mouse is over a handle, set the cursor to the corresponding icon.
			for (int i = 0; i < allHandles.size(); i++) {
				var handle = allHandles.get(i);
				if (handle.contains(event.x, event.y)) {
					var cursor = handleCursors[i];
					canvas.setCursor(cursor);
					cursorSet = true;
					break;
				}
			}

			// If not over a handle, check if we're over the selected rectangle's interior.
			var size = canvas.getSize();
			if (!cursorSet && event.x >= selected.x * size.x && event.x < (selected.x + selected.width) * size.x) {
				if (event.y >= selected.y * size.y && event.y < (selected.y + selected.height) * size.y) {
					// This is the four-arrows "move" cursor.
					canvas.setCursor(canvas.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
					cursorSet = true;
				}
			}
			if (!cursorSet) {
				// Reset the cursor back to normal.
				canvas.setCursor(canvas.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			}
		} else {
			// Reset the cursor back to normal.
			canvas.setCursor(canvas.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent event) {
		// Ignored
	}

	@Override
	public void mouseDown(MouseEvent event) {
		// Selection handles ignore the current tool.
		var selected = document.getSelectedRectangle();
		if (selected != null) {
			// Check the handles.
			for (var handle : allHandles) {
				if (handle.contains(event.x, event.y)) {
					resizingRect = true;
					mouseDownX = event.x;
					mouseDownY = event.y;
					currentMouseX = event.x;
					currentMouseY = event.y;
					heldHandle = handle;

					// Start a resize event.
					document.setTempSize(this, selected.x, selected.y, selected.width, selected.height);

					// Skip the logic for the current tool.
					return;
				}
			}

			// Check the shape itself.
			var size = canvas.getSize();
			if (event.x > selected.x * size.x && event.x < (selected.x + selected.width) * size.x) {
				if (event.y > selected.y * size.y && event.y < (selected.y + selected.height) * size.y) {
					movingRect = true;
					mouseDownX = event.x;
					mouseDownY = event.y;
					currentMouseX = event.x;
					currentMouseY = event.y;

					// Start a resize event.
					document.setTempSize(this, selected.x, selected.y, selected.width, selected.height);

					// Skip the logic for the current tool.
					return;
				}
			}
		}

		switch (currentTool) {
			case Place:
				// Start a drag event.
				makingNewRect = true;
				mouseDownX = event.x;
				mouseDownY = event.y;
				currentMouseX = mouseDownX;
				currentMouseY = mouseDownY;
				// Set a default cursor icon; this will be corrected to the actual appropriate
				// icon whenever the user moves their mouse.
				canvas.setCursor(canvas.getDisplay().getSystemCursor(SWT.CURSOR_SIZESE));
				canvas.redraw();
				break;
			case Select:
				// Find the rectangle under the mouse, and select it. If no rectagle is
				// under the mouse, clear the selection.
				var size = canvas.getSize();
				double mouseX = event.x / (double) size.x;
				double mouseY = event.y / (double) size.y;
				document.setSelectedRectangle(null);
				for (var rect : document.getRectangles()) {
					if (rect.x <= mouseX && rect.x + rect.width > mouseX) {
						if (rect.y <= mouseY && rect.y + rect.height > mouseY) {
							document.setSelectedRectangle(rect);
							break;
						}
					}
				}
				break;
		}
	}

	@Override
	public void mouseUp(MouseEvent event) {
		canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_ARROW));
		if (makingNewRect) {
			makingNewRect = false;
			var size = canvas.getSize();

			// Convert to normalized ratio coordinates.
			double width = Math.abs(currentMouseX - mouseDownX) / (double) size.x;
			double height = Math.abs(currentMouseY - mouseDownY) / (double) size.y;
			double x = Math.min(currentMouseX, mouseDownX) / (double) size.x;
			double y = Math.min(currentMouseY, mouseDownY) / (double) size.y;

			// Round to 3 decimal places.
			width = round(width);
			height = round(height);
			x = round(x);
			y = round(y);

			DocumentManager.getCurrentDocument().addRectangle(new main.Rectangle(x, y, width, height));
		} else if (resizingRect) {
			// Let the resize function do most of the calculations for us.
			updateTempResize();

			// Get the sizes we calculated in updateTempResize.
			double newX = document.getTempX();
			double newY = document.getTempY();
			double newWidth = document.getTempWidth();
			double newHeight = document.getTempHeight();

			// Normalize the rectangle.
			newX = Math.min(newX, newX + newWidth);
			newY = Math.min(newY, newY + newHeight);
			newWidth = Math.abs(newWidth);
			newHeight = Math.abs(newHeight);

			newX = round(newX);
			newY = round(newY);
			newWidth = round(newWidth);
			newHeight = round(newHeight);
			document.cancelTempSize(this);

			var selected = document.getSelectedRectangle();
			document.getUndoStack().push(new ResizeRectangleAction(selected, newX, newY, newWidth, newHeight));

			resizingRect = false;
			heldHandle = null;
		} else if (movingRect) {
			var size = canvas.getSize();
			double deltaX = (currentMouseX - mouseDownX) / (double) size.x;
			double deltaY = (currentMouseY - mouseDownY) / (double) size.y;

			var selected = document.getSelectedRectangle();
			double newX = selected.x + deltaX;
			double newY = selected.y + deltaY;

			newX = round(newX);
			newY = round(newY);
			document.cancelTempSize(this);
			document.getUndoStack().push(new ResizeRectangleAction(selected, newX, newY, selected.width, selected.height));

			movingRect = false;
		}
	}

	private void updateTempResize() {
		assert (heldHandle != null);
		var size = canvas.getSize();
		// General strategy: pick a corner to keep constant, then change width and height
		// by the movement of the mouse.

		// Get the distance the mouse has moved.
		double deltaX = (currentMouseX - mouseDownX) / (double) size.x;
		double deltaY = (currentMouseY - mouseDownY) / (double) size.y;

		double x;
		double y;
		double width;
		double height;

		var rect = document.getSelectedRectangle();

		if (heldHandle == handleNW || heldHandle == handleSW || heldHandle == handleW || heldHandle == visibleHandleNW || heldHandle == visibleHandleSW) {
			// Northern handles and vertical edges: use one of the southern corners.
			x = rect.x + rect.width;
			width = -rect.width;
		} else {
			// Southern handles: use one of the northern corners.
			x = rect.x;
			width = rect.width;
		}
		if (heldHandle == handleNW || heldHandle == handleNE || heldHandle == handleN || heldHandle == visibleHandleNW || heldHandle == visibleHandleNE) {
			// Western handles and horizontal edges: use one of the eastern corners.
			y = rect.y + rect.height;
			height = -rect.height;
		} else {
			// Eastern handles: use one of the western corners.
			y = rect.y;
			height = rect.height;
		}

		if (heldHandle != handleN && heldHandle != handleS) {
			// Update the width unless we're holding a north/south handle.
			width += deltaX;
		}
		if (heldHandle != handleW && heldHandle != handleE) {
			// Update the height unless we're holding an east/west handle.
			height += deltaY;
		}

		// Round.
		x = round(x);
		y = round(y);
		width = round(width);
		height = round(height);
		document.setTempSize(this, x, y, width, height);
	}

	public ToolType getCurrentTool() {
		return currentTool;
	}

	public void setCurrentTool(ToolType currentTool) {
		this.currentTool = currentTool;
	}

	public Document getDocument() {
		return document;
	}

	public void dispose() {
		tab.dispose();
		canvas.dispose();
	}

	public TabItem getTabItem() {
		return tab;
	}

	@Override
	public void resizeStarted(Object source) {}

	@Override
	public void resize(double x, double y, double width, double height) {
		canvas.redraw();
	}

	@Override
	public void resizeCancelled(Object source) {
		canvas.redraw();
	}

	private static double round(double value) {
		return Math.round(value * 1000) / 1000.0;
	}
}
