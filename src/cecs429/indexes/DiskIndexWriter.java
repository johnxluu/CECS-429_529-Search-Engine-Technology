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

import cecs429.utils.AppConstants;
import cecs429.utils.AppUtils;

public class DiskIndexWriter {
	private long[] vocabPositions;
	
	public void writeIndex(PositionalInvertedIndex index, String path) throws IOException {
		List<String> vocabList = index.getVocabulary();
		Map<Integer, Double> docWeightsMap = new HashMap<Integer, Double>();
		int i=0;
		int corpusSize = AppUtils.getCorpusSize(path);
		
		//postings.bin
//		FileOutputStream ostream = new FileOutputStream(createFileInADirectory(path +AppConstants.INDEX, AppConstants.POSTINGS_BIN));
		FileOutputStream postingsFos = new FileOutputStream(new File( path+AppConstants.INDEX, AppConstants.POSTINGS_BIN));
		
		//vocabtable.bin
//		OutputStream ostream1 = new FileOutputStream(createFileInADirectory(path +AppConstants.INDEX, AppConstants.VOCAB_TABLE_BIN));
		FileOutputStream vocabTable = new FileOutputStream(new File( path+AppConstants.INDEX, AppConstants.VOCAB_TABLE_BIN));
		byte[] vocabByteSize = ByteBuffer.allocate(4).putInt(vocabList.size()).array();
		vocabTable.write(vocabByteSize, 0, vocabByteSize.length);
		
		//docWeights.bin
//		OutputStream ostream2 = new FileOutputStream(createFileInADirectory(path +AppConstants.INDEX, AppConstants.DOC_WEIGHTS_BIN));
//		DataOutputStream docWeightsStream = new DataOutputStream(ostream2);
		FileOutputStream docWeightsStream = new FileOutputStream(new File( path+AppConstants.INDEX, AppConstants.DOC_WEIGHTS_BIN));
		
		//Wdt File wdt.bin
//		FileOutputStream WdtStream = new FileOutputStream(new File( path+AppConstants.INDEX, AppConstants.DOC_WEIGHTS_BIN));
		
		for(String vocab:vocabList) {
			List<PositionalIndexPosting> postings = index.getPositionIndexPostings(vocab);
			int prevDocId=0;
			
			//VocabTable
			byte[] vocabPosBytes = ByteBuffer.allocate(8).putLong(vocabPositions[i]).array();
			vocabTable.write(vocabPosBytes, 0, vocabPosBytes.length);
			byte[] postingsPosBytes = ByteBuffer.allocate(8).putLong(postingsFos.getChannel().position()).array();
			vocabTable.write(postingsPosBytes, 0, postingsPosBytes.length);
			
			//Dft
			byte[] docFreqBytes = ByteBuffer.allocate(4).putInt(postings.size()).array();
			
			postingsFos.write(docFreqBytes,0,docFreqBytes.length);
			for(PositionalIndexPosting post:postings) {
				
				int tf = post.getPositions().size();
				double wdt = 1 + Math.log(tf);			
				
				//Write Wdt as 8 byte to postings
				byte[] dwtBytes = ByteBuffer.allocate(8).putDouble(wdt).array();
				postingsFos.write(dwtBytes,0,dwtBytes.length);
				
				//Preparing map of document Id with squares of wdt
				if (docWeightsMap.containsKey(post.getDocumentId())) {						
					docWeightsMap.put(post.getDocumentId(), docWeightsMap.get(post.getDocumentId()) + Math.pow(wdt, 2));
				} else {
					docWeightsMap.put(post.getDocumentId(), Math.pow(wdt, 2));
				}
				
				int prevPostingId=0;
				//DocID
				byte[] docIdBytes = ByteBuffer.allocate(4).putInt(post.getDocumentId()-prevDocId).array();
				postingsFos.write(docIdBytes,0,docIdBytes.length);
				prevDocId = post.getDocumentId();
				
				List<Integer> positionsList = post.getPositions();
				//Tfd
				byte[] tfBytes = ByteBuffer.allocate(4).putInt(positionsList.size()).array();
				postingsFos.write(tfBytes,0,tfBytes.length);
				
				for(Integer position:positionsList) {
					//Position
					byte[] positionBytes = ByteBuffer.allocate(4).putInt(position-prevPostingId).array();
					postingsFos.write(positionBytes,0,positionBytes.length);
					prevPostingId = position;
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
//			byte[] wdtBuffer = ByteBuffer.allocate(8).putDouble(value).array();
			if(docWeightsMap.get(j)!=null) {
//				WdtStream.write(wdtBuffer, 0, wdtBuffer.length);
				value = Math.sqrt(docWeightsMap.get(j));
			} else {
//				wdtBuffer.write(wdtBuffer, 0, wdtBuffer.length);
				value = 0;
			}
			 byte[] docWeightsMapBuffer = ByteBuffer.allocate(8).putDouble(value).array();
			 docWeightsStream.write(docWeightsMapBuffer, 0, docWeightsMapBuffer.length);
		}
		docWeightsStream.close();
		postingsFos.close();

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
