package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import actions.UndoAction;
import main.Document.TemporaryResizeListener;

public class DocumentManager {

	private static final DocumentManager instance = new DocumentManager();

	private ArrayList<Document> files;
	private List<Document> immutableFiles;
	private Document currentFile;

	private ArrayList<Consumer<Document>> selectionListeners;
	private ArrayList<Consumer<Document>> creationListeners;
	private ArrayList<Consumer<Document>> closeListeners;
	private ArrayList<Consumer<Document>> saveListeners;
	private Predicate<Document> shouldCloseDocument;

	// Listeners that detach and re-attach themselves so that they're only listening
	// to the current document.
	private ArrayList<CurrentDocumentListener> currentDocListeners;

	// We can't change the selected file while iterating through selectionListeners.
	private boolean currentFileLock;

	private DocumentManager() {
		files = new ArrayList<>();
		immutableFiles = Collections.unmodifiableList(files);
		currentFile = new Document();
		files.add(currentFile);
		selectionListeners = new ArrayList<>();
		creationListeners = new ArrayList<>();
		closeListeners = new ArrayList<>();
		saveListeners = new ArrayList<>();
		shouldCloseDocument = null;

		currentDocListeners = new ArrayList<>();

		currentFileLock = false;
	}

	// INVARIANT: This will never return null.
	// INVARIANT: The returned document will always be contained in the getDocuments() array.
	public static Document getCurrentDocument() {
		return instance.currentFile;
	}

	public static void setCurrentDocument(Document newDocument) {
		// Validate the input.
		assert (newDocument != null);
		assert (instance.files.contains(newDocument));

		// Check if it's a new selection.
		if (instance.currentFile == newDocument) {
			return;
		}

		instance.currentFile = newDocument;

		instance.notifySelectionListeners();
	}

	public static List<Document> getDocuments() {
		return instance.immutableFiles;
	}

	/**
	 * @return The new document. Never returns null. Selects the newly created document.
	 */
	public static void newDocument() {
		var newDocument = new Document();
		var openDocumentNames = new ArrayList<String>(instance.files.size());
		for (var file : instance.files) {
			openDocumentNames.add(file.getFileName());
		}

		var base = "Untitled";
		var increment = 0;
		var current = base;
		while (openDocumentNames.contains(current)) {
			increment += 1;
			current = base + increment;
		}

		newDocument.setFileName(current);

		instance.files.add(newDocument);
		if (instance.currentFile == null) {
			// The only way this branch is taken is if we came from closeAllDocuments, which checks the currentFileLock
			// before running.
			assert (!instance.currentFileLock);

			// We need to select the new file before calling any listeners, to ensure we're in a valid state.
			instance.currentFile = newDocument;
			instance.notifyCreationListeners(newDocument);
			instance.notifySelectionListeners();
		} else {
			instance.notifyCreationListeners(newDocument);
			setCurrentDocument(newDocument);
		}
	}

	public static void openDocument(File file) {
		for (var document : instance.files) {
			try {
				if (document.getFilePath() != null && Files.isSameFile(document.getFilePath(), file.toPath())) {
					// Select that file instead of opening a new copy.
					setCurrentDocument(document);
				}
			} catch (IOException e) {
				// TODO: Log a message
				continue;
			}
		}

		var gson = new Gson();
		assert (file != null);
		Document newDocument = null;
		try (var input = new BufferedReader(new FileReader(file))) {
			newDocument = gson.fromJson(input, Document.class);
		} catch (JsonIOException e1) {
			// TODO Auto-generated catch block
			throw new RuntimeException("JsonIOException handler not yet written in onOpen of UiBuilder.", e1);
		} catch (JsonSyntaxException e1) {
			// TODO Auto-generated catch block
			throw new RuntimeException("JsonSyntaxException handler not yet written in onOpen of UiBuilder.", e1);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			throw new RuntimeException("FileNotFoundException handler not yet written in onOpen of UiBuilder.", e1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			throw new RuntimeException("IOException handler not yet written in onOpen of UiBuilder.", e2);
		}

		newDocument.setFile(file);
		instance.files.add(newDocument);
		instance.notifyCreationListeners(newDocument);
		setCurrentDocument(newDocument);
	}

	/**
	 * @return True if the document was closed, false otherwise.
	 */
	public static boolean closeDocument(Document document) {
		assert (document != null);
		if (instance.shouldCloseDocument != null && !instance.shouldCloseDocument.test(document)) {
			return false;
		}
		if (instance.currentFile == document) {
			if (instance.currentFileLock) {
				throw new RuntimeException("Can't close the currently selected document within a selection listener.");
			}
			if (instance.files.size() == 1) {
				// Create a blank document first.
				// This will also switch the selection to the new document.
				newDocument();
			}
		}
		instance.files.remove(document);
		instance.notifyCloseListeners(document);
		return true;
	}

	/**
	 * Closes all documents, then creates a blank document.
	 * 
	 * @return True if all documents were closed successfully.
	 */
	public static boolean closeAllDocuments() {
		if (instance.currentFileLock) {
			throw new RuntimeException("Can't close all documents within a selection listener.");
		}
		while (instance.files.size() > 0) {
			var document = instance.files.get(0);
			if (instance.shouldCloseDocument != null && !instance.shouldCloseDocument.test(document)) {
				// Abort.
				return false;
			}
			// Remove the file.
			instance.files.remove(0);

			// If it was selected, select something else.
			if (document == instance.currentFile && instance.files.size() > 0) {
				setCurrentDocument(instance.files.get(0));
			}
		}
		// Nullify the current file, to tell newDocument that it should select it before calling notifyCreationListeners
		instance.currentFile = null;

		// Make a blank file. This also selects it correctly.
		newDocument();

		return true;
	}

	public static void saveDocument(Document document) {
		saveDocument(document, document.getFilePath());
	}

	private static void saveDocument(Document document, Path outputFile) {
		assert (document != null);
		assert (outputFile != null);

		var gson = new Gson();
		try (var output = new BufferedWriter(new FileWriter(outputFile.toFile()))) {
			gson.toJson(document, output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("IOException handler not yet written in saveDocument of DocumentManager.", e);
			// return;
		}

		// Saved successfully.
		instance.notifySaveListeners(document);
	}

	public static Consumer<Document> addSelectionListener(Consumer<Document> listener) {
		instance.selectionListeners.add(listener);
		return listener;
	}

	public static void removeSelectionListener(Consumer<Document> listener) {
		instance.selectionListeners.remove(listener);
	}

	private void notifySelectionListeners() {
		// Ensure that we're not already iterating.
		if (currentFileLock) {
			throw new RuntimeException();
		}
		currentFileLock = true;

		// Notify all listeners.
		var listeners = new ArrayList<>(selectionListeners);
		for (var listener : listeners) {
			listener.accept(instance.currentFile);
		}

		// Release the lock.
		currentFileLock = false;
	}

	public static Consumer<Document> addCreationListener(Consumer<Document> listener) {
		instance.creationListeners.add(listener);
		return listener;
	}

	public static void removeCreationListener(Consumer<Document> listener) {
		instance.creationListeners.remove(listener);
	}

	private void notifyCreationListeners(Document document) {
		var listeners = new ArrayList<>(creationListeners);
		for (var listener : listeners) {
			listener.accept(document);
		}
	}

	public static Consumer<Document> addCloseListener(Consumer<Document> listener) {
		instance.closeListeners.add(listener);
		return listener;
	}

	public static void removeCloseListener(Consumer<Document> listener) {
		instance.closeListeners.remove(listener);
	}

	private void notifyCloseListeners(Document document) {
		var listeners = new ArrayList<>(closeListeners);
		for (var listener : listeners) {
			listener.accept(document);
		}
	}

	public static Consumer<Document> addSaveListener(Consumer<Document> listener) {
		instance.saveListeners.add(listener);
		return listener;
	}

	public static void removeSaveListener(Consumer<Document> listener) {
		instance.saveListeners.remove(listener);
	}

	private void notifySaveListeners(Document document) {
		var listeners = new ArrayList<>(saveListeners);
		for (var listener : listeners) {
			listener.accept(document);
		}
	}

	/**
	 * Return false to cancel the close operation, and true to allow it.
	 */
	public static void setShouldCloseDocument(Predicate<Document> predicate) {
		instance.shouldCloseDocument = predicate;
	}

	public static CurrentDocumentListener addCurrentDocumentSelectionListener(Consumer<Rectangle> listener) {
		var newListener = CurrentDocumentListener.selectionListener(listener);
		instance.currentDocListeners.add(newListener);
		return newListener;
	}

	public static CurrentDocumentListener addCurrentDocumentUndoActionListener(Consumer<UndoAction> listener) {
		var newListener = CurrentDocumentListener.undoListener(listener);
		instance.currentDocListeners.add(newListener);
		return newListener;
	}

	public static CurrentDocumentListener addCurrentDocumentTemporaryResizeListener(TemporaryResizeListener listener) {
		var newListener = CurrentDocumentListener.resizeListener(listener);
		instance.currentDocListeners.add(newListener);
		return newListener;
	}

	public static void removeCurrentDocumentListener(CurrentDocumentListener listener) {
		instance.currentDocListeners.remove(listener);
		listener.dispose();
	}

	public static class CurrentDocumentListener {
		private Document attachedDocument;
		private Object listener;
		boolean isSelectionListener;
		boolean isUndoListener;
		boolean isResizeListener;
		Consumer<Document> documentListener;

		private CurrentDocumentListener(Document document, Object listener) {
			isSelectionListener = false;
			isUndoListener = false;
			isResizeListener = false;
			attachedDocument = document;
			this.listener = listener;

			documentListener = DocumentManager.addSelectionListener(newDocument-> {
				detach();
				attachedDocument = newDocument;
				attach();
			});
		}

		protected static CurrentDocumentListener selectionListener(Consumer<Rectangle> listener) {
			var ret = new CurrentDocumentListener(getCurrentDocument(), listener);
			ret.isSelectionListener = true;
			ret.attach();
			return ret;
		}

		protected static CurrentDocumentListener undoListener(Consumer<UndoAction> listener) {
			var ret = new CurrentDocumentListener(getCurrentDocument(), listener);
			ret.isUndoListener = true;
			ret.attach();
			return ret;
		}

		protected static CurrentDocumentListener resizeListener(TemporaryResizeListener listener) {
			var ret = new CurrentDocumentListener(getCurrentDocument(), listener);
			ret.isResizeListener = true;
			ret.attach();
			return ret;
		}

		@SuppressWarnings("unchecked")
		private void attach() {
			if (isSelectionListener) {
				attachedDocument.addSelectionListener((Consumer<Rectangle>) listener);
			}
			if (isResizeListener) {
				attachedDocument.addTemporaryResizeListener((TemporaryResizeListener) listener);
			}
			if (isUndoListener) {
				attachedDocument.getUndoStack().addListener((Consumer<UndoAction>) listener);
			}
		}

		@SuppressWarnings("unchecked")
		private void detach() {
			if (isSelectionListener) {
				attachedDocument.removeSelectionListener((Consumer<Rectangle>) listener);
			}
			if (isResizeListener) {
				attachedDocument.removeTemporaryResizeListener((TemporaryResizeListener) listener);
			}
			if (isUndoListener) {
				attachedDocument.getUndoStack().removeListener((Consumer<UndoAction>) listener);
			}
		}

		protected void dispose() {
			detach();
			DocumentManager.removeSelectionListener(documentListener);
		}
	}
}
