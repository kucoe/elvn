package net.kucoe.elvn.lang;

import java.util.*;
import java.util.regex.Pattern;

import net.kucoe.elvn.ListColor;
import net.kucoe.elvn.lang.result.*;

/**
 * Enumerates EL commands.
 * 
 * @author Vitaliy Basyuk
 */
public enum ELCommand {
    
    /**
     * End command.
     */
    End("."),
    
    /**
     * String command.
     */
    String("") {
        @Override
        protected ELCommand doNext() {
            String[] split = split(processing, ':');
            String text = split[0];
            String list = null;
            int length = split.length;
            if (length > 1) {
                list = split[0];
                text = split[1];
            }
            if (result == null) {
                result = doEditTask(result, list, text);
            } else if (result instanceof SearchTask) {
                String query = parseTill("=");
                result = new SearchTask(null, null, query);
                String string = parseNext(1, "=");
                ELCommand next = command(string);
                if (next != null) {
                    return next;
                }
            } else if (result instanceof LocateTask) {
                result = new TaskCommand(((LocateTask) result).position, processing);
            } else if (result instanceof TaskGroup) {
                result = new GroupTaskCommand(processing, ((TaskGroup) result).positions);
            } else if (result instanceof EditTask) {
                result = doEditTask(result, list, text);
            } else if (result instanceof SwitchSync) {
                result = new SyncCommand(processing);
            } else if (result instanceof SwitchTimer) {
                result = new TimerCommand(processing);
            } else if (result instanceof Switch) {
                ListColor color = ListColor.color(processing);
                if (color == null) {
                    result = new SwitchListLabel(processing);
                } else {
                    result = new SwitchListColor(processing);
                }
            }
            return super.doNext();
        }
        
        protected TaskResult doEditTask(final ELResult result, final String list, final String text) {
            if (result instanceof EditTask) {
                TaskResult mainResult = ((EditTask) result).mainResult;
                return doEditTask(mainResult, list, text);
            } else if (result instanceof SearchTask) {
                return new SearchTask(list, text, ((SearchTask) result).query);
            } else if (result instanceof LocateTask) {
                return new LocateTask(list, text, ((LocateTask) result).position);
            } else {
                return new TaskResult(list, text);
            }
        }
    },
    
    /**
     * Group command
     */
    Group(",") {
        @Override
        protected ELCommand doNext() {
            if (result instanceof LocateTask) {
                int[] positions = { ((LocateTask) result).position };
                result = new TaskGroup(positions);
                return Number;
            } else if (result instanceof TaskGroup) {
                return Number;
            }
            return super.doNext();
        }
    },
    
    /**
     * Range command
     */
    Range("-") {
        @Override
        protected ELCommand doNext() {
            if (result instanceof LocateTask) {
                result = new TaskRange(((LocateTask) result).position, -1);
                return Number;
            }
            return super.doNext();
        }
    },
    
    /**
     * All command
     */
    All("*") {
        @Override
        protected ELCommand doNext() {
            if (result instanceof LocateTask) {
                result = new TaskGroup(null);
                return String;
            }
            return super.doNext();
        }
    },
    
    /**
     * Number command.
     */
    Number("") {
        @Override
        protected ELCommand doNext() {
            if (result instanceof LocateTask) {
                String next = parseNext(1, "*");
                if (next != null) {
                    return findNext(next);
                }
                String num = parseWhile("\\d");
                int position = toInt(num);
                try {
                    checkPosition(position);
                } catch (Exception e) {
                    result = null;
                    processing = Locate.el() + processing;
                    return String;
                }
                result = new LocateTask(null, null, position);
                String string = parseNext(1, "=-,");
                return findNext(string);
            } else if (result instanceof TaskRange) {
                String num = parseWhile("\\d");
                int start = ((TaskRange) result).start;
                int end = toInt(num);
                checkPosition(start);
                try {
                    checkPosition(end);
                } catch (Exception e) {
                    result = new TaskCommand(start, Range.el());
                    return super.doNext();
                }
                int l = 0;
                int min = 0;
                if (start < end) {
                    l = end - start + 1;
                    min = start;
                } else {
                    l = start - end + 1;
                    min = end;
                }
                int[] positions = new int[l];
                for (int i = 0; i < l; i++) {
                    positions[i] = min + i;
                }
                result = new TaskGroup(positions);
                String string = parseNext(1, "=");
                return findNext(string);
            } else if (result instanceof TaskGroup) {
                String num = parseWhile("\\d");
                int i = toInt(num);
                checkPosition(i);
                int[] pos = ((TaskGroup) result).positions;
                int l = pos.length + 1;
                int[] positions = Arrays.copyOf(pos, l);
                positions[l - 1] = i;
                result = new TaskGroup(positions);
                String string = parseNext(1, "=,");
                return findNext(string);
            }
            return super.doNext();
        }
        
        private ELCommand findNext(final String string) {
            ELCommand next = command(string);
            if (next != null) {
                return next;
            }
            if (!processing.isEmpty()) {
                return String;
            }
            return super.doNext();
        }
        
        private void checkPosition(final int position) {
            if (position <= 0) {
                throw new IllegalArgumentException("Wrong position" + position);
            }
        }
    },
    
    /**
     * Start command.
     */
    Start("") {
        @Override
        protected ELCommand doNext() {
            String string = parseNext(1, "/?#$!@&%");
            ELCommand next = command(string);
            if (next == null) {
                next = String;
            }
            return next;
        }
    },
    
    /**
     * List edit command.
     */
    ListEdit("&") {
        @Override
        protected ELCommand doNext() {
            if (processing.isEmpty()) {
                result = new SwitchListEdit();
                return super.doNext();
            }
            processing = el + processing;
            return String;
        }
    },
    
    /**
     * Notes command.
     */
    Notes("@") {
        @Override
        protected ELCommand doNext() {
            if (processing.isEmpty()) {
                result = new SwitchNotes();
                return super.doNext();
            }
            processing = el + processing;
            return String;
        }
    },
    
    /**
     * Sync command.
     */
    Sync("%") {
        @Override
        protected ELCommand doNext() {
            if (processing.isEmpty() || processing.length() == 1) {
                result = new SwitchSync();
                return processing.isEmpty() ? super.doNext() : String;
            }
            processing = el + processing;
            return String;
        }
    },
    
    /**
     * Timer command.
     */
    Timer("$") {
        @Override
        protected ELCommand doNext() {
            if (processing.isEmpty() || processing.length() == 1) {
                result = new SwitchTimer();
                return processing.isEmpty() ? super.doNext() : String;
            }
            processing = el + processing;
            return String;
        }
    },
    
    /**
     * Status command.
     */
    Status("!") {
        @Override
        protected ELCommand doNext() {
            if (processing.isEmpty()) {
                result = new SwitchStatus();
                return super.doNext();
            }
            processing = el + processing;
            return String;
        }
    },
    
    /**
     * Assign command.
     */
    Assign("=") {
        @Override
        protected ELCommand doNext() {
            result = new EditTask((TaskResult) result);
            return String;
        }
    },
    
    /**
     * Search command.
     */
    Search("?") {
        @Override
        protected ELCommand doNext() {
            result = new SearchTask();
            return String;
        }
    },
    
    /**
     * Locate command.
     */
    Locate("#") {
        @Override
        protected ELCommand doNext() {
            result = new LocateTask();
            return Number;
        }
    },
    
    /**
     * Switch command.
     */
    Switch("/") {
        @Override
        protected ELCommand doNext() {
            result = new Switch();
            return String;
        }
    };
    
    protected String el;
    protected String processing;
    protected ELResult result;
    
    private ELCommand(final String el) {
        this.el = el;
    }
    
    /**
     * Returns command
     * 
     * @return string
     */
    public String el() {
        return el;
    }
    
    /**
     * Returns result.
     * 
     * @return {@link Object}
     */
    public ELResult getResult() {
        return result;
    }
    
    /**
     * Initializes command.
     * 
     * @param command {@link String}
     */
    public void init(final String command) {
        processing = command;
    }
    
    /**
     * Returns next command.
     * 
     * @return {@link ELCommand}
     */
    public ELCommand next() {
        ELCommand next = doNext();
        next.result = result;
        next.init(processing);
        return next;
    }
    
    protected ELCommand doNext() {
        return End;
    }
    
    protected String parseWhile(final String match) {
        if (processing == null) {
            return processing;
        }
        int i = 0;
        Pattern p = Pattern.compile(match);
        while (processing.length() > i && p.matcher("" + processing.charAt(i)).matches()) {
            i++;
        }
        String result = processing.substring(0, i);
        processing = processing.substring(i);
        return result;
    }
    
    protected String parseTill(final String string) {
        if (processing == null || !processing.contains(string)) {
            return processing;
        }
        int i = processing.indexOf(string);
        String result = processing.substring(0, i);
        processing = processing.substring(i);
        return result;
    }
    
    protected String parseNext(final int number, final String expect) {
        if (processing == null || processing.length() < number) {
            return null;
        }
        String next = processing.substring(0, number);
        if (expect == null || expect.contains(next)) {
            processing = processing.substring(number);
            return next;
        }
        return null;
    }
    
    protected static String[] split(final String haystack, final char needle) {
        List<String> splits = new ArrayList<String>();
        int lastPoint = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                splits.add(haystack.substring(lastPoint, i));
                lastPoint = i;
            }
        }
        splits.add(haystack.substring(lastPoint));
        List<String> newSplits = new ArrayList<String>();
        for (String string : splits) {
            newSplits.add(string.replace(":", ""));
        }
        return newSplits.toArray(new String[] {});
    }
    
    protected int toInt(final String string) {
        int i = 0;
        try {
            i = Integer.valueOf(string);
        } catch (NumberFormatException e) {
            // ignore
        }
        return i;
    }
    
    private static Map<String, ELCommand> commands = new HashMap<String, ELCommand>();
    
    static {
        for (ELCommand command : ELCommand.values()) {
            String st = command.el;
            if (!st.isEmpty()) {
                commands.put(st, command);
            }
        }
    }
    
    /**
     * Returns command by string
     * 
     * @param string
     * @return {@link ELCommand}
     */
    public static ELCommand command(final String string) {
        return commands.get(string);
    }
    
}
