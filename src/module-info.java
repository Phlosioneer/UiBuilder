module main {
	requires transitive org.eclipse.swt.win32.win32.x86_64;
	requires java.desktop;
	requires transitive gson;

	// Gson depends on java.sql.
	requires java.sql;

	// Required for Gson's reflection framework.
	exports main;

	// Silence warning messages.
	exports actions;
}