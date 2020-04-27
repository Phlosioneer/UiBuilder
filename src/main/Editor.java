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
	private boolean mouseIsDown;
	private ToolType currentTool;

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

	public Editor(TabFolder parent, Document document) {
		assert (document != null);
		this.document = document;
		tab = new TabItem(parent, SWT.NONE);
		tab.setData(TAB_ITEM_DATA_NAME, this);

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
			}
			int scaledX = (int) Math.round(rectX * size.x);
			int scaledY = (int) Math.round(rectY * size.y);
			int scaledWidth = (int) Math.round(rectWidth * size.x);
			int scaledHeight = (int) Math.round(rectHeight * size.y);
			context.drawRectangle(scaledX, scaledY, scaledWidth, scaledHeight);
		}

		if (mouseIsDown && currentTool == ToolType.Place) {
			// drawRectangle handles negative width/height almost correctly; it's off by one pixel
			// on negative lengths.
			int width = currentMouseX - mouseDownX;
			int height = currentMouseY - mouseDownY;
			if (width < 0) {
				width -= 1;
			}
			if (height < 0) {
				height -= 1;
			}
			context.drawRectangle(mouseDownX, mouseDownY, width, height);
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

	private void updateHandles() {
		var selected = document.getSelectedRectangle();
		if (selected == null) {
			for (var handle : allHandles) {
				handle.x = -1;
				handle.y = -1;
				handle.width = 0;
				handle.height = 0;
			}
			return;
		}

		var size = canvas.getSize();
		double rectX = selected.x;
		double rectY = selected.y;
		double rectWidth = selected.width;
		double rectHeight = selected.height;
		if (document.hasTempResize()) {
			rectX = document.getTempX();
			rectY = document.getTempY();
			rectWidth = document.getTempWidth();
			rectHeight = document.getTempHeight();
		}
		int roundedX = (int) Math.round(rectX * size.x);
		int roundedY = (int) Math.round(rectY * size.y);
		int roundedWidth = (int) Math.round(rectWidth * size.x);
		int roundedHeight = (int) Math.round(rectHeight * size.y);

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

		for (var handle : allHandles) {
			handle.width = HANDLE_SIZE;
			handle.height = HANDLE_SIZE;
		}

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
		if (mouseIsDown) {
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
			currentMouseX = event.x;
			currentMouseY = event.y;
			canvas.redraw();
		} else if (selected != null) {
			boolean cursorSet = false;
			for (int i = 0; i < allHandles.size(); i++) {
				var handle = allHandles.get(i);
				if (handle.contains(event.x, event.y)) {
					var cursor = handleCursors[i];
					canvas.setCursor(cursor);
					cursorSet = true;
					break;
				}
			}
			var size = canvas.getSize();
			if (!cursorSet && event.x >= selected.x * size.x && event.x < (selected.x + selected.width) * size.x) {
				if (event.y >= selected.y * size.y && event.y < (selected.y + selected.height) * size.y) {
					canvas.setCursor(canvas.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
					cursorSet = true;
				}
			}
			if (!cursorSet) {
				canvas.setCursor(canvas.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			}
		} else {
			canvas.setCursor(canvas.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent event) {
		// Ignored
	}

	@Override
	public void mouseDown(MouseEvent event) {
		switch (currentTool) {
			case Place:
				// Start a drag event.
				mouseIsDown = true;
				mouseDownX = event.x;
				mouseDownY = event.y;
				currentMouseX = mouseDownX;
				currentMouseY = mouseDownY;
				canvas.setCursor(canvas.getDisplay().getSystemCursor(SWT.CURSOR_SIZESE));
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
		canvas.redraw();
	}

	@Override
	public void mouseUp(MouseEvent event) {
		canvas.setCursor(new Cursor(canvas.getDisplay(), SWT.CURSOR_ARROW));
		if (mouseIsDown) {
			mouseIsDown = false;
			var size = canvas.getSize();
			double width = Math.abs(currentMouseX - mouseDownX) / (double) size.x;
			double height = Math.abs(currentMouseY - mouseDownY) / (double) size.y;
			double x = Math.min(currentMouseX, mouseDownX) / (double) size.x;
			double y = Math.min(currentMouseY, mouseDownY) / (double) size.y;

			width = Math.round(width * 1000) / 1000.0;
			height = Math.round(height * 1000) / 1000.0;
			x = Math.round(x * 1000) / 1000.0;
			y = Math.round(y * 1000) / 1000.0;
			DocumentManager.getCurrentDocument().addRectangle(new main.Rectangle(x, y, width, height));
		}
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
	public void resizeStarted() {}

	@Override
	public void resize(double x, double y, double width, double height) {
		canvas.redraw();
	}

	@Override
	public void resizeCancelled() {
		canvas.redraw();
	}
}
