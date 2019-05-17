package collect;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by jiazhangheng on 2019/5/17.
 */
public class LauCache<K, V> extends LinkedHashMap<K, V> {

    private final int CACHESIZE;

    public LauCache(int cacheSize) {
        super((int) Math.ceil(cacheSize/0.75) +1, 0.75f, true);
        CACHESIZE = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > CACHESIZE;
    }


    public  static  void main(String[] args) {
        Map<Integer, Integer > lauCache = new LauCache(100);
        for(int i=0; i< 200;i++) {
            lauCache.put(i, i);
        }
        for (Map.Entry<Integer, Integer > a : lauCache.entrySet()) {
            System.out.println(a.getKey());
        }
    }
}
