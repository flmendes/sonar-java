package checks;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class NullReturnedOnComputeIfPresentOrAbsent {

  public void badComputeIfPresent() {
    Map<String, String> map = new HashMap<>();
    map.computeIfPresent("myKey", (key, value) -> null); // Noncompliant {{Use "Map.containsKey(key)" followed by "Map.put(key, null)" to add null values.}}

    String nullValue = null;
    map.computeIfPresent("myKey", (key, value) -> nullValue); // Compliant ?

    map.computeIfPresent("myKey", (key, value) -> value + " modified"); // Compliant

    TreeMap<String, String> second = new TreeMap<>();
    second.computeIfPresent("myKey", (key, value) -> null); // Noncompliant {{Use "Map.containsKey(key)" followed by "Map.put(key, null)" to add null values.}}
  }
}
