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
	private Rectangle selectedRectangle;

	private ArrayList<CreationListener> creationListeners;
	private ArrayList<SelectionListener> selectionListeners;
	private ArrayList<ModificationListener> modificationListeners;
	private ArrayList<TemporaryModificationListener> temporaryModificationListeners;

	public Preview(Composite parent) {
		super(parent, SWT.NONE);
		data = new ArrayList<>();
		addMouseListener(this);
		addMouseMoveListener(this);
		addPaintListener(this);

		creationListeners = new ArrayList<>();
		selectionListeners = new ArrayList<>();
		modificationListeners = new ArrayList<>();
		temporaryModificationListeners = new ArrayList<>();
		selectedRectangle = null;

		setBackground(new Color(getDisplay(), 255, 255, 255));
	}

	@Override
	public void paintControl(PaintEvent event) {
		var context = event.gc;

		context.setForeground(new Color(getDisplay(), 0, 0, 0));
		for (var rect : data) {
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
			var width = Math.abs(currentMouseX - mouseDownX);
			var height = Math.abs(currentMouseY - mouseDownY);
			var x = Math.min(currentMouseX, mouseDownX);
			var y = Math.min(currentMouseY, mouseDownY);
			notifyCreationListeners(x, y, width, height);
			redraw();
		}
	}

	public void setData(ArrayList<Rectangle> newData) {
		if (newData == null) {
			data.clear();
		} else {
			data = newData;
		}

		mouseIsDown = false;
		notifySelectionListeners(null);
	}

	/**
	 * 
	 * @param rect
	 *            Null for deselection.
	 */
	public void setSelection(Rectangle rect) {
		if (rect == selectedRectangle) {
			return;
		}
		selectedRectangle = rect;
		redraw();
	}

	/////////////////////////////////
	// Listener management

	public void addCreationListener(CreationListener listener) {
		creationListeners.add(listener);
	}

	public void addSelectionListener(SelectionListener listener) {
		selectionListeners.add(listener);
	}

	public void addModificationListener(ModificationListener listener) {
		modificationListeners.add(listener);
	}

	public void addTemporaryModificationListener(TemporaryModificationListener listener) {
		temporaryModificationListeners.add(listener);
	}

	public boolean removeCreationListener(CreationListener listener) {
		return creationListeners.remove(listener);
	}

	public boolean removeSelectionListener(SelectionListener listener) {
		return selectionListeners.remove(listener);
	}

	public boolean removeModificationListener(ModificationListener listener) {
		return modificationListeners.remove(listener);
	}

	public boolean removeTemporaryModificationListener(TemporaryModificationListener listener) {
		return temporaryModificationListeners.remove(listener);
	}

	private void notifyCreationListeners(int x, int y, int width, int height) {
		// We have to clone the listener list so that listeners can add/remove themselves during
		// the iteration.
		var tempListenerArray = new ArrayList<>(creationListeners);

		for (var listener : tempListenerArray) {
			var castListener = listener;
			castListener.created(x, y, width, height);
		}
	}

	private void notifySelectionListeners(Rectangle rectangle) {
		// We have to clone the listener list so that listeners can add/remove themselves during
		// the iteration.
		var tempListenerArray = new ArrayList<>(selectionListeners);

		for (var listener : tempListenerArray) {
			var castListener = listener;
			castListener.selected(rectangle);
		}
	}

	private void notifyModificationListeners(Rectangle rectangle) {
		// We have to clone the listener list so that listeners can add/remove themselves during
		// the iteration.
		var tempListenerArray = new ArrayList<>(modificationListeners);

		for (var listener : tempListenerArray) {
			var castListener = listener;
			castListener.changed(rectangle);
		}
	}

	private void notifyTemporaryModificationListeners(Rectangle rectangle, int x, int y, int width, int height) {
		// We have to clone the listener list so that listeners can add/remove themselves during
		// the iteration.
		var tempListenerArray = new ArrayList<>(temporaryModificationListeners);

		for (var listener : tempListenerArray) {
			var castListener = listener;
			castListener.changed(rectangle, x, y, width, height);
		}
	}

	/**
	 * Notify of cancellation
	 */
	private void notifyTemporaryModificationListeners() {
		// We have to clone the listener list so that listeners can add/remove themselves during
		// the iteration.
		var tempListenerArray = new ArrayList<>(temporaryModificationListeners);

		for (var listener : tempListenerArray) {
			var castListener = listener;
			castListener.cancelled();
		}
	}

	public static interface CreationListener {
		void created(int x, int y, int width, int height);
	}

	public static interface SelectionListener {
		/**
		 * 
		 * @param rectangle
		 *            Null for deselection event.
		 */
		void selected(Rectangle rectangle);
	}

	/**
	 * Notified when the user is done moving or resizing a rectangle.
	 */
	public static interface ModificationListener {
		void changed(Rectangle rect);
	}

	/**
	 * Notified of changes while the user is still holding the handle of the rectangle.
	 * The cancellation event is always sent before any other related events (such as
	 * selection or modification events).
	 */
	public static interface TemporaryModificationListener {
		/**
		 * By default, rect is set to null for a cancellation event.
		 */
		void changed(Rectangle rect, int x, int y, int width, int height);

		default void cancelled() {
			changed(null, 0, 0, 0, 0);
		}
	}
}
