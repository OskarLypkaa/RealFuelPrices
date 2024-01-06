package project.analysis.DataProcessing;

import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class DataProcessor {

    public abstract Map<String, List<String>> processHistoricalData(
            List<Map<String, List<String>>> historicalResultList);

    public abstract Map<String, List<String>> processCurrentData(List<Map<String, List<String>>> currentResultList);

    protected Map<String, List<String>> mergeMaps(List<Map<String, List<String>>> maps) {
        Map<String, List<String>> mergedMap = new LinkedHashMap<>();

        for (Map<String, List<String>> map : maps) {
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();

                mergedMap.merge(key, value, (list1, list2) -> {
                    List<String> mergedList = new ArrayList<>(list1);
                    mergedList.addAll(list2);
                    return mergedList;
                });
            }
        }

        return mergedMap;
    }
}
