package kvv.education.khasang.java1.chat.model.storage.omd;

import java.io.Serializable;

public class Link implements Comparable<Link>, Serializable {

    private static final long serialVersionUID = 0000000000000000001L;

    private Integer key1;
    private Integer key2;

    Link(Integer key1, Integer key2) {
        this.key1 = key1;
        this.key2 = key2;
    }

    public Integer getKey1() {
        return key1;
    }

    public Integer getKey2() {
        return key2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link)) return false;

        Link link = (Link) o;

        if (key1 != null ? !key1.equals(link.key1) : link.key1 != null) return false;
        return key2 != null ? key2.equals(link.key2) : link.key2 == null;
    }

    @Override
    public int hashCode() {
        int result = key1 != null ? key1.hashCode() : 0;
        result = 31 * result + (key2 != null ? key2.hashCode() : 0);
        return result;
    }


    @Override
    public int compareTo(Link o) {
        if (this.key1 > o.getKey1()) return 1;
        if (this.key1 < o.getKey1()) return -1;
        return Integer.valueOf(this.key2).compareTo(o.key2);
    }
}
