package actions;

import main.Document.UndoActionView;
import main.Rectangle;

public class RenameRectangleAction implements UndoAction {

	Rectangle rect;
	String newName;
	String oldName;

	public RenameRectangleAction(Rectangle rect, String newName) {
		this.rect = rect;
		this.newName = newName;
		oldName = rect.name;
	}

	@Override
	public void doAction(UndoActionView view) {
		assert (rect.name.equals(oldName));
		rect.name = newName;
	}

	@Override
	public void undoAction(UndoActionView view) {
		assert (rect.name.equals(newName));
		rect.name = oldName;
	}
}
