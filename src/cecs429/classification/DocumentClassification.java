package cecs429.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.indexes.DiskIndexWriter;
import cecs429.indexes.DiskPositionalIndex;
import cecs429.indexes.Index;
import cecs429.indexes.PositionalIndexPosting;
import cecs429.indexes.PositionalInvertedIndex;
import cecs429.text.CustomTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.utils.AppConstants;
import cecs429.utils.AppUtils;
import edu.csulb.TermDocumentIndexer;

public class DocumentClassification {

	private DiskPositionalIndex jayDiskPositionalIndex;
	private DiskPositionalIndex hamDiskPositionalIndex;
	private DiskPositionalIndex madDiskPositionalIndex;
	private DiskPositionalIndex disputeDiskPositionalIndex;
	private DiskPositionalIndex comprehensiveDiskPositionalIndex;
	private double jayCount = 0;
	private double hamCount = 0;
	private double madCount = 0;
	private double totalCount = 0;
	private String basePath;
	Set<String> compVocabList;
	Set<String> jayVocabList;
	Set<String> hamVocabList;
	Set<String> madVocabList;

	public void startDiskIndexing() throws IOException {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Corpus Directory");
		basePath = sc.nextLine();
		if (!basePath.endsWith(File.separator)) {
			basePath += File.separator;
		}
		long startTime = System.currentTimeMillis();
		jayVocabList = writeDiskIndexes(basePath + AppConstants.JAY_PATH);
		hamVocabList = writeDiskIndexes(basePath + AppConstants.HAMILTON_PATH);
		madVocabList = writeDiskIndexes(basePath + AppConstants.MADISON_PATH);
		writeDiskIndexes(basePath + AppConstants.DISPUTED_PATH);

		// Complete list of all files from Jay, Hamilton, Madison
		compVocabList = writeDiskIndexes(basePath + AppConstants.COMPREHENSIVE_PATH);

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time taken to build disk Index: " + totalTime + " milliseconds");

		jayCount = getDocCount(basePath + AppConstants.JAY_PATH);
		hamCount = getDocCount(basePath + AppConstants.HAMILTON_PATH);
		madCount = getDocCount(basePath + AppConstants.MADISON_PATH);
		totalCount = jayCount + hamCount + madCount;

		try {
			jayDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.JAY_PATH);
			hamDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.HAMILTON_PATH);
			madDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.MADISON_PATH);
			disputeDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.DISPUTED_PATH);
			comprehensiveDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.COMPREHENSIVE_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}

		bayesianClassifer();
	}

	private int getDocCount(String path) {
		// Subtracting 1 for index folder
		return (new File(path).listFiles().length) - 1;
	}

	private Set<String> writeDiskIndexes(String path) {
		Set<String> localVocabSet = new HashSet<>();
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(path).toAbsolutePath(), ".txt");
		System.out.println("Corpus Indexing started...");
		Index index = TermDocumentIndexer.indexCorpus(corpus, ".txt");
		DiskIndexWriter diw = new DiskIndexWriter();
		System.out.println("Building disk index...");
		try {
			localVocabSet.addAll(diw.writeVocabList((PositionalInvertedIndex) index, path));
			diw.writeIndex((PositionalInvertedIndex) index, path);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return localVocabSet;
	}

	public void bayesianClassifer() throws IOException {
		int tStar = comprehensiveDiskPositionalIndex.getVocabSize();
		List<String> discVocabList = getDiscriminatingSet();
		Map<Integer, Map<String, Double>> probMap = new HashMap<>();

		// Class Id: 1 for ham, 2 for Jay, 3 for Madison
		prepareTermProbabilityMap(discVocabList, hamDiskPositionalIndex, probMap, 1);
		prepareTermProbabilityMap(discVocabList, jayDiskPositionalIndex, probMap, 2);
		prepareTermProbabilityMap(discVocabList, madDiskPositionalIndex, probMap, 3);


		DocumentCorpus corpus = DirectoryCorpus
				.loadTextDirectory(Paths.get(basePath + AppConstants.DISPUTED_PATH).toAbsolutePath(), ".txt");
		CustomTokenProcessor customTokenProcessor = new CustomTokenProcessor();
		List<String> authorNamesList = Arrays.asList("Hamilton","Jay","Madison");
		for (Document d : corpus.getDocuments()) {
			double[] maxDoubleArr =new double[1]; 
			maxDoubleArr[0]=Double.NEGATIVE_INFINITY;
			int[] finalClassId=new int[1];
			finalClassId[0]=1;
			Set<String> docTermSet = new HashSet<>();
			EnglishTokenStream es = new EnglishTokenStream(d.getContent());
			for (String token : es.getTokens()) {
				docTermSet.addAll(customTokenProcessor.processToken(token));
			}
			classifyDisputedDoc(docTermSet, probMap, hamDiskPositionalIndex, hamCount, maxDoubleArr, 1, finalClassId);
			
			classifyDisputedDoc(docTermSet, probMap, jayDiskPositionalIndex, jayCount, maxDoubleArr, 2, finalClassId);
			classifyDisputedDoc(docTermSet, probMap, madDiskPositionalIndex, madCount, maxDoubleArr, 3, finalClassId);
			System.out.println("Doc Name: "+d.getTitle()+" belongs to author "+authorNamesList.get(finalClassId[0]-1));
		}

	}

	private void classifyDisputedDoc(Set<String> disputedDocTerms, Map<Integer, Map<String, Double>> probMap,
			DiskPositionalIndex hamDiskPositionalIndex2, double classCorpusCount, double[] max, int classId, int[] finalClassId) {
		Map<String, Double> classProbMap = probMap.get(classId);
		
		// log(p(c))
		double probInClass = Math.log10(classCorpusCount / totalCount);
		double temp = 0;
		
		for(String t:disputedDocTerms) {
			if(classProbMap.containsKey(t)) {
				temp+= Math.log10(classProbMap.get(t));
			}
		}
		probInClass+=temp;
//		max = Math.max(max, probInClass);
		
		if(max[0] <probInClass) {
			max [0]= probInClass;
			finalClassId[0] = classId;
		}
	}

	private Map<Integer, Map<String, Double>> prepareTermProbabilityMap(List<String> discVocabList,
			DiskPositionalIndex classDiskPositionalIndex, Map<Integer, Map<String, Double>> probMap, int classId)
			throws IOException {
		// It'll be 50
		double discVocabSize = discVocabList.size();
		double termFreq = 0;
		for (String discTerm : discVocabList) {
			if (CollectionUtils.isNotEmpty(classDiskPositionalIndex.getPositionIndexPostings(discTerm))) {
				for (PositionalIndexPosting pos : classDiskPositionalIndex.getPositionIndexPostings(discTerm)) {
					termFreq += pos.getPositions().size();
				}
			}
		}
		Map<String, Double> innerMap = new HashMap<>();
		for (String discTerm : discVocabList) {
			double termFreqNumerator = 0;
			if (CollectionUtils.isNotEmpty(classDiskPositionalIndex.getPositionIndexPostings(discTerm))) {
				for (PositionalIndexPosting pos : classDiskPositionalIndex.getPositionIndexPostings(discTerm)) {
					termFreqNumerator += pos.getPositions().size();
				}
			}
			double prob = (termFreqNumerator + 1) / (termFreq + discVocabSize);
			innerMap.put(discTerm, prob);
		}
		probMap.put(classId, innerMap);
		return probMap;
	}

	/*
	 * I(C,T) N_11 -> No. of docs with term N_X1 -> No. of docs with term (don't
	 * consider class) N_01 -> No of docs with term but not in this class N_10 -> No
	 * of docs without this term (same class) N_00 -> No. of docs Neither in class
	 * nor contains the term
	 *
	 * 
	 */
	private List<String> getDiscriminatingSet() throws IOException {
		List<String> discVocabList = new ArrayList<>();
		List<MutualInformation> termIctList = new ArrayList<>();

		// To get top K terms, use heap
		PriorityQueue<MutualInformation> miPQ = new PriorityQueue<>();

		calcIctForAuthor(miPQ, hamVocabList, hamDiskPositionalIndex, hamCount);
		calcIctForAuthor(miPQ, jayVocabList, jayDiskPositionalIndex, jayCount);
		calcIctForAuthor(miPQ, madVocabList, madDiskPositionalIndex, madCount);

//		calcHamIct(miPQ);
//		calcJayIct(miPQ);
//		calcMadIct(miPQ);

		Set<MutualInformation> tempTopMiList = new HashSet<>();
		// Top 10 discriminating terms
		int i = 0;
		while (i < 50 && !miPQ.isEmpty()) {
			MutualInformation mInformation = miPQ.poll();
			if (!discVocabList.contains(mInformation.getTerm())) {
				discVocabList.add(mInformation.getTerm());
				tempTopMiList.add(mInformation);
			} else
				i--;
			i++;
		}
		System.out.println("=== Top 10 terms by I(T, C) ===");
		for (i = 0; i < 10; i++) {
			System.out.println(discVocabList.get(i));
		}

		return discVocabList;
	}

	private void calcIctForAuthor(PriorityQueue<MutualInformation> mi, Set<String> classVocabSet,
			DiskPositionalIndex classDiskPositionalIndex, double classCorpusCount) throws IOException {
		double n_1x = classCorpusCount;
		for (String eachTerm : classVocabSet) {
			double n_11 = classDiskPositionalIndex.getPositionIndexPostings(eachTerm).size();
			double n_x1 = comprehensiveDiskPositionalIndex.getPositionIndexPostings(eachTerm).size();
			double n_01 = n_x1 - n_11;
			double n_10 = n_1x - n_11;
			double n_00 = totalCount - (n_11 + n_01 + n_10);

			try {
				double itc = (n_11 / totalCount) * AppUtils.log2((totalCount * n_11) / ((n_1x * n_x1)))
						+ (n_01 / totalCount) * AppUtils.log2((totalCount * n_01) / ((n_01 + n_00) * n_x1))
						+ (n_10 / totalCount) * AppUtils.log2((totalCount * n_10) / (n_1x * (n_00 + n_10)))
						+ (n_00 / totalCount) * AppUtils.log2((totalCount * n_00) / ((n_01 + n_00) * (n_00 + n_10)));
				if (Double.isNaN(itc)) {
					mi.add(new MutualInformation(eachTerm, 0));
				} else {
					mi.add(new MutualInformation(eachTerm, itc));
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

}
