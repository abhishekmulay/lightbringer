package hw2.search;

import hw2.indexing.Indexer;
import hw2.indexing.IndexingUnit;

import java.util.*;

/**
 * Created by Abhishek Mulay on 6/10/17.
 */
public class ProximitySearcher {

    public static void search(List<IndexingUnit> indexingUnitList) {
        System.out.println("Finding shortest span in: " + indexingUnitList);
        Map<String, List<Integer>> termPositionMap = new HashMap<>();
        for (IndexingUnit unit : indexingUnitList) {
            termPositionMap.put(unit.getTerm(), unit.getPosition());
        }



    }


    public static void main(String[] args) {
        Integer [] positions1 = {0, 5, 10, 15};
        IndexingUnit u1 = new IndexingUnit("cheap", "d1", 1 , 4, Arrays.asList(positions1), 1, 1);

        Integer [] positions2 = {1, 3, 6, 9};
        IndexingUnit u2 = new IndexingUnit("pudding", "d2", 2 , 4, Arrays.asList(positions2), 4, 1);

        Integer [] positions3 = {4, 8, 16, 21};
        IndexingUnit u3 = new IndexingUnit("pops", "d3", 3 , 4, Arrays.asList(positions3), 4, 4);

        List<IndexingUnit> units = new ArrayList<>();
        units.add(u1); units.add(u2); units.add(u3);
        search(units);
    }
}
