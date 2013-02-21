package net.kucoe.elvn;

import java.io.File;
import java.io.IOException;

import jline.*;
import jline.History;
import net.kucoe.elvn.lang.EL;
import net.kucoe.elvn.lang.result.ELResult;
import net.kucoe.elvn.lang.result.SwitchListColor;
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
        ConsoleReader reader = new ConsoleReader();
        reader.addCompletor(createCompletor());
        reader.setDefaultPrompt("elvn>");
        Config config = new Config();
        reader.setHistory(createHistory(config));
        Display display = new ConsoleDisplay();
        final ConsoleTimerView timerView = new ConsoleTimerView();
        Timer.setTimerView(timerView);
        Timer.setProcess(new ThreadProcess());
        try {
            ELResult result = new SwitchListColor(ListColor.Teal.toString());
            String command = joinArgs(args);
            if (command != null) {
                result = EL.process(command);
            }
            String current = result.execute(display, config);
            display.setCurrentList(current);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equalsIgnoreCase("\\q") || line.equalsIgnoreCase("exit")) {
                    timerView.onExit();
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
        historyFile = historyFile.substring(0, historyFile.lastIndexOf('.'));
        File file = new File(historyFile);
        if (!file.exists()) {
            file.createNewFile();
        }
        return new History(file);
    }
    
    private static Completor createCompletor() {
        return new SimpleCompletor(new String[] { "/all", "/done", "/today", "/@", "/!>" });
    }
    
}
