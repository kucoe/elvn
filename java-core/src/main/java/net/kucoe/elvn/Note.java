package net.kucoe.elvn;

/**
 * Note bean.
 * 
 * @author Vitaliy Basyuk
 */
public class Note implements Comparable<Note> {
    
    private final Long id;
    private final String text;
    
    /**
     * Constructs Note.
     * 
     * @param id
     * @param text
     */
    public Note(final Long id, final String text) {
        this.id = id;
        this.text = text;
    }
    
    /**
     * Returns the text String.
     * 
     * @return the text String.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns the position integer.
     * 
     * @return the position integer.
     */
    public Long getId() {
        return id;
    }
    
    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Note) {
            return getId().equals(((Note) obj).getId());
        }
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(":");
        sb.append(getText());
        sb.append("-");
        sb.append(getId());
        return sb.toString();
    }
    
    @Override
    public int compareTo(final Note o) {
        long thisVal = getId();
        long anotherVal = o == null ? -1l : o.getId();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }
}
