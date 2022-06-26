package cecs429.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.indexes.DiskIndexWriter;
import cecs429.indexes.DiskPositionalIndex;
import cecs429.indexes.Index;
import cecs429.indexes.PositionalInvertedIndex;
import cecs429.utils.AppConstants;
import edu.csulb.TermDocumentIndexer;

public class DocumentClassification {
	
	private DiskPositionalIndex jayDiskPositionalIndex;
	private DiskPositionalIndex hamDiskPositionalIndex;
	private DiskPositionalIndex madDiskPositionalIndex;
	private DiskPositionalIndex disputeDiskPositionalIndex;
	private DiskPositionalIndex comprehensiveDiskPositionalIndex;
	private int jayCount = 0;
	private int hamCount = 0;
	private int madCount = 0;
	private int totalCount = 0;
	
	public void startDiskIndexing() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Corpus Directory");
		String basePath = sc.nextLine();
		if(!basePath.endsWith(File.separator)) {
			basePath+=File.separator;
		}
		writeDiskIndexes(basePath+AppConstants.JAY_PATH);
		writeDiskIndexes(basePath+AppConstants.HAMILTON_PATH);
		writeDiskIndexes(basePath+AppConstants.MADISON_PATH);
		writeDiskIndexes(basePath+AppConstants.DISPUTED_PATH);
		
		// Complete list of all files from Jay, Hamilton, Madison
		writeDiskIndexes(basePath+AppConstants.COMPREHENSIVE_PATH);
		
		jayCount = getDocCount(basePath+AppConstants.JAY_PATH);
		hamCount = getDocCount(basePath+AppConstants.HAMILTON_PATH);
		madCount = getDocCount(basePath+AppConstants.MADISON_PATH);
		totalCount = jayCount+hamCount+madCount;
		
		try {
			jayDiskPositionalIndex = new DiskPositionalIndex(basePath+AppConstants.JAY_PATH);
			hamDiskPositionalIndex = new DiskPositionalIndex(basePath+AppConstants.HAMILTON_PATH);
			madDiskPositionalIndex = new DiskPositionalIndex(basePath+AppConstants.MADISON_PATH);
			disputeDiskPositionalIndex = new DiskPositionalIndex(basePath+AppConstants.DISPUTED_PATH);
			comprehensiveDiskPositionalIndex = new DiskPositionalIndex(basePath+AppConstants.COMPREHENSIVE_PATH);
			comprehensiveDiskPositionalIndex.getVocabulary();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		bayesianClassifer();
	}
	
	private int getDocCount(String path) {
		return new File(path).listFiles().length;
	}

	private void writeDiskIndexes(String path) {
		
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(path).toAbsolutePath(), ".txt");
		System.out.println("Corpus Indexing started...");
		Index index = TermDocumentIndexer.indexCorpus(corpus, ".txt");
		DiskIndexWriter diw = new DiskIndexWriter();
		System.out.println("Building disk index...");
		try {
			long startTime = System.currentTimeMillis();
			diw.writeVocabList((PositionalInvertedIndex) index, path);
			diw.writeIndex((PositionalInvertedIndex) index, path);
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Time taken to build disk Index: " + totalTime + " milliseconds");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void bayesianClassifer() {
		int tStar=comprehensiveDiskPositionalIndex.getVocabSize();
	}
	
	public void mutualInformation() {
		
	}
}
