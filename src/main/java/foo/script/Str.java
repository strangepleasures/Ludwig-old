package foo.script;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public final class Str implements Comparable<Str> {
    public final String str;

    public Str(String str) {
        this.str = str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return str.equals(((Str) o).str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public String toString() {
        return "\"" + StringEscapeUtils.escapeJavaScript(str) + "\"";
    }

    @Override
    public int compareTo(Str o) {
        return str.compareTo(o.str);
    }
}

