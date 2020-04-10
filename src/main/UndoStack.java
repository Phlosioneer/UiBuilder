package main;

import java.util.ArrayList;

public class UndoStack {

	private ArrayList<Action> actions;
	private int cursor;

	public UndoStack() {
		actions = new ArrayList<>();
		cursor = 0;
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
		action.undoAction();
	}

	public void redo() {
		if (!canRedo()) {
			throw new RuntimeException("No actions to redo");
		}
		cursor += 1;
		actions.get(cursor).doAction();
	}

	public void push(Action action) {
		// Truncate list to cursor position.
		while (cursor < actions.size()) {
			actions.remove(actions.size() - 1);
		}

		actions.add(action);
		cursor += 1;

		// Do the action last, so that UndoStack is in a valid state while it runs.
		action.doAction();
	}

	/**
	 * Empty the stack WITHOUT executing any `undo` actions.
	 */
	public void clear() {
		cursor = 0;
		actions.clear();
	}
}
