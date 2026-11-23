package net.sf.finanx.fx12c.calc;

/**
 * Stub "window" facade that the engine calls when it wants the display
 * refreshed. On the Swing finanx-12c this was a JFrame; on Android it is
 * just a forwarder that runs a Runnable registered by the Activity.
 */
public class MainWindow {

    public static final MainWindow STUB = new MainWindow();

    private Runnable onUpdate;

    public void setOnUpdateDisplay(Runnable r) {
        this.onUpdate = r;
    }

    public void updateDisplay() {
        if (onUpdate != null) {
            onUpdate.run();
        }
    }
}
