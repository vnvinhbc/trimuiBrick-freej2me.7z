package javax.microedition.lcdui;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public final class PackagingTest {
    public static void main(String[] args) throws Exception {
        File patchDir = new File(requiredProperty("freej2me.patch.dir"));
        File repoDir = patchDir.getParentFile().getParentFile();
        File active = new File(repoDir, "Emus/JAVA/zulu17/bin/freej2me-sdl.jar");
        File backup = new File(active.getPath() + ".pre-virtual-keyboard.bak");
        File baseline = backup.isFile() ? backup : active;
        File patched = new File(patchDir, "build/freej2me-sdl.jar");
        File smoke = new File(patchDir, "build/virtual-keyboard-smoke.jar");

        assertTrue(patched.isFile(), "patched runtime JAR is missing");
        assertTrue(smoke.isFile(), "smoke MIDlet JAR is missing");
        assertMainClass(patched, "org.recompile.freej2me.Anbu");
        assertOriginalEntriesPreserved(baseline, patched);
        assertEntry(patched, "javax/microedition/lcdui/Form.class");
        assertEntry(patched, "javax/microedition/lcdui/TextBox.class");
        assertEntry(patched, "javax/microedition/lcdui/VirtualKeyboardModel.class");
        assertEntry(patched, "javax/microedition/lcdui/VirtualKeyboardView.class");
        assertEntry(smoke, "javax/microedition/lcdui/VirtualKeyboardSmokeMidlet.class");
        assertMidletManifest(smoke);
        System.out.println("PackagingTest: PASS (manifest, entries, smoke MIDlet)");
    }

    private static void assertMainClass(File file, String expected) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            String actual = jar.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            assertEquals(expected, actual, "Main-Class changed");
        }
    }

    private static void assertOriginalEntriesPreserved(File baseline, File patched) throws IOException {
        Set<String> patchedEntries = entries(patched);
        for (String entry : entries(baseline)) {
            assertTrue(patchedEntries.contains(entry), "missing original entry: " + entry);
        }
    }

    private static Set<String> entries(File file) throws IOException {
        Set<String> names = new HashSet<>();
        try (JarFile jar = new JarFile(file)) {
            Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) names.add(entries.nextElement().getName());
        }
        return names;
    }

    private static void assertEntry(File file, String name) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            assertTrue(jar.getEntry(name) != null, "missing entry " + name + " in " + file);
        }
    }

    private static void assertMidletManifest(File file) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            Attributes attributes = jar.getManifest().getMainAttributes();
            assertEquals("Virtual Keyboard Smoke", attributes.getValue("MIDlet-Name"),
                    "MIDlet-Name missing");
            assertTrue(attributes.getValue("MIDlet-1").contains("VirtualKeyboardSmokeMidlet"),
                    "MIDlet-1 missing class");
        }
    }

    private static String requiredProperty(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isEmpty()) throw new AssertionError("missing property " + name);
        return value;
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
