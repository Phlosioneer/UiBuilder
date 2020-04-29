package main;

import java.util.ArrayList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import actions.RenameRectangleAction;
import actions.ResizeRectangleAction;

public class ObjectInfoTab extends Composite {
	private Text textName;
	private Text textX;
	private Text textY;
	private Text textWidth;
	private Text textHeight;

	private Document currentlyModifiedDocument;
	private Rectangle currentlyModifiedRectangle;
	private Text focusedTextField;

	private ArrayList<Text> textFields;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	////////////////////////////////////
	// Begin generated code
	public ObjectInfoTab(Composite parent) {
		super(parent, SWT.None);
		setLayout(new FormLayout());

		textName = new Text(this, SWT.BORDER);
		textName.addFocusListener(FocusListener.focusLostAdapter(this::fieldLostFocus));
		textName.addFocusListener(FocusListener.focusGainedAdapter(this::fieldGainedFocus));
		FormData fd_textName = new FormData();
		fd_textName.top = new FormAttachment(0, 10);
		fd_textName.left = new FormAttachment(0, 71);
		fd_textName.right = new FormAttachment(100, -10);
		textName.setLayoutData(fd_textName);

		textX = new Text(this, SWT.BORDER);
		textX.addVerifyListener(this::verifyNumber);
		textX.addFocusListener(FocusListener.focusLostAdapter(this::fieldLostFocus));
		textX.addFocusListener(FocusListener.focusGainedAdapter(this::fieldGainedFocus));
		FormData fd_textX = new FormData();
		fd_textX.top = new FormAttachment(textName, 6);
		fd_textX.left = new FormAttachment(textName, 0, SWT.LEFT);
		fd_textX.right = new FormAttachment(textName, 0, SWT.RIGHT);
		textX.setLayoutData(fd_textX);

		textY = new Text(this, SWT.BORDER);
		textY.addVerifyListener(this::verifyNumber);
		textY.addFocusListener(FocusListener.focusLostAdapter(this::fieldLostFocus));
		textY.addFocusListener(FocusListener.focusGainedAdapter(this::fieldGainedFocus));
		FormData fd_textY = new FormData();
		fd_textY.top = new FormAttachment(textX, 6);
		fd_textY.left = new FormAttachment(textX, 0, SWT.LEFT);
		fd_textY.right = new FormAttachment(textX, 0, SWT.RIGHT);
		textY.setLayoutData(fd_textY);

		textWidth = new Text(this, SWT.BORDER);
		textWidth.addVerifyListener(this::verifyNumber);
		textWidth.addFocusListener(FocusListener.focusLostAdapter(this::fieldLostFocus));
		textWidth.addFocusListener(FocusListener.focusGainedAdapter(this::fieldGainedFocus));
		FormData fd_textWidth = new FormData();
		fd_textWidth.top = new FormAttachment(textY, 6);
		fd_textWidth.left = new FormAttachment(textY, 0, SWT.LEFT);
		fd_textWidth.right = new FormAttachment(textY, 0, SWT.RIGHT);
		textWidth.setLayoutData(fd_textWidth);

		textHeight = new Text(this, SWT.BORDER);
		textHeight.addVerifyListener(this::verifyNumber);
		textHeight.addFocusListener(FocusListener.focusLostAdapter(this::fieldLostFocus));
		textHeight.addFocusListener(FocusListener.focusGainedAdapter(this::fieldGainedFocus));
		FormData fd_textHeight = new FormData();
		fd_textHeight.top = new FormAttachment(textWidth, 6);
		fd_textHeight.left = new FormAttachment(textWidth, 0, SWT.LEFT);
		fd_textHeight.right = new FormAttachment(textWidth, 0, SWT.RIGHT);
		textHeight.setLayoutData(fd_textHeight);

		Label lblName = new Label(this, SWT.NONE);
		FormData fd_lblName = new FormData();
		fd_lblName.top = new FormAttachment(textName, 3, SWT.TOP);
		fd_lblName.left = new FormAttachment(0, 10);
		lblName.setLayoutData(fd_lblName);
		lblName.setText("Name:");

		Label lblX = new Label(this, SWT.NONE);
		FormData fd_lblX = new FormData();
		fd_lblX.top = new FormAttachment(textX, 3, SWT.TOP);
		fd_lblX.left = new FormAttachment(lblName, 0, SWT.LEFT);
		lblX.setLayoutData(fd_lblX);
		lblX.setText("X:");

		Label lblY = new Label(this, SWT.NONE);
		FormData fd_lblY = new FormData();
		fd_lblY.top = new FormAttachment(textY, 3, SWT.TOP);
		fd_lblY.left = new FormAttachment(lblName, 0, SWT.LEFT);
		lblY.setLayoutData(fd_lblY);
		lblY.setText("Y:");

		Label lblWidth = new Label(this, SWT.NONE);
		FormData fd_lblWidth = new FormData();
		fd_lblWidth.top = new FormAttachment(textWidth, 3, SWT.TOP);
		fd_lblWidth.left = new FormAttachment(lblName, 0, SWT.LEFT);
		lblWidth.setLayoutData(fd_lblWidth);
		lblWidth.setText("Width:");

		Label lblHeight = new Label(this, SWT.NONE);
		FormData fd_lblHeight = new FormData();
		fd_lblHeight.top = new FormAttachment(textHeight, 3, SWT.TOP);
		fd_lblHeight.left = new FormAttachment(lblName, 0, SWT.LEFT);
		lblHeight.setLayoutData(fd_lblHeight);
		lblHeight.setText("Height:");

		/////////////////////////////////////////////////////
		// End generated code

		textFields = new ArrayList<>(5);
		textFields.add(textName);
		textFields.add(textX);
		textFields.add(textY);
		textFields.add(textWidth);
		textFields.add(textHeight);

		for (var field : textFields) {
			field.setEnabled(false);
		}

		var selectionListener = DocumentManager.addCurrentDocumentSelectionListener(this::selectedRectChanged);
		var documentChangeListener = DocumentManager.addSelectionListener(newDocument->selectedRectChanged(newDocument.getSelectedRectangle()));

		// Clean up listeners.
		addDisposeListener(e-> {
			DocumentManager.removeCurrentDocumentListener(selectionListener);
			DocumentManager.removeSelectionListener(documentChangeListener);
		});
	}

	private void verifyNumber(VerifyEvent event) {
		double value;
		var textField = (Text) event.widget;
		var newText = textField.getText().substring(0, event.start);
		newText += event.text;
		newText += textField.getText().substring(event.end);

		try {
			value = Double.parseDouble(newText);
		} catch (NumberFormatException exception) {
			// Try adding a 0 to the end. This will allow inputs like "-" or "."
			try {
				Double.parseDouble(newText + '0');
				// Successfully changed. We can't use the value to update the temporary
				// sizing, though.
				return;
			} catch (NumberFormatException exception2) {
				// Outside will reject.
			}
			// Reject the change.
			event.text = "";
			return;
		}

		// Successful change. Set the value as temporary until the user finishes editing.
		var document = DocumentManager.getCurrentDocument();
		var rect = document.getSelectedRectangle();
		var x = rect.x;
		var y = rect.y;
		var width = rect.width;
		var height = rect.height;
		if (textField == textX) {
			x = value;
		} else if (textField == textY) {
			y = value;
		} else if (textField == textWidth) {
			width = value;
		} else {
			assert (textField == textHeight);
			height = value;
		}

		document.setTempSize(x, y, width, height);
	}

	private void fieldLostFocus(FocusEvent event) {
		commitChanges((Text) event.widget);
		currentlyModifiedDocument = null;
		currentlyModifiedRectangle = null;
		populate();
	}

	private void fieldGainedFocus(FocusEvent event) {
		currentlyModifiedDocument = DocumentManager.getCurrentDocument();
		currentlyModifiedRectangle = currentlyModifiedDocument.getSelectedRectangle();
		assert (currentlyModifiedRectangle != null);
		focusedTextField = (Text) event.widget;
	}

	private void selectedRectChanged(Rectangle newSelection) {
		// Remove focus from any text fields, if they're focused.
		for (var field : textFields) {
			if (field.isFocusControl()) {
				forceFocus();
				break;
			}
		}
		populate();
	}

	private void populate() {
		var rect = DocumentManager.getCurrentDocument().getSelectedRectangle();
		if (rect == null) {
			for (var field : textFields) {
				field.setText("");
				field.setEnabled(false);
			}
		} else {
			for (var field : textFields) {
				field.setEnabled(true);
			}
			textName.setText(rect.name);
			textX.setText(Double.toString(rect.x));
			textY.setText(Double.toString(rect.y));
			textWidth.setText(Double.toString(rect.width));
			textHeight.setText(Double.toString(rect.height));
		}
	}

	private void commitChanges(Text changedField) {
		assert (currentlyModifiedDocument != null);
		assert (currentlyModifiedRectangle != null);
		currentlyModifiedDocument.cancelTempSize();

		if (changedField == textName) {
			currentlyModifiedDocument.getUndoStack().push(new RenameRectangleAction(currentlyModifiedRectangle, changedField.getText()));
			return;
		}

		double newValue;
		try {
			newValue = Double.parseDouble(changedField.getText());
		} catch (NumberFormatException exception) {
			// Invalid number, ignore the change.
			return;
		}

		double x = currentlyModifiedRectangle.x;
		double y = currentlyModifiedRectangle.y;
		double width = currentlyModifiedRectangle.width;
		double height = currentlyModifiedRectangle.height;

		if (changedField == textX) {
			x = newValue;
		} else if (changedField == textY) {
			y = newValue;
		} else if (changedField == textWidth) {
			x = Math.min(x, x + newValue);
			width = Math.abs(newValue);
		} else {
			assert (changedField == textHeight);
			y = Math.min(y, y + newValue);
			height = Math.abs(newValue);
		}

		currentlyModifiedDocument.getUndoStack().push(new ResizeRectangleAction(currentlyModifiedRectangle, x, y, width, height));
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
