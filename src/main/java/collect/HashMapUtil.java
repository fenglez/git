package collect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiazhangheng on 2017/8/25.
 */
public class HashMapUtil<K,V> {

    transient HashMapUtil.Node<K,V>[] table;
    transient Set<Map.Entry<K,V>> entrySet;
    transient int size;
    transient int modCount;
    int threshold;
    final float loadFactor = 0.75f;

    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        V value;
        HashMapUtil.Node<K,V> next;

        Node(int hash, K key, V value, HashMapUtil.Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }
        public final V getValue()      { return value; }
        public final String toString() { return key + "=" + value; }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }
    }

    public void add() {
        HashMap<String, String> map = new HashMap<String, String>();
        while (true) {
            map.put("a", "b");
        }
    }

    /*****************************************************/
    /**
     *
     * @param <K>
     * @param <V>
     */
    static class CNode<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        volatile V val;
        volatile CNode<K,V> next;

        CNode(int hash, K key, V val, CNode<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        public final K getKey()       { return key; }
        public final V getValue()     { return val; }
        public final int hashCode()   { return key.hashCode() ^ val.hashCode(); }
        public final String toString(){ return key + "=" + val; }
        public final V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            Object k, v, u; Map.Entry<?,?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?,?>)o).getKey()) != null &&
                    (v = e.getValue()) != null &&
                    (k == key || k.equals(key)) &&
                    (v == (u = val) || v.equals(u)));
        }

        /**
         * Virtualized support for map.get(); overridden in subclasses.
         */
        CNode<K,V> find(int h, Object k) {
            CNode<K,V> e = this;
            if (k != null) {
                do {
                    K ek;
                    if (e.hash == h &&
                            ((ek = e.key) == k || (ek != null && k.equals(ek))))
                        return e;
                } while ((e = e.next) != null);
            }
            return null;
        }
    }
    transient volatile CNode<K,V>[] ctable;
    private transient volatile CNode<K,V>[] cnextTable;

    private transient volatile long baseCount;
    private transient volatile int sizeCtl;

    private transient volatile int transferIndex;
    private transient volatile int cellsBusy;
    @sun.misc.Contended static final class CounterCell {
        volatile long value;
        CounterCell(long x) { value = x; }
    }
    private transient volatile CounterCell[] counterCells;
//    private transient ConcurrentHashMap.KeySetView<K,V> ckeySet;
//    private transient ConcurrentHashMap.ValuesView<K,V> cvalues;
//    private transient ConcurrentHashMap.EntrySetView<K,V> centrySet;
    public void addc() {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
        while (true) {
            map.put("a", "b");
        }
    }
}
