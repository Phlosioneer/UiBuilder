package main;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import main.UiBuilder.ToolType;

public class Editor extends Canvas implements PaintListener, MouseListener, MouseMoveListener {

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

	public Editor(Composite parent, Document document) {
		super(parent, SWT.NONE);
		assert (document != null);
		this.document = document;
		var actionListener = document.getUndoStack().addListener(action->redraw());
		var selectionListener = document.addSelectionListener(rect->redraw());

		addDisposeListener(e-> {
			document.getUndoStack().removeListener(actionListener);
			document.removeSelectionListener(selectionListener);
		});

		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener(this);

		setBackground(new Color(getDisplay(), 255, 255, 255));
		currentTool = ToolType.Place;

		BLACK = new Color(getDisplay(), 0, 0, 0);
		GRAY = new Color(getDisplay(), 150, 150, 150);
	}

	@Override
	public void paintControl(PaintEvent event) {
		var context = event.gc;
		context.setForeground(BLACK);
		var file = DocumentManager.getCurrentDocument();
		var size = getSize();
		for (var rect : file.getRectangles()) {
			int x = (int) Math.round(rect.x * size.x);
			int y = (int) Math.round(rect.y * size.y);
			int width = (int) Math.round(rect.width * size.x);
			int height = (int) Math.round(rect.height * size.y);
			context.drawRectangle(x, y, width, height);
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

		var selected = file.getSelectedRectangle();
		if (selected != null) {
			int x = (int) Math.round(selected.x * size.x);
			int y = (int) Math.round(selected.y * size.y);
			int x2 = x + (int) Math.round(selected.width * size.x);
			int y2 = y + (int) Math.round(selected.height * size.y);
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
		redraw();
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
				var size = getSize();
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
		redraw();
	}

	@Override
	public void mouseUp(MouseEvent event) {
		if (mouseIsDown) {
			mouseIsDown = false;
			var size = getSize();
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
}
