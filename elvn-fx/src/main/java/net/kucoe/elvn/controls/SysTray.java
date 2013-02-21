package net.kucoe.elvn.controls;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javafx.application.Platform;
import net.kucoe.elvn.timer.Timer;

/**
 * System tray tool.
 * 
 * @author Vitaliy Basyuk
 */
public class SysTray {
    
    private static TrayIcon icon;
    
    /**
     * Updates tooltip.
     * 
     * @param tooltip
     */
    public static void updateTooltip(final String tooltip) {
        if (icon != null) {
            icon.setToolTip(tooltip);
        }
    }
    
    /**
     * Shows/hides timer in tray
     * 
     * @param show
     */
    public static void showInTray(final boolean show) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            try {
                if (show) {
                    if (icon == null) {
                        URL resource = SysTray.class.getResource("/net/kucoe/elvn/resources/icons/timer.png");
                        Image image = Toolkit.getDefaultToolkit().getImage(resource.getPath());
                        icon = new TrayIcon(image, "Elvn");
                        icon.setImageAutoSize(true);
                        ActionListener listener = new ActionListener() {
                            @Override
                            public void actionPerformed(final ActionEvent event) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (Timer.isRunning()) {
                                            Timer.show();
                                            showInTray(false);
                                        }
                                    }
                                });
                            }
                        };
                        icon.addActionListener(listener);
                        icon.displayMessage(null, "Move mouse over the icon to see time", MessageType.INFO);
                        tray.add(icon);
                    }
                } else {
                    tray.remove(icon);
                    icon = null;
                }
            } catch (Exception e) {
                System.err.println("Can't add to tray");
            }
        } else {
            System.err.println("Tray unavailable");
        }
    }
}
