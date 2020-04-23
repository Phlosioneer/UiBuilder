module main {
	requires org.eclipse.swt.win32.win32.x86_64;
	requires java.desktop;
	requires gson;

	// Gson depends on java.sql.
	requires java.sql;

	// Required for Gson's reflection framework.
	exports main;
}