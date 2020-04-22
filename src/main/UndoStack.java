package main;

import java.util.ArrayList;
import actions.UndoAction;
import main.Document.UndoActionView;

public class UndoStack {

	private ArrayList<UndoAction> actions;
	private int cursor;
	private UndoActionView view;

	public UndoStack(UndoActionView view) {
		actions = new ArrayList<>();
		cursor = -1;
		this.view = view;
	}

	public boolean canUndo() {
		return actions.size() > 0 && cursor >= 0;
	}

	public boolean canRedo() {
		return actions.size() > 0 && cursor < actions.size() - 1;
	}

	public void undo() {
		if (!canUndo()) {
			throw new RuntimeException("No actions to undo");
		}
		var action = actions.get(cursor);
		cursor -= 1;

		// Do the action last, so that UndoStack is in a valid state while it runs.
		action.undoAction(view);
	}

	public void redo() {
		if (!canRedo()) {
			throw new RuntimeException("No actions to redo");
		}
		cursor += 1;
		actions.get(cursor).doAction(view);
	}

	public void push(UndoAction action) {
		// Truncate list to cursor position.
		while (cursor < actions.size() - 1) {
			actions.remove(actions.size() - 1);
		}

		cursor += 1;
		actions.add(action);

		// Do the action last, so that UndoStack is in a valid state while it runs.
		action.doAction(view);
	}

	/**
	 * Empty the stack WITHOUT executing any `undo` actions.
	 */
	public void clear() {
		cursor = 0;
		actions.clear();
	}
}
