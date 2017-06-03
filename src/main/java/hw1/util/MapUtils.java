package hw1.util;

import java.util.*;

/**
 * Created by Abhishek Mulay on 5/24/17.
 */
public class MapUtils {

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }


    public static <K, V extends Comparable<? super V>> String getPrettyString(Map<K, V> map) {
        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            buffer.append(key).append("\t").append(value).append('\n');
        }
        return buffer.toString();
    }

}
