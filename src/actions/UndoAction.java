package actions;

import main.Document.UndoActionView;

public interface UndoAction {

	void doAction(UndoActionView view);

	void undoAction(UndoActionView view);
}
