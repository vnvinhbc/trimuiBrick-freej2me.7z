package javax.microedition.lcdui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.recompile.mobile.PlatformGraphics;
import org.recompile.mobile.PlatformImage;

public final class VirtualKeyboardViewTest {
    private static int passed;

    public static void main(String[] args) {
        renderFits(128, 128);
        renderFits(176, 208);
        renderFits(240, 320);
        renderFits(320, 240);
        renderFits(640, 360);
        run("selected key is visually distinct", VirtualKeyboardViewTest::selectedKeyIsVisuallyDistinct);
        run("password preview never draws plaintext", VirtualKeyboardViewTest::passwordPreviewNeverDrawsPlaintext);
        System.out.println("VirtualKeyboardViewTest: PASS (" + passed + " tests)");
    }

    private static void renderFits(int width, int height) {
        run("render fits " + width + "x" + height, () -> {
            PlatformImage image = new PlatformImage(width, height);
            VirtualKeyboardModel model = new VirtualKeyboardModel("hello", 32, TextField.ANY);
            VirtualKeyboardView.render(image.getGraphics(), width, height, model, "Name");
            for (int row = 0; row < model.getRows().size(); row++) {
                for (int column = 0; column < model.getRows().get(row).size(); column++) {
                    Rectangle bounds = VirtualKeyboardView.keyBounds(width, height, model, row, column);
                    assertTrue(bounds.x >= 0);
                    assertTrue(bounds.y >= 0);
                    assertTrue(bounds.width > 0);
                    assertTrue(bounds.height > 0);
                    assertTrue(bounds.x + bounds.width <= width);
                    assertTrue(bounds.y + bounds.height <= height);
                }
            }
        });
    }

    private static void selectedKeyIsVisuallyDistinct() {
        PlatformImage image = new PlatformImage(240, 320);
        VirtualKeyboardModel model = new VirtualKeyboardModel("", 32, TextField.ANY);
        VirtualKeyboardView.render(image.getGraphics(), 240, 320, model, "Name");
        Rectangle selected = VirtualKeyboardView.keyBounds(240, 320, model, 0, 0);
        Rectangle normal = VirtualKeyboardView.keyBounds(240, 320, model, 0, 1);
        int selectedPixel = image.getCanvas().getRGB(selected.x + 2, selected.y + 2);
        int normalPixel = image.getCanvas().getRGB(normal.x + 2, normal.y + 2);
        assertTrue(selectedPixel != normalPixel);
    }

    private static void passwordPreviewNeverDrawsPlaintext() {
        PlatformImage image = new PlatformImage(240, 320);
        RecordingGraphics graphics = new RecordingGraphics(image);
        VirtualKeyboardModel model = new VirtualKeyboardModel(
                "secret", 32, TextField.ANY | TextField.PASSWORD);
        VirtualKeyboardView.render(graphics, 240, 320, model, "Password");
        String drawn = String.join("|", graphics.strings);
        assertFalse(drawn.contains("secret"));
        assertTrue(drawn.contains("******"));
    }

    private static final class RecordingGraphics extends PlatformGraphics {
        private final List<String> strings = new ArrayList<>();

        private RecordingGraphics(PlatformImage image) {
            super(image);
        }

        @Override
        public void drawString(String value, int x, int y, int anchor) {
            strings.add(value);
            super.drawString(value, x, y, anchor);
        }
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
        } catch (Throwable failure) {
            throw new AssertionError(name + " failed", failure);
        }
    }

    private static void assertTrue(boolean condition) {
        if (!condition) throw new AssertionError("expected true");
    }

    private static void assertFalse(boolean condition) {
        if (condition) throw new AssertionError("expected false");
    }
}
