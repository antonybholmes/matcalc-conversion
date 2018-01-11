package edu.columbia.rdf.matcalc.toolbox.conversion;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.collections.TreeSetCreator;

public class GeneMapping {
  private Map<String, Set<String>> mOfficalIdMap = DefaultHashMap
      .create(new TreeSetCreator<String>());

  public Collection<String> getIds(String type) {
    return mOfficalIdMap.get(type);
  }
}
