package cecs429.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

public class InvertedIndex implements Index {

	Map<String,List<Posting>> map = new HashMap<>();
	
	@Override
	public List<Posting> getPostings(String term) {
		if(map.containsKey(term)) {
			return map.get(term);
		} 
		return new ArrayList<Posting>();
	}

	@Override
	public List<String> getVocabulary() {
		Set<String> vocab = map.keySet();
		List<String> list = vocab.stream().collect(Collectors.toList());
		Collections.sort(list);
		return list;
	}

	public void addTerm(String term,int docId) {
		List<Posting> list = map.get(term);
		
		if(CollectionUtils.isEmpty(list)) {
			list = new ArrayList<>();
			list.add(new Posting(docId));
		} else if(docId!=list.get(list.size()-1).getDocumentId()){
			list.add(new Posting(docId));
		}
		map.put(term, list);
	}
}
