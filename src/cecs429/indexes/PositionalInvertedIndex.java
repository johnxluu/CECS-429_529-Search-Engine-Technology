package cecs429.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

public class PositionalInvertedIndex implements Index{
	Map<String,Map<Posting,List<Integer>>> map = new HashMap<>();
	
	@Override
	public List<Posting> getPostings(String term) {
		if(map.containsKey(term)) {
			Map<Posting, List<Integer>> innerMap = map.get(term);
			Set<Posting> postings = innerMap.keySet();
			List<Posting> list = postings.stream().collect(Collectors.toList());
			return list;
		}
		return new ArrayList<>();
	}

	@Override
	public List<String> getVocabulary() {
		if(MapUtils.isEmpty(map)) return new ArrayList<>();
		
		Set<String> vocab = map.keySet();
		List<String> list = vocab.stream().collect(Collectors.toList());
		Collections.sort(list);
		return list;
	}
	
	public List<Integer> getPositions(String term, int docId){
		if(map.containsKey(term)) {
			Map<Posting, List<Integer>> innerMap = map.get(term);
			if(innerMap.containsKey(new Posting(docId))) {
				return innerMap.get(new Posting(docId));
			}
		}
		return new ArrayList<>();
	}
	
	public void addTerm(String term, int docId, int position) {
//		Map<Posting,List<Integer>> innerMap = new HashMap<>();
//		List<Posting> positions = getPositions(term, docId);
//		
//		if(CollectionUtils.isEmpty(postings)) {
//			
//		} else if(docId!=postings.get(postings.size()-1).getDocumentId())){
//			postings.add(new Posting(docId));
//			map.put(term, postings);
//		}
	}

}
