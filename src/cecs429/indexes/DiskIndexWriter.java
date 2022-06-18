package cecs429.indexes;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DiskIndexWriter {
	private long[] vocabPositions;
	private int vocabSize;
	
	public void writeIndex(PositionalInvertedIndex index, String path) throws IOException {
		
		String directoryName = path +"/index";
		vocabSize = index.getVocabulary().size();
		
		File directory = new File(directoryName);
	    if (!directory.exists()){
	        directory.mkdir();
	    }
	    
		File f = new File(directoryName+"/index.bin");
		f.createNewFile();
		
		List<String> vocabList = index.getVocabulary();
		OutputStream ostream = new FileOutputStream(f);
		DataOutputStream dataOutputStream = new DataOutputStream(ostream);
		
		for(String vocab:vocabList) {
			List<PositionalIndexPosting> postings = index.getPositionIndexPostings(vocab);
			//Dft
			dataOutputStream.writeInt(postings.size());
			for(PositionalIndexPosting post:postings) {
				//DocID
				dataOutputStream.writeInt(post.getDocumentId());
				List<Integer> positionsList = post.getPositions();
				//Tfd
				dataOutputStream.writeInt(positionsList.size());
				for(Integer position:positionsList) {
					//Position
					dataOutputStream.writeInt(position);
				}
				
			}
		}
		dataOutputStream.close();
		ostream.close();
	}
	
	public void writeVocabList(PositionalInvertedIndex index, String path) throws IOException {
		String directoryName = path +"/index";
		vocabPositions = new long[vocabSize];
		int i=0,pos=0;
		File directory = new File(directoryName);
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	    
	    //vocablist.bin file
		File f = new File(directoryName+"/vocablist.bin");
		f.createNewFile();
		
		List<String> vocabList = index.getVocabulary();
		OutputStream ostream = new FileOutputStream(f);
		DataOutputStream vocabOutputStream = new DataOutputStream(ostream);
		for (String eachVocab : vocabList) {
			vocabOutputStream.writeBytes(eachVocab);
			vocabPositions[i] = pos;
			pos += eachVocab.length();
			i++;
		}
		vocabOutputStream.close();
		ostream.close();
	}

	
}
