package cecs429.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

public class PositionalInvertedIndex implements Index{
	Map<String,List<PositionalIndexPosting>> map = new HashMap<>();
	
	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> posting = new ArrayList<>();
		if(map.containsKey(term)) {
			List<PositionalIndexPosting> postingsList = map.get(term);
			for(PositionalIndexPosting each: postingsList) {
				posting.add(new Posting(each.getDocumentId()));
			}
			
			return posting;
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
		List<Integer> pos = new ArrayList<>();
		if(map.containsKey(term)) {
			List<PositionalIndexPosting> postingsList = map.get(term);
			if(postingsList.contains(pos))
			for(PositionalIndexPosting each: postingsList) {
				if(each.getDocumentId()==docId) {
					pos.addAll(each.getPositions());
					break;
				}
			}
			
			return pos;
		}
		return new ArrayList<>();
	}
	
	public List<PositionalIndexPosting> getPositionIndexPostings(String term){
		if(map.containsKey(term)) {
			List<PositionalIndexPosting> postingsList = map.get(term);
			if(!CollectionUtils.isEmpty(postingsList)) {
				return postingsList;
			}
		}
		return new ArrayList<>();
	}
	
	public void addTerm(String term, int docId, int position) {

		List<PositionalIndexPosting> pos = getPositionIndexPostings(term);
		if(!CollectionUtils.isEmpty(pos)) {
			if(docId!=pos.get(pos.size()-1).getDocumentId()) {
				pos.add(new PositionalIndexPosting(docId, position));
				
			} else {
				pos.get(pos.size()-1).getPositions().add(position);
				
			}
			map.put(term, pos);
		} else {
			List<PositionalIndexPosting> list = new ArrayList<>();
			list.add(new PositionalIndexPosting(docId, position));
			map.put(term, list);
		}
		
	}

}
