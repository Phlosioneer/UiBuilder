package actions;

import main.Document.UndoActionView;
import main.Rectangle;

public class ResizeRectangleAction implements UndoAction {

	private final Rectangle rect;

	private final double oldX;
	private final double oldY;
	private final double oldWidth;
	private final double oldHeight;

	private final double newX;
	private final double newY;
	private final double newWidth;
	private final double newHeight;

	public ResizeRectangleAction(Rectangle rect, double x, double y, double width, double height) {
		assert (rect != null);
		this.rect = rect;
		oldX = rect.x;
		oldY = rect.y;
		oldWidth = rect.width;
		oldHeight = rect.height;
		newX = x;
		newY = y;
		newWidth = width;
		newHeight = height;
	}

	@Override
	public void doAction(UndoActionView view) {
		assert (rect.x == oldX);
		assert (rect.y == oldY);
		assert (rect.width == oldWidth);
		assert (rect.height == oldHeight);
		rect.x = newX;
		rect.y = newY;
		rect.width = newWidth;
		rect.height = newHeight;
	}

	@Override
	public void undoAction(UndoActionView view) {
		assert (rect.x == newX);
		assert (rect.y == newY);
		assert (rect.width == newWidth);
		assert (rect.height == newHeight);
		rect.x = oldX;
		rect.y = oldY;
		rect.width = oldWidth;
		rect.height = oldHeight;
	}
}
