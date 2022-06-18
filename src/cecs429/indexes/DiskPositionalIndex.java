package cecs429.indexes;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.utils.AppConstants;

public class DiskPositionalIndex implements Index {
	private RandomAccessFile postingStream;
	private RandomAccessFile vocabListStream;
	private RandomAccessFile docWeightsStream;
	
	private long[] vocabTable;

	public DiskPositionalIndex(String path) throws IOException {
		vocabListStream = new RandomAccessFile(new File(path + AppConstants.VOCAB_BIN), "r");
		postingStream = new RandomAccessFile(new File(path + AppConstants.POSTINGS_BIN),"r");
		docWeightsStream = new RandomAccessFile(new File(path + AppConstants.DOC_WEIGHTS_BIN),"r");
		vocabTable = readVocabTable(path + AppConstants.VOCAB_TABLE_BIN);
	}

	private long[] readVocabTable(String path) throws IOException {
		int vocabTableIndex = 0;
		long[] vocabTable;
		byte[] temp = new byte[4];
		
		RandomAccessFile dis = new RandomAccessFile(new File(path),"r");

		dis.read(temp, 0, temp.length);
		int len = ByteBuffer.wrap(temp).getInt() * 2;
		vocabTable = new long[len];

		temp = new byte[8];

		while (dis.read(temp, 0, temp.length) > 0) {
			vocabTable[vocabTableIndex] = ByteBuffer.wrap(temp).getLong();
			vocabTableIndex++;
		}
		
		dis.close();
		return vocabTable;
	}

	@Override
	public List<Posting> getPostings(String term) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getVocabulary() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PositionalIndexPosting> getPositionIndexPostings(String term) throws IOException {
		long postingsPosition = searchPosting(term);
		if (postingsPosition >= 0) {
			return readPostingsFromFile(postingStream, postingsPosition);
		}
		return null;
	}

	private List<PositionalIndexPosting> readPostingsFromFile(RandomAccessFile postingStream, long postingsPosition) throws IOException {
		List<PositionalIndexPosting>
		docList = new ArrayList<PositionalIndexPosting>();

		postingStream.seek(postingsPosition);

		byte[] buffer = new byte[4];
		postingStream.read(buffer, 0, buffer.length);

		int documentFrequency = ByteBuffer.wrap(buffer).getInt();

		int docId = 0;
		int lastDocId = 0;

		byte docIdsBuffer[] = new byte[4];
		byte positionsBuffer[] = new byte[4];
		byte wdtBuffer[] = new byte[8];

		for (int docIdIndex = 0; docIdIndex < documentFrequency; docIdIndex++) {

			postingStream.read(docIdsBuffer, 0, docIdsBuffer.length);

			docId = ByteBuffer.wrap(docIdsBuffer).getInt() + lastDocId;
			postingStream.read(wdtBuffer, 0, wdtBuffer.length);
			float wdt = ByteBuffer.wrap(wdtBuffer).getFloat();
			
			buffer = new byte[4];
			
			postingStream.read(buffer, 0, buffer.length);
			int termFreq = ByteBuffer.wrap(buffer).getInt();

			int[] positions = new int[termFreq];
			
			for (int positionIndex = 0; positionIndex < termFreq; positionIndex++) {
				postingStream.read(positionsBuffer, 0, positionsBuffer.length);
				positions[positionIndex] = ByteBuffer.wrap(positionsBuffer).getInt();
			}

			lastDocId = docId;
			PositionalIndexPosting positionalPosting = new PositionalIndexPosting(docId,
					Arrays.stream(positions).boxed().collect(Collectors.toList()), wdt);

			docList.add(positionalPosting);
		}
		return docList;
	}

	//Since, vocabList is sorted, we use binarySearch approach
	private long searchPosting(String term) {
		int i = 0, j = (vocabTable.length / 2) - 1;
		while (i <= j) {
			try {
				int mid = (i + j) / 2;
				long vListPosition = vocabTable[mid * 2];
				int n;
				if (mid == vocabTable.length / 2 - 1) {
					n = (int) (vocabListStream.length() - vocabTable[mid * 2]);
				} else {
					n = (int) (vocabTable[(mid + 1) * 2] - vListPosition);
				}

				vocabListStream.seek(vListPosition);

				byte[] buffer = new byte[n];
				vocabListStream.read(buffer, 0, n);
				String fileTerm = new String(buffer, "ASCII");

				int compareValue = term.compareTo(fileTerm);
				if (compareValue == 0) {
					return vocabTable[mid * 2 + 1];
				} else if (compareValue < 0) {
					j = mid - 1;
				} else {
					i = mid + 1;
				}
			} catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
		return -1;
	}

}
