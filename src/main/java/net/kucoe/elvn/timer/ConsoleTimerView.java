package net.kucoe.elvn.timer;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.script.*;

import net.kucoe.elvn.Task;
import net.kucoe.elvn.util.Console;

/**
 * {@link TimerView} implementation.
 * 
 * @author Vitaliy Basyuk
 */
public class ConsoleTimerView implements TimerView {
    
    private Task lastTask;
    private TaskStage lastStage;
    private int lastSeconds;
    private boolean silent;
    private TrayIcon icon;
    private static String OS = System.getProperty("os.name").toLowerCase();
    
    private final Console console;
    
    /**
     * Constructs ConsoleTimerView.
     * 
     * @param console
     */
    public ConsoleTimerView(final Console console) {
        this.console = console;
    }
    
    /**
     * Clean up
     */
    public void onExit() {
        if (icon != null) {
            final SystemTray tray = SystemTray.getSystemTray();
            tray.remove(icon);
        }
    }
    
    @Override
    public void show(final Task task, final TaskStage stage, final int seconds) {
        if (task != null) {
            lastTask = task;
        }
        if (stage != null) {
            lastStage = stage;
        }
        if (seconds > 0) {
            lastSeconds = seconds;
        }
        if (lastTask == null) {
            console.write("No running tasks");
        } else {
            console.write(lastStage.toString() + ":" + lastTask.getText() + " - " + formatSecs(lastSeconds));
        }
    }
    
    @Override
    public void showSmall() {
        show(null, null, 0);
    }
    
    @Override
    public void hide() {
        lastSeconds = 0;
        lastStage = null;
        lastTask = null;
    }
    
    @Override
    public void update(final int seconds) {
        lastSeconds = seconds;
        if (!silent && lastSeconds % 60 == 0) {
            show(null, null, 0);
        }
    }
    
    private String formatSecs(final int seconds) {
        int mins = (seconds / 60);
        int secs = seconds % 60;
        String divider = secs < 10 ? ":0" : ":";
        return "" + mins + divider + secs;
    }
    
    @Override
    public void playOnTime() {
        if (lastTask == null) {
            return;
        }
        if (isMac()) {
            if (!hasNotifier()) {
                runAScript("tell app \"System Events\" to display alert \"Elvn\" message \"The stage "
                        + lastStage.toString() + " finished.\"");
            }
        } else if (isWindows()) {
            showInTray();
        }
        hide();
    }
    
    @Override
    public void playOnStart() {
        if (silent) {
            show(null, null, 0);
        }
    }
    
    @Override
    public void silent() {
        silent = !silent;
        console.write("Silent mode " + (silent ? "activated" : "deactivated"));
    }
    
    // https://github.com/alloy/terminal-notifier/downloads
    private boolean hasNotifier() {
        try {
            ProcessBuilder pb =
                    new ProcessBuilder("/Applications/terminal-notifier.app/Contents/MacOS/terminal-notifier",
                            "-message", "The stage " + lastStage.toString() + " finished.", "-title", "Elvn",
                            "-activate", "com.apple.Terminal");
            pb.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private void runAScript(final String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("AppleScript");
        try {
            engine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
    
    private void showInTray() {
        if (SystemTray.isSupported()) {
            final SystemTray tray = SystemTray.getSystemTray();
            try {
                Image image = ImageIO.read(getClass().getResourceAsStream("/net/kucoe/elvn/resources/icons/timer.png"));
                if (icon != null) {
                    tray.remove(icon);
                }
                icon = new TrayIcon(image, "Elvn");
                icon.setImageAutoSize(true);
                icon.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseReleased(final MouseEvent e) {
                        // empty
                    }
                    
                    @Override
                    public void mousePressed(final MouseEvent e) {
                        // empty
                    }
                    
                    @Override
                    public void mouseExited(final MouseEvent e) {
                        // empty
                    }
                    
                    @Override
                    public void mouseEntered(final MouseEvent e) {
                        // empty
                    }
                    
                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        tray.remove(icon);
                    }
                });
                tray.add(icon);
                icon.displayMessage("Elvn", "The stage " + lastStage.toString() + " finished.", MessageType.NONE);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Can't add to tray");
            }
        } else {
            System.err.println("Tray unavailable");
        }
    }
    
    private static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }
    
    private static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }
    
    private static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }
}
