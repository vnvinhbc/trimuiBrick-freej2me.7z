package javax.microedition.lcdui;

import java.io.File;

public final class RuntimeOriginTest {
    public static void main(String[] args) throws Exception {
        File patchDir = new File(System.getProperty("freej2me.patch.dir"));
        File repoDir = patchDir.getParentFile().getParentFile();
        File expected = new File(repoDir, "Emus/JAVA/zulu17/bin/freej2me-sdl.jar").getCanonicalFile();
        File actual = new File(Form.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI()).getCanonicalFile();
        if (!expected.equals(actual)) {
            throw new AssertionError("expected installed runtime " + expected + " but loaded " + actual);
        }
        System.out.println("RuntimeOriginTest: PASS (" + actual + ")");
    }
}
