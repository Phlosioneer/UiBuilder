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

public class Preview extends Canvas implements PaintListener, MouseListener, MouseMoveListener {

	private int mouseDownX;
	private int mouseDownY;
	private int currentMouseX;
	private int currentMouseY;
	private boolean mouseIsDown;

	private Document document;

	public Preview(Composite parent, Document document) {
		super(parent, SWT.NONE);
		this.document = document;
		assert (document != null);
		document.getUndoStack().addListener(action->redraw());
		document.addSelectionListener(rect->redraw());

		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener(this);

		setBackground(new Color(getDisplay(), 255, 255, 255));
	}

	@Override
	public void paintControl(PaintEvent event) {
		var context = event.gc;
		context.setForeground(new Color(getDisplay(), 0, 0, 0));
		var file = DocumentManager.getCurrentDocument();
		for (var rect : file.getRectangles()) {
			var size = getSize();
			int x = (int) Math.round(rect.x * size.x);
			int y = (int) Math.round(rect.y * size.y);
			int width = (int) Math.round(rect.width * size.x);
			int height = (int) Math.round(rect.height * size.y);
			context.drawRectangle(x, y, width, height);
		}

		if (mouseIsDown) {
			// drawRectangle handles negative width/height correctly.
			int width = currentMouseX - mouseDownX;
			int height = currentMouseY - mouseDownY;
			context.drawRectangle(mouseDownX, mouseDownY, width, height);
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
		mouseIsDown = true;
		mouseDownX = event.x;
		mouseDownY = event.y;
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
			DocumentManager.getCurrentDocument().addRectangle(new Rectangle(x, y, width, height));
		}
	}
}
