package main;

import java.util.ArrayList;
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

	private ArrayList<Rectangle> data;
	private int mouseDownX;
	private int mouseDownY;
	private int currentMouseX;
	private int currentMouseY;
	private boolean mouseIsDown;

	public Preview(Composite parent) {
		super(parent, SWT.NONE);
		data = new ArrayList<>();
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener(this);

		setBackground(new Color(getDisplay(), 255, 255, 255));
	}

	@Override
	public void paintControl(PaintEvent event) {
		var context = event.gc;

		context.setForeground(new Color(getDisplay(), 0, 0, 0));
		for (var rect : data) {
			context.drawRectangle((int) Math.round(rect.x), (int) Math.round(rect.y), (int) Math.round(rect.width), (int) Math.round(rect.height));
		}

		if (mouseIsDown) {
			int width = currentMouseX - mouseDownX;
			int height = currentMouseY - mouseDownY;
			context.drawRectangle(mouseDownX, mouseDownY, width, height);
			System.out.println("mouseIsDown");
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
			// TODO: Notify listeners to add new rectangle.
			redraw();
		}
	}
}
