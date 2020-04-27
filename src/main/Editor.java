package main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import main.Document.TemporaryResizeListener;
import main.UiBuilder.ToolType;

public class Editor implements PaintListener, MouseListener, MouseMoveListener, TemporaryResizeListener {

	public static final String TAB_ITEM_DATA_NAME = "ParentEditor";

	// In pixels.
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
			DocumentManager.removeSaveListener(saveListener);
		});

		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addPaintListener(this);

		canvas.setBackground(new Color(tab.getDisplay(), 255, 255, 255));
		currentTool = ToolType.Place;

		BLACK = new Color(tab.getDisplay(), 0, 0, 0);
		GRAY = new Color(tab.getDisplay(), 150, 150, 150);
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
		var context = event.gc;
		context.setForeground(BLACK);
		var file = DocumentManager.getCurrentDocument();
		var size = canvas.getSize();
		var selected = file.getSelectedRectangle();
		for (var rect : file.getRectangles()) {
			double rectX = rect.x;
			double rectY = rect.y;
			double rectWidth = rect.width;
			double rectHeight = rect.height;
			if (file.hasTempResize() && selected == rect) {
				rectX = file.getTempX();
				rectY = file.getTempY();
				rectWidth = file.getTempWidth();
				rectHeight = file.getTempHeight();
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
			double rectX = selected.x;
			double rectY = selected.y;
			double rectWidth = selected.width;
			double rectHeight = selected.height;
			if (file.hasTempResize()) {
				rectX = file.getTempX();
				rectY = file.getTempY();
				rectWidth = file.getTempWidth();
				rectHeight = file.getTempHeight();
			}
			int x = (int) Math.round(rectX * size.x);
			int y = (int) Math.round(rectY * size.y);
			int x2 = x + (int) Math.round(rectWidth * size.x);
			int y2 = y + (int) Math.round(rectHeight * size.y);
			x -= HANDLE_SPACING;
			y -= HANDLE_SPACING;
			x2 += HANDLE_SPACING;
			y2 += HANDLE_SPACING;

			// drawRectangle handles negative width/height almost correctly; it's off by one pixel
			// on negative lengths.
			context.setBackground(GRAY);
			context.fillRectangle(x, y, -HANDLE_SIZE, -HANDLE_SIZE);
			context.fillRectangle(x, y2, -HANDLE_SIZE, HANDLE_SIZE);
			context.fillRectangle(x2, y, HANDLE_SIZE, -HANDLE_SIZE);
			context.fillRectangle(x2, y2, HANDLE_SIZE, HANDLE_SIZE);
			context.setForeground(BLACK);
			context.drawRectangle(x, y, -HANDLE_SIZE - 1, -HANDLE_SIZE - 1);
			context.drawRectangle(x, y2, -HANDLE_SIZE - 1, HANDLE_SIZE);
			context.drawRectangle(x2, y, HANDLE_SIZE, -HANDLE_SIZE - 1);
			context.drawRectangle(x2, y2, HANDLE_SIZE, HANDLE_SIZE);
		}
	}

	@Override
	public void mouseMove(MouseEvent event) {
		currentMouseX = event.x;
		currentMouseY = event.y;
		canvas.redraw();
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
			DocumentManager.getCurrentDocument().addRectangle(new Rectangle(x, y, width, height));
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
