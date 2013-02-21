package net.kucoe.elvn.util;

import java.lang.reflect.Array;
import java.text.*;
import java.util.*;

/**
 * Abstract implementation of {@link Jsonizer}.
 * 
 * @author Vitaliy Basyuk
 */
public class Jsonizer {
    
    private static final Object OBJECT_END = new Object();
    private static final Object ARRAY_END = new Object();
    private static final Object COLON = new Object();
    private static final Object COMMA = new Object();
    protected static final char QUOTE = '"';
    protected static final String BEGIN_OBJECT = "{";
    protected static final String END_OBJECT = "}";
    protected static final String BEGIN_ARRAY = "[";
    protected static final String END_ARRAY = "]";
    protected static final String NAME_SEPARATOR = ":";
    protected static final String VALUE_SEPARATOR = ",";
    protected static final String DOT = ".";
    protected static final String NULL = "null";
    protected static final char[] hex = "0123456789ABCDEF".toCharArray();
    protected static final Map<Character, Character> escapes = new HashMap<Character, Character>();
    protected Map<String, Object> current;
    protected Map<String, Object> instance;
    
    static {
        escapes.put('"', '"');
        escapes.put('\\', '\\');
        escapes.put('/', '/');
        escapes.put('b', '\b');
        escapes.put('f', '\f');
        escapes.put('n', '\n');
        escapes.put('r', '\r');
        escapes.put('t', '\t');
    }
    
    private CharacterIterator it;
    private char c;
    private Object token;
    private StringBuilder buf;
    @SuppressWarnings("rawtypes")
    private final Stack stack = new Stack();
    private DateFormat formatter;
    
    private char next() {
        c = it.next();
        return c;
    }
    
    private void skipWhiteSpace() {
        while (Character.isWhitespace(c)) {
            next();
        }
    }
    
    /**
     * Reads JSON string to object.
     * 
     * @param string string
     * @return Object
     * @throws JsonException if json reading fails
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> read(final String string) throws JsonException {
        if (string == null) {
            return null;
        }
        buf = new StringBuilder();
        it = new StringCharacterIterator(string);
        c = it.first();
        return (Map<String, Object>) read();
    }
    
    /**
     * Writes Object to JSON string.
     * 
     * @param object Object to be serialized into JSON
     * @return JSON string for object
     * @throws JsonException if json writing fails
     */
    public String write(final Map<String, Object> object) throws JsonException {
        if (object == null) {
            return null;
        }
        buf = new StringBuilder();
        current = object;
        writeValue(object);
        return buf.toString();
    }
    
    protected Object read() throws JsonException {
        Object ret;
        skipWhiteSpace();
        if (c == '"') {
            next();
            ret = readString('"');
        } else if (c == '\'') {
            next();
            ret = readString('\'');
        } else if (c == '[') {
            next();
            ret = readArray();
        } else if (c == ']') {
            ret = ARRAY_END;
            next();
        } else if (c == ',') {
            ret = COMMA;
            next();
        } else if (c == '~') {
            next();
            next();
            ret = readDate(readString('"'));
        } else if (c == '{') {
            next();
            ret = readMap();
        } else if (c == '}') {
            ret = OBJECT_END;
            next();
        } else if (c == ':') {
            ret = COLON;
            next();
        } else if ((c == 't') && (next() == 'r') && (next() == 'u') && (next() == 'e')) {
            ret = Boolean.TRUE;
            next();
        } else if ((c == 'f') && (next() == 'a') && (next() == 'l') && (next() == 's') && (next() == 'e')) {
            ret = Boolean.FALSE;
            next();
        } else if ((c == 'n') && (next() == 'u') && (next() == 'l') && (next() == 'l')) {
            ret = null;
            next();
        } else if (Character.isDigit(c) || (c == '-')) {
            ret = readNumber();
        } else {
            throw buildInvalidInputException();
        }
        token = ret;
        return ret;
    }
    
    protected Object readObject() throws JsonException {
        instance = new HashMap<String, Object>();
        Object ret = instance;
        Object next = read();
        if (next != OBJECT_END) {
            if (next instanceof String) {
                String key = (String) next;
                while (token != OBJECT_END) {
                    read(); // should be a colon
                    if (token != OBJECT_END) {
                        Object value = read();
                        if (!getExcludeRProperties().contains(key)) {
                            applyProperty(key, value);
                        }
                        if (read() == COMMA) {
                            Object name = read();
                            if (name instanceof String) {
                                key = (String) name;
                            } else {
                                throw buildInvalidInputException();
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    protected void applyProperty(final String name, final Object value) {
        instance.put(name, value);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map readMap() throws JsonException {
        Map ret = new HashMap();
        Object next = read();
        if (next != OBJECT_END) {
            Object key = next;
            while (token != OBJECT_END) {
                read(); // should be a colon
                if (token != OBJECT_END) {
                    ret.put(key, read());
                    if (read() == COMMA) {
                        key = read();
                    }
                }
            }
        }
        return ret;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List readArray() throws JsonException {
        List ret = new ArrayList();
        Object value = read();
        while (token != ARRAY_END) {
            if (value instanceof String && ((String) value).equals(NULL)) {
                value = null;
            }
            ret.add(value);
            Object read = read();
            if (read == COMMA) {
                value = read();
            } else if (read != ARRAY_END) {
                throw buildInvalidInputException();
            }
        }
        return ret;
    }
    
    protected Date readDate(final String string) {
        if (formatter == null) {
            formatter = new SimpleDateFormat("yyyy-MM-dd");
        }
        DateFormat formatter = this.formatter;
        try {
            return formatter.parse(string);
        } catch (ParseException e) {
            return null;
        }
    }
    
    protected Number readNumber() {
        buf.setLength(0);
        if (c == '-') {
            add();
        }
        addDigits();
        if (c == '.') {
            add();
            addDigits();
        }
        if ((c == 'e') || (c == 'E')) {
            add();
            
            if ((c == '+') || (c == '-')) {
                add();
            }
            addDigits();
        }
        return (buf.indexOf(".") >= 0) ? (Number) Double.parseDouble(buf.toString()) : (Number) Long.parseLong(buf
                .toString());
    }
    
    protected String readString(final char quote) {
        buf.setLength(0);
        while (c != quote && c != CharacterIterator.DONE) {
            if (c == '\\') {
                next();
                if (c == 'u') {
                    addChar(readUnicode());
                } else {
                    Object value = escapes.get(c);
                    if (value != null) {
                        addChar((Character) value);
                    }
                }
            } else {
                add();
            }
        }
        next();
        return buf.toString();
    }
    
    protected void addChar(final char cc) {
        buf.append(cc);
        next();
    }
    
    private void add() {
        addChar(c);
    }
    
    private void addDigits() {
        while (Character.isDigit(c)) {
            add();
        }
    }
    
    private char readUnicode() {
        int value = 0;
        for (int i = 0; i < 4; ++i) {
            switch (next()) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    value = ((value << 4) + c) - '0';
                    break;
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    value = ((value << 4) + c) - 'k';
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    value = ((value << 4) + c) - 'K';
                    break;
            }
        }
        return (char) value;
    }
    
    /**
     * Detect cyclic references
     */
    @SuppressWarnings({ "rawtypes" })
    protected void writeValue(final Object object) throws JsonException {
        if (object == null) {
            writeElement(NULL);
            return;
        }
        if (stack.contains(object)) {
            Class clazz = object.getClass();
            if (clazz.isPrimitive() || clazz.equals(String.class)) {
                process(object);
            } else {
                writeElement(NULL);
            }
            return;
        }
        process(object);
    }
    
    /**
     * Serialize object into json
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void process(final Object object) throws JsonException {
        stack.push(object);
        if (object instanceof Class) {
            writeString(object);
        } else if (object instanceof Boolean) {
            writeBool((Boolean) object);
        } else if (object instanceof Number) {
            writeElement(object);
        } else if (object instanceof String) {
            writeString(object);
        } else if (object instanceof Character) {
            writeString(object);
        } else if (object instanceof Map) {
            writeMap((Map) object);
        } else if (object.getClass().isArray()) {
            writeArray(object);
        } else if (object instanceof Iterable) {
            writeArray(((Iterable) object).iterator());
        } else if (object instanceof Date) {
            writeDate((Date) object);
        } else if (object instanceof Calendar) {
            writeDate(((Calendar) object).getTime());
        } else if (object instanceof Locale) {
            writeString(object);
        } else if (object instanceof Enum) {
            writeEnum((Enum) object);
        } else {
            if (current == object) {
                writeBean((Map<String, Object>) object);
            } else {
                Jsonizer jsonizer = new Jsonizer();
                String json = jsonizer.write((Map<String, Object>) object);
                writeElement(json);
            }
        }
        stack.pop();
    }
    
    /**
     * Instrospects bean and serialize its properties
     */
    protected void writeBean(final Map<String, Object> object) throws JsonException {
        writeElement(BEGIN_OBJECT);
        boolean hasData = false;
        for (String field : object.keySet()) {
            if (getExcludeWProperties().contains(field)) {
                continue;
            }
            Object value = object.get(field);
            boolean propertyPrinted = writeProperty(field, value, hasData);
            hasData = hasData || propertyPrinted;
        }
        writeElement(END_OBJECT);
    }
    
    /**
     * Instrospects an Enum and serialize it as a name/value pair
     */
    @SuppressWarnings("rawtypes")
    protected void writeEnum(final Enum enumeration) {
        writeString(enumeration.name());
    }
    
    /**
     * Add name/value pair to buffer
     */
    protected boolean writeProperty(final String name, final Object value, final boolean hasData) throws JsonException {
        if (value != null) {
            if (hasData) {
                writeElement(VALUE_SEPARATOR);
            }
            writeChar(QUOTE);
            writeElement(name);
            writeElement('\"' + NAME_SEPARATOR);
            writeValue(value);
            return true;
        }
        return false;
    }
    
    /**
     * Add map to buffer
     */
    @SuppressWarnings("rawtypes")
    protected void writeMap(final Map map) throws JsonException {
        writeElement(BEGIN_OBJECT);
        Iterator it = map.entrySet().iterator();
        boolean hasData = false;
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            if (key == null) {
                continue;
            }
            if (hasData) {
                writeElement(VALUE_SEPARATOR);
            }
            hasData = true;
            writeValue(key);
            writeElement(NAME_SEPARATOR);
            writeValue(entry.getValue());
        }
        writeElement(END_OBJECT);
    }
    
    /**
     * Add date to buffer
     */
    protected void writeDate(final Date date) {
        if (formatter == null) {
            formatter = new SimpleDateFormat("yyyy-MM-dd");
        }
        DateFormat formatter = this.formatter;
        writeChar('~');
        writeString(formatter.format(date));
    }
    
    /**
     * Add array to buffer
     */
    @SuppressWarnings({ "rawtypes" })
    protected void writeArray(final Iterator it) throws JsonException {
        writeElement(BEGIN_ARRAY);
        boolean hasData = false;
        while (it.hasNext()) {
            if (hasData) {
                writeElement(VALUE_SEPARATOR);
            }
            hasData = true;
            writeValue(it.next());
        }
        writeElement(END_ARRAY);
    }
    
    /**
     * Add array to buffer
     */
    protected void writeArray(final Object object) throws JsonException {
        writeElement(BEGIN_ARRAY);
        int length = Array.getLength(object);
        boolean hasData = false;
        for (int i = 0; i < length; ++i) {
            if (hasData) {
                writeElement(VALUE_SEPARATOR);
            }
            hasData = true;
            writeValue(Array.get(object, i));
        }
        writeElement(END_ARRAY);
    }
    
    /**
     * Add boolean to buffer
     */
    protected void writeBool(final boolean b) {
        writeElement(b ? "true" : "false");
    }
    
    /**
     * Escape characters
     */
    protected void writeString(final Object obj) {
        writeChar(QUOTE);
        CharacterIterator it = new StringCharacterIterator(obj.toString());
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (c == QUOTE) {
                writeElement("\\\"");
            } else if (c == '\\') {
                writeElement("\\\\");
            } else if (c == '/') {
                writeElement("\\/");
            } else if (c == '\b') {
                writeElement("\\b");
            } else if (c == '\f') {
                writeElement("\\f");
            } else if (c == '\n') {
                writeElement("\\n");
            } else if (c == '\r') {
                writeElement("\\r");
            } else if (c == '\t') {
                writeElement("\\t");
            } else if (Character.isISOControl(c)) {
                writeUnicode(c);
            } else {
                writeChar(c);
            }
        }
        writeChar(QUOTE);
    }
    
    /**
     * Add object to buffer
     */
    protected void writeElement(final Object obj) {
        buf.append(obj);
    }
    
    /**
     * Add char to buffer
     */
    protected void writeChar(final char c) {
        buf.append(c);
    }
    
    /**
     * Represent as unicode.
     * 
     * @param c character to be encoded
     */
    protected void writeUnicode(final char c) {
        writeElement("\\u");
        int n = c;
        for (int i = 0; i < 4; ++i) {
            int digit = (n & 0xf000) >> 12;
            
            writeChar(hex[digit]);
            n <<= 4;
        }
    }
    
    private List<String> getExcludeWProperties() {
        List<String> asList = Arrays.asList("class", "declaringClass", "serialVersionUID");
        List<String> excludeWriteProperties = getExcludeWriteProperties();
        if (excludeWriteProperties != null) {
            asList.addAll(excludeWriteProperties);
        }
        return asList;
    }
    
    private List<String> getExcludeRProperties() {
        List<String> asList = Arrays.asList("class", "declaringClass", "serialVersionUID");
        List<String> excludeReadProperties = getExcludeReadProperties();
        if (excludeReadProperties != null) {
            asList.addAll(excludeReadProperties);
        }
        return asList;
    }
    
    protected List<String> getExcludeWriteProperties() {
        return Collections.emptyList();
    }
    
    protected List<String> getExcludeReadProperties() {
        return Collections.emptyList();
    }
    
    protected JsonException buildInvalidInputException() {
        return new JsonException("Input string is not well formed JSON (invalid char " + c + ')');
    }
    
}
