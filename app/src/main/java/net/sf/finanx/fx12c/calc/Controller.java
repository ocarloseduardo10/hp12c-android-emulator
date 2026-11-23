package net.sf.finanx.fx12c.calc;

/**
 * Minimal Controller stub for the Android port of finanx-12c.
 *
 * The original finanx-12c Controller (in calc/Controller.java) bound the
 * Calculator engine to the Swing MainWindow. On Android we replace that
 * Swing glue with a lightweight callback interface: the Android UI
 * registers itself as the Controller, and the engine calls
 * {@link #updateDisplay()} and {@link #quit()} when it needs the UI to
 * refresh or to exit.
 *
 * This class is intentionally side-effect-free; it only forwards to
 * optional listeners so that unit tests can run headless.
 */
public class Controller {

    private Calculator executor;
    private Runnable displayRefreshListener;
    private Runnable quitListener;

    public Controller() {
        // Wire the stub MainWindow so engine calls to
        // controller.getWindow().updateDisplay() reach our listener.
        MainWindow.STUB.setOnUpdateDisplay(new Runnable() {
            @Override
            public void run() {
                Controller.this.updateDisplay();
            }
        });
    }

    public void init() {
    }

    public void initExecutor() {
        this.executor = new Calculator();
        this.executor.setController(this);
    }

    public void setExecutor(Calculator executor) {
        this.executor = executor;
        if (executor != null) {
            executor.setController(this);
        }
    }

    public Calculator getExecutor() {
        return this.executor;
    }

    /** Returns a stub "window" that simply calls back the display-refresh listener. */
    public MainWindow getWindow() {
        return MainWindow.STUB;
    }

    /** Called by the engine when the display buffer / state has changed. */
    public void updateDisplay() {
        if (displayRefreshListener != null) {
            displayRefreshListener.run();
        }
    }

    /** Called by the engine when the calculator should power-off (ON pressed twice). */
    public void quit() {
        if (quitListener != null) {
            quitListener.run();
        }
    }

    public void setDisplayRefreshListener(Runnable listener) {
        this.displayRefreshListener = listener;
    }

    public void setQuitListener(Runnable listener) {
        this.quitListener = listener;
    }
}
