package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import actions.CreateRectangle;

// All coordinates are in ratios. (0 <= coord <= 1)
@JsonAdapter(Document.DocumentTypeAdapter.class)
public class Document {

	private ArrayList<Rectangle> rectangles;

	private List<Rectangle> rectanglesView;
	private int currentSelection;
	private Path filepath;
	private String filename;
	private UndoStack undoStack;
	private boolean hasUnsavedChanges;

	// TODO: Should the temp resize dimensions be in Document?
	private double tempX;
	private double tempY;
	private double tempWidth;
	private double tempHeight;
	private boolean isInResizeMode;
	private ArrayList<TemporaryResizeListener> tempResizeListeners;
	private ArrayList<Consumer<Rectangle>> selectionListeners;
	private int selectionListenersSemaphore;

	private Document(ArrayList<Rectangle> rectangles) {
		this.rectangles = rectangles;
		rectanglesView = Collections.unmodifiableList(rectangles);
		currentSelection = -1;
		filepath = null;
		filename = "Untitled";
		undoStack = new UndoStack(new UndoActionView(this));
		hasUnsavedChanges = false;

		tempX = 0;
		tempY = 0;
		tempWidth = 0;
		tempHeight = 0;
		isInResizeMode = false;
		tempResizeListeners = new ArrayList<>();

		selectionListeners = new ArrayList<>();
		selectionListenersSemaphore = 0;

		undoStack.addListener(action->hasUnsavedChanges = true);
	}

	/**
	 * Creates a new, untitled document.
	 */
	public Document() {
		this(new ArrayList<>());
	}

	public boolean hasUnsavedChanges() {
		return hasUnsavedChanges;
	}

	/**
	 * Call this when a document is saved.
	 */
	public void clearUnsavedChangesFlag() {
		hasUnsavedChanges = false;
	}

	public void setFile(File file) {
		setFile(file.toPath());
	}

	public void setFile(Path path) {
		filepath = path;
		filename = path.getFileName().toString();
	}

	public void setFileName(String name) {
		assert (filepath == null);
		filename = name;
	}

	public String getFileName() {
		return filename;
	}

	public Path getFilePath() {
		return filepath;
	}

	public List<Rectangle> getRectangles() {
		return rectanglesView;
	}

	public void addRectangle(Rectangle rect) {
		addRectangle(rect, true);
	}

	private void addRectangle(Rectangle rect, boolean makeUndoAction) {
		assert (rect != null);
		assert (!rectangles.contains(rect));
		if (makeUndoAction) {
			var action = new CreateRectangle(rect, true);
			undoStack.push(action);
			notifySelectionListeners();
		} else {
			rectangles.add(rect);
			currentSelection = rectangles.size() - 1;
		}
	}

	public void removeRectangle(Rectangle rect) {
		removeRectangle(rect, true);
	}

	private void removeRectangle(Rectangle rect, boolean makeUndoAction) {
		assert (rect != null);
		assert (rectangles.contains(rect));
		if (makeUndoAction) {
			var action = new CreateRectangle(rect, false);
			undoStack.push(action);
		} else {
			var index = rectangles.indexOf(rect);
			if (currentSelection == index) {
				currentSelection = -1;
				notifySelectionListeners();
			}
			rectangles.remove(rect);
		}
	}

	public void setSelectedRectangle(int index) {
		assert (index >= 0);
		currentSelection = index;
		notifySelectionListeners();
	}

	public void setSelectedRectangle(Rectangle rectangle) {
		if (rectangle == null) {
			currentSelection = -1;
		} else {
			currentSelection = rectangles.indexOf(rectangle);
			assert (currentSelection != -1);
		}
		notifySelectionListeners();
	}

	public Rectangle getSelectedRectangle() {
		if (currentSelection == -1) {
			return null;
		} else {
			return rectangles.get(currentSelection);
		}
	}

	public int getSelectedIndex() {
		return currentSelection;
	}

	public void setTempSize(double x, double y, double width, double height) {
		tempX = x;
		tempY = y;
		tempWidth = width;
		tempHeight = height;
		var listeners = new ArrayList<>(tempResizeListeners);
		if (!isInResizeMode) {
			assert (currentSelection != -1);
			isInResizeMode = true;

			for (var listener : listeners) {
				listener.resizeStarted();
			}
		}

		for (var listener : listeners) {
			listener.resize(x, y, width, height);
		}
	}

	public void cancelTempSize() {
		tempX = 0;
		tempY = 0;
		tempWidth = 0;
		tempHeight = 0;
		isInResizeMode = false;

		var listeners = new ArrayList<>(tempResizeListeners);
		for (var listener : listeners) {
			listener.resizeCancelled();
		}
	}

	public void setPosition(Rectangle original, int newIndex) {
		assert (newIndex >= 0 && newIndex < rectangles.size());
		var oldIndex = rectangles.indexOf(original);
		assert (oldIndex != -1);
		if (oldIndex == newIndex) {
			return;
		}

		var oldSelection = getSelectedRectangle();

		rectangles.remove(oldIndex);
		rectangles.add(newIndex, original);

		setSelectedRectangle(oldSelection);
	}

	public double getTempX() {
		return tempX;
	}

	public double getTempY() {
		return tempY;
	}

	public double getTempWidth() {
		return tempWidth;
	}

	public double getTempHeight() {
		return tempHeight;
	}

	public UndoStack getUndoStack() {
		return undoStack;
	}

	private void notifySelectionListeners() {
		var listeners = new ArrayList<>(selectionListeners);
		Rectangle selected = null;
		if (currentSelection != -1) {
			selected = rectangles.get(currentSelection);
		}

		// This cancels any other notifySelectionListeners() currently running.
		selectionListenersSemaphore += 1;
		var semaphoreId = selectionListenersSemaphore;

		for (var listener : listeners) {
			if (selectionListenersSemaphore != semaphoreId) {
				break;
			}
			listener.accept(selected);
		}
	}

	/**
	 * 
	 * @return The listener, for easy usage with lambdas.
	 */
	public Consumer<Rectangle> addSelectionListener(Consumer<Rectangle> listener) {
		selectionListeners.add(listener);
		return listener;
	}

	public void removeSelectionListener(Consumer<Rectangle> listener) {
		selectionListeners.remove(listener);
	}

	/**
	 * 
	 * @return The listener, for easy usage with lambdas.
	 */
	public TemporaryResizeListener addTemporaryResizeListener(TemporaryResizeListener listener) {
		tempResizeListeners.add(listener);
		return listener;
	}

	public void removeTemporaryResizeListener(TemporaryResizeListener listener) {
		tempResizeListeners.remove(listener);
	}

	public boolean hasTempResize() {
		return isInResizeMode;
	}

	public static interface TemporaryResizeListener {
		void resizeStarted();

		void resize(double x, double y, double width, double height);

		void resizeCancelled();
	}

	public static class DocumentTypeAdapter extends TypeAdapter<Document> {
		@Override
		public void write(JsonWriter out, Document value) throws IOException {
			out.beginObject();
			out.name("rectangles");
			var gson = new Gson();
			var token = new TypeToken<ArrayList<Rectangle>>() {};
			gson.toJson(value.rectangles, token.getType(), out);
			out.endObject();
		}

		@Override
		public Document read(JsonReader in) throws IOException {
			in.beginObject();
			ArrayList<Rectangle> rectangles = null;
			var gson = new Gson();
			var token = new TypeToken<ArrayList<Rectangle>>() {};
			while (in.hasNext()) {
				String name = in.nextName();
				if (name.equalsIgnoreCase("rectangles")) {
					rectangles = gson.fromJson(in, token.getType());
				} else {
					in.skipValue();
				}
			}
			in.endObject();
			assert (rectangles != null);
			return new Document(rectangles);
		}
	}

	/**
	 * This class provides an interface with a Document that doesn't trigger undo actions.
	 * It's provided to UndoActions to avoid recursive undo action creation.
	 */
	public static class UndoActionView {
		private Document parent;

		public UndoActionView(Document parent) {
			this.parent = parent;
		}

		public Document getParent() {
			return parent;
		}

		public void addRectangle(Rectangle rectangle) {
			parent.addRectangle(rectangle, false);
		}

		public void removeRectangle(Rectangle rectangle) {
			parent.removeRectangle(rectangle, false);
		}
	}
}
