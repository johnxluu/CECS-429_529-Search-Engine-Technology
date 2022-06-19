package cecs429.indexes;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cecs429.utils.AppConstants;

public class DiskIndexWriter {
	private long[] vocabPositions;
	
	public void writeIndex(PositionalInvertedIndex index, String path) throws IOException {
		List<String> vocabList = index.getVocabulary();
		Map<Integer, Double> docWeightsMap = new HashMap<Integer, Double>();
		int i=0;
		int corpusSize = getCorpusSize(path);
		
		//postings.bin
		FileOutputStream ostream = new FileOutputStream(createFileInADirectory(path +AppConstants.INDEX, AppConstants.POSTINGS_BIN));
		DataOutputStream dataOutputStream = new DataOutputStream(ostream);
		
		//vocabtable.bin
		OutputStream ostream1 = new FileOutputStream(createFileInADirectory(path +AppConstants.INDEX, AppConstants.VOCAB_TABLE_BIN));
		DataOutputStream vocabTable = new DataOutputStream(ostream1);
		byte[] vocabByteSize = ByteBuffer.allocate(4).putInt(vocabList.size()).array();
		vocabTable.write(vocabByteSize, 0, vocabByteSize.length);
		
		//docWeights.bin
		OutputStream ostream2 = new FileOutputStream(createFileInADirectory(path +AppConstants.INDEX, AppConstants.DOC_WEIGHTS_BIN));
		DataOutputStream docWeightsStream = new DataOutputStream(ostream2);
		
		
		for(String vocab:vocabList) {
			List<PositionalIndexPosting> postings = index.getPositionIndexPostings(vocab);
			long prevDocId=0;
			byte[] vocabPosBytes = ByteBuffer.allocate(8).putLong(vocabPositions[i]).array();
			byte[] postingsPosBytes = ByteBuffer.allocate(8).putLong(ostream.getChannel().position()).array();

			vocabTable.write(vocabPosBytes, 0, vocabPosBytes.length);
			vocabTable.write(postingsPosBytes, 0, postingsPosBytes.length);
			
			//Dft
			dataOutputStream.writeLong(postings.size());
			for(PositionalIndexPosting post:postings) {
				
				int tf = post.getPositions().size();
				double wdt = 1 + Math.log(tf);			
				
				
				//Preparing map of document Id with squares of wdt
				if (docWeightsMap.containsKey(post.getDocumentId())) {						
					docWeightsMap.put(post.getDocumentId(), docWeightsMap.get(post.getDocumentId()) + Math.pow(wdt, 2));
				} else {
					docWeightsMap.put(post.getDocumentId(), Math.pow(wdt, 2));
				}
				
				long prevPostingId=0;
				prevDocId = post.getDocumentId()-prevDocId;
				//DocID
				dataOutputStream.writeLong(prevDocId);
				List<Integer> positionsList = post.getPositions();
				//Tfd
				dataOutputStream.writeLong(positionsList.size());
				for(Integer position:positionsList) {
					//Position
					prevPostingId = position-prevPostingId;
					dataOutputStream.writeLong(prevPostingId);
				}
			}
			i++;
		}
		//Doesn't give sorted docIds
//		for(Entry<Long,Double> map:docWeightsMap.entrySet()) {
//			double value;
//			if(map.getValue()!=null) {
//				value = Math.sqrt(map.getValue());
//			} else {
//				value = 0;
//			}
//			byte[] buffer = ByteBuffer.allocate(8).putDouble(value).array();
//			docWeightsStream.write(buffer, 0, buffer.length);
//		}
		for (int j = 0; j < corpusSize; j++) {
			double value;
			
			if(docWeightsMap.get(j)!=null) {
				value = Math.sqrt(docWeightsMap.get(j));
			} else {
				value = 0;
			}
			 byte[] docWeightsMapBuffer = ByteBuffer.allocate(8).putDouble(value).array();
			 docWeightsStream.write(docWeightsMapBuffer, 0, docWeightsMapBuffer.length);
		}
		docWeightsStream.close();
		dataOutputStream.close();
		ostream.close();
	}

	private int getCorpusSize(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		int size = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
		    size++;
		  }
		}
		return size;
	}

	private File createFileInADirectory(String directoryName,String fileName) throws IOException {
		
		File directory = new File(directoryName);
	    if (!directory.exists()){
	        directory.mkdir();	
	    }
	    
		File f = new File(directoryName+fileName);
		f.createNewFile();
		return f;
	}
	
	public void writeVocabList(PositionalInvertedIndex index, String path) throws IOException {
		String directoryName = path +AppConstants.INDEX;
		int i=0,pos=0;
		
	    //vocablist.bin file
		File f = createFileInADirectory(directoryName,AppConstants.VOCAB_LIST_BIN);
		f.createNewFile();
		
		List<String> vocabList = index.getVocabulary();
		vocabPositions = new long[vocabList.size()];
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

//	public void writeVocabTable(PositionalInvertedIndex index, String path) {
//		
//	}
}
