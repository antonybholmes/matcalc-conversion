package edu.columbia.rdf.matcalc.toolbox.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jebtk.core.collections.DefaultTreeMap;
import org.jebtk.core.collections.TreeSetCreator;

public class HomologyMap {
	private Map<String, Set<String>> mIdMap = 
			DefaultTreeMap.create(new TreeSetCreator<String>());
	
	public void addMapping(String id1, String id2) {
		mIdMap.get(id1.toLowerCase()).add(id2.toLowerCase());
	}
	
	public List<Conversion> homology(Conversion c) {
		List<Conversion> ret = new ArrayList<Conversion>();
		
		String id = c.getId().toLowerCase();
		
		for (String newId : mIdMap.get(id)) {
			ret.add(new Conversion(newId, c, "hom:" + newId));
		}
		
		return ret; //new Conversion(ret, c, "homology");
	}

	public boolean contains(String id) {
		return mIdMap.containsKey(id.toLowerCase());
	}

	public boolean contains(Conversion c) {
		return contains(c.getId());
	}

	public String printKeys() {
		return mIdMap.keySet().toString();
	}
}
