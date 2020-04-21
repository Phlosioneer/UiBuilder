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
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class DocumentManager {

	private static final DocumentManager instance = new DocumentManager();

	private ArrayList<Document> files;
	private List<Document> immutableFiles;
	private Document currentFile;

	private ArrayList<FileSelectionListener> selectionListeners;

	// We can't change the selected file while iterating through selectionListeners.
	// This is used to track queued changes.
	private boolean currentFileLock;
	private Document pendingSelection;

	private DocumentManager() {
		files = new ArrayList<>();
		immutableFiles = Collections.unmodifiableList(files);
		currentFile = new Document();
		files.add(currentFile);

		selectionListeners = new ArrayList<>();
		currentFileLock = false;
	}

	public static Document getCurrentDocument() {
		return instance.currentFile;
	}

	public static void setCurrentFile(Document file) {
		instance.setCurrentFileInner(file);
	}

	public static List<Document> getDocuments() {
		return instance.immutableFiles;
	}

	// This method is complicated because we need to ensure each listener sees
	// a continuous chain of selected and deselected files. Otherwise, components will
	// not correctly remove listeners from deselected files if `setCurrentFile` is called by a listener.
	//
	// In other words: if `selected(B, C)` is recieved, then a `selected(A, B)` event
	// must have come before it.
	private void setCurrentFileInner(Document newFile) {
		// Validate the input.
		assert (newFile != null);
		assert (files.contains(newFile));

		// Check if it's a new selection.
		if (currentFile == newFile) {
			return;
		}

		// Check if we're already iterating through listeners.
		if (currentFileLock) {
			// Save this for later. Overwriting pendingSelection is allowed.
			pendingSelection = newFile;
			return;
		}

		// Lock the selection.
		currentFileLock = true;
		pendingSelection = null;

		// Notify all listeners.
		var oldFile = currentFile;
		var listeners = new ArrayList<>(selectionListeners);
		for (var listener : listeners) {
			assert (currentFile == newFile);
			listener.selected(oldFile, newFile);
		}

		// Release the lock.
		assert (currentFileLock);
		currentFileLock = false;

		// If something was selected while we were iterating, recurse with the new selection.
		// If we get stuck in a loop, this will cause a stack overflow, which is better than
		// hanging forever.
		if (pendingSelection != null) {
			setCurrentFileInner(pendingSelection);
		}
	}

	/**
	 * @return The new document. Never returns null.
	 */
	public static Document newDocument() {
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
		setCurrentFile(newDocument);

		return newDocument;
	}

	/**
	 * 
	 * @return The new document, if one was created. If <i>file</i> corresponds to an existing document,
	 *         returns null. Returns null if creation failed.
	 */
	public static Document openDocument(File file) {
		for (var document : instance.files) {
			try {
				if (Files.isSameFile(document.getFilePath(), file.toPath())) {
					// Select that file instead of opening a new copy.
					setCurrentFile(document);
					return null;
				}
			} catch (IOException e) {
				// TODO: Log a message
				continue;
			}
		}

		var gson = new Gson();
		assert (file != null);
		try (var input = new BufferedReader(new FileReader(file))) {
			Document document = gson.fromJson(input, Document.class);
			document.setFile(file);
			instance.files.add(document);
			setCurrentFile(document);
			return document;
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
		//return null;
	}

	public static void closeDocument(Document document) {
		assert (document != null);
		if (document == instance.currentFile) {
			// This would mess up our invariants in the setCurrentFile function. Instead, select a different document,
			// then call closeDocument during that selection event.
			throw new RuntimeException("Can't close the currently open document.");
		}
		instance.files.remove(document);
		if (instance.pendingSelection == document) {
			instance.pendingSelection = null;
		}
	}

	public static void saveDocument(Document document) {
		saveDocument(document, document.getFilePath());
	}

	public static void saveDocument(Document document, Path outputFile) {
		assert (document != null);
		assert (outputFile != null);

		var gson = new Gson();
		try (var output = new BufferedWriter(new FileWriter(outputFile.toFile()))) {
			gson.toJson(document, output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("IOException handler not yet written in saveDocument of DocumentManager.", e);
		}
	}

	public static FileSelectionListener addSelectionListener(FileSelectionListener listener) {
		instance.selectionListeners.add(listener);
		return listener;
	}

	public static void removeSelectionListener(FileSelectionListener listener) {
		instance.selectionListeners.remove(listener);
	}

	public static interface FileSelectionListener {
		void selected(Document oldFile, Document newFile);
	}
}
