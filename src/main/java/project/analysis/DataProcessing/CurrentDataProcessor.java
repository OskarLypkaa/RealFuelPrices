package project.analysis.DataProcessing;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;

public class CurrentDataProcessor extends DataProcessor {

    public Map<String, List<String>> processCurrentData(List<Map<String, List<String>>> currentResultList) {
        Map<String, List<String>> resultMap = mergeMaps(currentResultList);

        // Format to round to two decimal places with dot as the decimal separator
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#.##", symbols);

        // Divide the first element by the second element and add the result as a third
        // element
        for (Map.Entry<String, List<String>> entry : resultMap.entrySet()) {
            List<String> values = entry.getValue();
            if (values.size() == 2) {
                try {
                    double firstValue = Double.parseDouble(values.get(0));
                    double secondValue = Double.parseDouble(values.get(1));

                    // Check for division by zero
                    if (secondValue != 0.0) {
                        double resultValue = firstValue / secondValue;
                        // Format the result to two decimal places with dot as the decimal separator
                        String formattedResult = decimalFormat.format(resultValue);
                        // Add the formatted result as a third element to the list
                        values.add(formattedResult);
                    } else {
                        System.out.println("Error: Division by zero for key " + entry.getKey());
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid number format for key " + entry.getKey());
                }
            }
        }

        return resultMap;
    }

    public Map<String, List<String>> processHistoricalData(List<Map<String, List<String>>> historicalResultList) {
        return null;
    }
}
