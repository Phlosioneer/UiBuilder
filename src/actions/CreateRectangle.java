package actions;

import main.Document.UndoActionView;
import main.Rectangle;

public class CreateRectangle implements UndoAction {

	private Rectangle rectangle;
	private boolean isCreating;

	public CreateRectangle(Rectangle rectangle, boolean isCreating) {
		this.rectangle = rectangle;
		this.isCreating = isCreating;
	}

	@Override
	public void doAction(UndoActionView view) {
		if (isCreating) {
			view.addRectangle(rectangle);
		} else {
			view.removeRectangle(rectangle);
		}
	}

	@Override
	public void undoAction(UndoActionView view) {
		if (isCreating) {
			view.removeRectangle(rectangle);
		} else {
			view.addRectangle(rectangle);
		}
	}
}
