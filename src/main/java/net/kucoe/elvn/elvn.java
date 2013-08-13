package net.kucoe.elvn;

import java.io.File;
import java.io.IOException;

import jline.*;
import net.kucoe.elvn.lang.EL;
import net.kucoe.elvn.lang.result.ELResult;
import net.kucoe.elvn.lang.result.SwitchListColor;
import net.kucoe.elvn.sync.ConsoleSyncStatus;
import net.kucoe.elvn.sync.Sync;
import net.kucoe.elvn.timer.*;
import net.kucoe.elvn.util.*;

/**
 * Elvn implementation
 * 
 * @author Vitaliy Basyuk
 */
public class elvn {
    
    /**
     * Main method
     * 
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        Console console = new Console();
        ConsoleReader reader = console.getReader();
        reader.addCompletor(createCompletor());
        Config config = new Config();
        Sync sync = config.getSync();
        if (sync != null) {
            sync.setStatusListener(new ConsoleSyncStatus(console));
            sync.pull();
        }
        reader.setHistory(createHistory(config));
        Display display = new ConsoleDisplay(console);
        final ConsoleTimerView timerView = new ConsoleTimerView(console);
        Timer.setTimerView(timerView);
        Timer.setProcess(new ThreadProcess());
        try {
            ELResult result = new SwitchListColor(ListColor.Today.toString());
            String command = joinArgs(args);
            if (command != null) {
                result = EL.process(command);
            }
            String current = result.execute(display, config);
            display.setCurrentList(current);
            String line;
            while ((line = console.readLine()) != null) {
                if (line.equalsIgnoreCase("\\q") || line.equalsIgnoreCase("exit")) {
                    break;
                }
                if (line.equalsIgnoreCase("\\c")) {
                    reader.clearScreen();
                } else if (line.equalsIgnoreCase("\\s")) {
                    Timer.silent();
                } else {
                    result = EL.process(line);
                    String res = result.execute(display, config);
                    if (res != null) {
                        display.setCurrentList(res);
                    }
                }
            }
            Timer.shutdown();
            timerView.onExit();
            if (sync != null) {
                sync.push();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String joinArgs(final String[] args) {
        if (args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String a : args) {
                sb.append(a);
                sb.append(" ");
            }
            return sb.toString();
        }
        return null;
    }
    
    private static History createHistory(final Config config) throws IOException {
        String historyFile = config.getHistoryFile();
        File file = new File(historyFile);
        if (!file.exists()) {
            file.createNewFile();
        }
        return new History(file);
    }
    
    private static Completor createCompletor() {
        return new SimpleCompletor(new String[] { "/all", "/done", "/today", "@", "!", "$", "#" });
    }
    
}
