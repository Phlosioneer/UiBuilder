package main;

import java.util.ArrayList;
import java.util.function.Consumer;
import actions.UndoAction;
import main.Document.UndoActionView;

public class UndoStack {

	private ArrayList<UndoAction> actions;
	private int cursor;
	private UndoActionView view;
	private ArrayList<Consumer<UndoAction>> undoActionListeners;

	public UndoStack(UndoActionView view) {
		actions = new ArrayList<>();
		cursor = -1;
		this.view = view;
		undoActionListeners = new ArrayList<>();
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
		notifyListeners(action);
	}

	public void redo() {
		if (!canRedo()) {
			throw new RuntimeException("No actions to redo");
		}
		cursor += 1;
		var action = actions.get(cursor);
		action.doAction(view);
		notifyListeners(action);
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
		notifyListeners(action);
	}

	/**
	 * Empty the stack WITHOUT executing any `undo` actions.
	 */
	public void clear() {
		cursor = 0;
		actions.clear();
	}

	private void notifyListeners(UndoAction action) {
		// TODO: There's no way for the listeners to tell whether we're undoing or redoing this action.
		var listeners = new ArrayList<>(undoActionListeners);
		for (var listener : listeners) {
			listener.accept(action);
		}
	}

	public Consumer<UndoAction> addListener(Consumer<UndoAction> listener) {
		undoActionListeners.add(listener);
		return listener;
	}

	public void removeListener(Consumer<UndoAction> listener) {
		undoActionListeners.remove(listener);
	}
}
