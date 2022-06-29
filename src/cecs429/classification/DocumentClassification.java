package cecs429.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private double disputedCount = 0;
	private String basePath;
	Set<String> compVocabList;
	Set<String> jayVocabList;
	Set<String> hamVocabList;
	Set<String> madVocabList;
	Set<String> disputedVocabList;
	ArrayList<String> sortedCompList;
	List<Integer> disputedDocIds;
	List<String> disputedDocNames;
	List<Integer> hamDocIds;
	List<String> hamDocNames;
	List<Integer> jayDocIds;
	List<String> jayDocNames;
	List<Integer> madDocIds;
	List<String> madDocNames;


	public void startDiskIndexing(int option) throws IOException {
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
		disputedVocabList = writeDiskIndexes(basePath + AppConstants.DISPUTED_PATH);
		writeDiskIndexes(basePath + AppConstants.DISPUTED_PATH);

		// Complete list of all files from Jay, Hamilton, Madison
		compVocabList = writeDiskIndexes(basePath + AppConstants.COMPREHENSIVE_PATH);

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time taken to build disk Index: " + totalTime + " milliseconds");

		jayCount = getDocCount(basePath + AppConstants.JAY_PATH);
		hamCount = getDocCount(basePath + AppConstants.HAMILTON_PATH);
		madCount = getDocCount(basePath + AppConstants.MADISON_PATH);
		disputedCount = getDocCount(basePath + AppConstants.DISPUTED_PATH);
		totalCount = jayCount + hamCount + madCount;

		try {
			jayDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.JAY_PATH);
			hamDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.HAMILTON_PATH);
			madDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.MADISON_PATH);
			disputeDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.DISPUTED_PATH);
			comprehensiveDiskPositionalIndex = new DiskPositionalIndex(basePath + AppConstants.COMPREHENSIVE_PATH);
			sortedCompList = new ArrayList<String>(compVocabList);
			sortedCompList.addAll(new ArrayList<String>(disputedVocabList));
			Collections.sort(sortedCompList);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (option == 1) {
			bayesianClassifer();
		} else if (option == 2) {
			rocchioClassification();
		} else if (option == 3) {
			System.out.println("Enter K value");
			int k = sc.nextInt();
			knn(k);

		}

	}

	private int getDocCount(String path) {
		// Subtracting 1 for index folder
		return (new File(path).listFiles().length) - 1;
	}

	private Set<String> writeDiskIndexes(String path) {
		Set<String> localVocabSet = new HashSet<>();
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(path).toAbsolutePath(), ".txt");
		prepareDocIdsAndNames(path, corpus);
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

	private void prepareDocIdsAndNames(String path, DocumentCorpus corpus) {
		if (path.contains(AppConstants.DISPUTED_PATH)) {
			disputedDocIds = new ArrayList<>();
			disputedDocNames = new ArrayList<>();
			for (Document d : corpus.getDocuments()) {
				disputedDocIds.add(d.getId());
				disputedDocNames.add(d.getTitle());
			}
		} else if (path.contains(AppConstants.HAMILTON_PATH)) {
			hamDocIds = new ArrayList<>();
			hamDocNames = new ArrayList<>();
			for (Document d : corpus.getDocuments()) {
				hamDocIds.add(d.getId());
				hamDocNames.add(d.getTitle());
			}
		} else if (path.contains(AppConstants.JAY_PATH)) {
			jayDocIds = new ArrayList<>();
			jayDocNames = new ArrayList<>();
			for (Document d : corpus.getDocuments()) {
				jayDocIds.add(d.getId());
				jayDocNames.add(d.getTitle());
			}
		} else if (path.contains(AppConstants.MADISON_PATH)) {
			madDocIds = new ArrayList<>();
			madDocNames = new ArrayList<>();
			for (Document d : corpus.getDocuments()) {
				madDocIds.add(d.getId());
				madDocNames.add(d.getTitle());
			}
		}
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
		List<String> authorNamesList = Arrays.asList("Hamilton", "Jay", "Madison");
		for (Document d : corpus.getDocuments()) {
			double[] maxDoubleArr = new double[1];
			maxDoubleArr[0] = Double.NEGATIVE_INFINITY;
			int[] finalClassId = new int[1];
			finalClassId[0] = 1;
			Set<String> docTermSet = new HashSet<>();
			EnglishTokenStream es = new EnglishTokenStream(d.getContent());
			for (String token : es.getTokens()) {
				docTermSet.addAll(customTokenProcessor.processToken(token));
			}
			classifyDisputedDoc(docTermSet, probMap, hamDiskPositionalIndex, hamCount, maxDoubleArr, 1, finalClassId);

			classifyDisputedDoc(docTermSet, probMap, jayDiskPositionalIndex, jayCount, maxDoubleArr, 2, finalClassId);
			classifyDisputedDoc(docTermSet, probMap, madDiskPositionalIndex, madCount, maxDoubleArr, 3, finalClassId);
			System.out.println(
					"Doc Name: " + d.getTitle() + " belongs to author " + authorNamesList.get(finalClassId[0] - 1));
		}

	}

	private void classifyDisputedDoc(Set<String> disputedDocTerms, Map<Integer, Map<String, Double>> probMap,
			DiskPositionalIndex hamDiskPositionalIndex2, double classCorpusCount, double[] max, int classId,
			int[] finalClassId) {
		Map<String, Double> classProbMap = probMap.get(classId);

		// log(p(c))
		double probInClass = Math.log10(classCorpusCount / totalCount);
		double temp = 0;

		for (String t : disputedDocTerms) {
			if (classProbMap.containsKey(t)) {
				temp += Math.log10(classProbMap.get(t));
			}
		}
		probInClass += temp;
//		max = Math.max(max, probInClass);

		if (max[0] < probInClass) {
			max[0] = probInClass;
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

		List<MutualInformation> tempTopMiList = new ArrayList<>();
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
		System.out.println("==============================================");
		System.out.println("=== Top 10 terms by I(C, T) ===");
		for (i = 0; i < 10; i++) {
			System.out.println(
					"Term: " + tempTopMiList.get(i).getTerm() + " ==> Score: " + tempTopMiList.get(i).getIct());
		}
		System.out.println("==============================================");
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

	public void rocchioClassification() throws IOException {
		ArrayList<Double> jayVector = getCentroid(jayDiskPositionalIndex, jayVocabList, jayCount);
		ArrayList<Double> hamVector = getCentroid(hamDiskPositionalIndex, hamVocabList, hamCount);
		ArrayList<Double> madVector = getCentroid(madDiskPositionalIndex, madVocabList, madCount);

		for (Integer disputedDocID : this.disputedDocIds) {
			ArrayList<Double> disputedDocVector = new ArrayList<Double>(
					Collections.nCopies(this.sortedCompList.size(), 0.0));
			for (String term : disputedVocabList) {
				double LD = 0;
				double sumOFWDT = 0;
				for (PositionalIndexPosting dp : disputeDiskPositionalIndex.getPositionIndexPostings(term)) {
					if (dp.getDocumentId() == disputedDocID) {
						LD = disputeDiskPositionalIndex.getDocWeight(disputedDocID);
						sumOFWDT += dp.getWdt() / LD;
						disputedDocVector.set(sortedCompList.indexOf(term), sumOFWDT);
						break;
					}

				}

			}
			if (disputedDocID == 4) {
				for (int i = 0; i < 20; i++) {
					System.out.println("=======");
					System.out.println(disputedDocVector.get(i));
					System.out.println("=======");
				}
			}
			double hamEucledianDistance = getEucledianDistance(hamVector, disputedDocVector);
			double madEucledianDistance = getEucledianDistance(madVector, disputedDocVector);
			double jayEucledianDistance = getEucledianDistance(jayVector, disputedDocVector);
			System.out.println(
					"Dist to hamilton for doc: " + disputedDocNames.get(disputedDocID) + " is " + hamEucledianDistance);
			System.out.println(
					"Dist to madison for doc: " + disputedDocNames.get(disputedDocID) + " is " + madEucledianDistance);
			System.out.println(
					"Dist to jay for doc: " + disputedDocNames.get(disputedDocID) + " is " + jayEucledianDistance);
			findMinDistance(hamEucledianDistance, madEucledianDistance, jayEucledianDistance);
		}

	}

	private void findMinDistance(double hamEucledianDistance, double madEucledianDistance,
			double jayEucledianDistance) {
		String s = "";
		if (hamEucledianDistance < jayEucledianDistance && hamEucledianDistance < madEucledianDistance) {
			// ham
			s = "/hamilton";
		} else if (jayEucledianDistance < madEucledianDistance) {
			// jay
			s = "/jay";
		} else {
			// mad
			s = "/madison";
		}
		System.out.println("Low distance for paper is: " + s);

	}

	public double getEucledianDistance(ArrayList<Double> p1, ArrayList<Double> p2) {
		double distance = 0;
		for (int i = 0; i < p1.size(); i++) {
			distance += Math.pow(p1.get(i) - p2.get(i), 2);
		}
		return Math.sqrt(distance);
	}

	public ArrayList<Double> getCentroid(DiskPositionalIndex dpi, Set<String> termsSet, double docsCount)
			throws IOException {
		ArrayList<Double> vector = new ArrayList<Double>(Collections.nCopies(this.sortedCompList.size(), 0.0));
		double LD = 0;
		double sumOFWDT = 0;
		for (String term : termsSet) {
			LD = 0.0;
			sumOFWDT = 0.0;
			for (PositionalIndexPosting pi : dpi.getPositionIndexPostings(term)) {
				LD = dpi.getDocWeight(pi.getDocumentId());
				sumOFWDT += pi.getWdt() / (LD);

			}
			int index = this.sortedCompList.indexOf(term);
			vector.set(index, sumOFWDT / docsCount);
		}

		return vector;

	}

	public void knn(int k) throws IOException {
		ArrayList<ArrayList<Double>> allVectors = new ArrayList<ArrayList<Double>>();
		ArrayList<String> fileNames = new ArrayList<String>();

		allVectors.addAll(addVector(hamDocIds, hamDiskPositionalIndex, hamVocabList, fileNames, hamDocNames));

		allVectors.addAll(addVector(jayDocIds, jayDiskPositionalIndex, jayVocabList, fileNames, jayDocNames));
		allVectors.addAll(addVector(madDocIds, madDiskPositionalIndex, madVocabList, fileNames, madDocNames));

		ArrayList<ArrayList<Double>> allDisputedList = addVector(disputedDocIds, disputeDiskPositionalIndex,
				disputedVocabList, fileNames, disputedDocNames);
		int index = 0;
		for (ArrayList<Double> disputedVector : allDisputedList) {
			ArrayList<Double> distances = new ArrayList<Double>();
			ArrayList<String> fileNamesCopy = new ArrayList<>(fileNames);
			for (ArrayList<Double> vector : allVectors) {
				distances.add(getEucledianDistance(disputedVector, vector));
			}

			// sort the distance and file names based on distances
			for (int i = 0; i < distances.size(); i++) {
				for (int j = i + 1; j < distances.size(); j++) {
					if (distances.get(i) > distances.get(j)) {
						double temp = distances.get(i);
						distances.set(i, distances.get(j));
						distances.set(j, temp);
						// swap names
						String tempFileName = fileNamesCopy.get(i);
						fileNamesCopy.set(i, fileNamesCopy.get(j));
						fileNamesCopy.set(j, tempFileName);
					}
				}
			}
			System.out.println("==================");
			System.out.println(disputedDocNames.get(index));
			for (int i = 0; i < k; i++) {
				System.out.println(distances.get(i) + "    " + fileNamesCopy.get(i));
			}
			index++;
		}

	}

	public ArrayList<ArrayList<Double>> addVector(List<Integer> docIDs, DiskPositionalIndex dpi, Set<String> termSet,
			ArrayList<String> fileNames, List<String> classNameList) throws IOException {

		ArrayList<ArrayList<Double>> vectorsList = new ArrayList<ArrayList<Double>>();

		for (Integer docID : docIDs) {
			ArrayList<Double> vector = new ArrayList<Double>(Collections.nCopies(sortedCompList.size(), 0.0));
			for (String term : termSet) {
				double LD = 0;
				double sumOFWDT = 0;
				for (PositionalIndexPosting dp : dpi.getPositionIndexPostings(term)) {
					if (dp.getDocumentId() == docID) {
						LD = dpi.getDocWeight(docID);
						sumOFWDT += dp.getWdt() / LD;
						vector.set(sortedCompList.indexOf(term), sumOFWDT);
						break;
					}
				}

			}
			vectorsList.add(vector);
			fileNames.add(classNameList.get(docID));

		}
		return vectorsList;
	}


}
