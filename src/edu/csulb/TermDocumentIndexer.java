package edu.csulb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;

import cecs429.classification.DocumentClassification;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.documents.RankedDocument;
import cecs429.indexes.DiskIndexWriter;
import cecs429.indexes.DiskPositionalIndex;
import cecs429.indexes.Index;
import cecs429.indexes.PositionalInvertedIndex;
import cecs429.indexes.Posting;
import cecs429.queries.BooleanQueryParser;
import cecs429.queries.QueryService;
import cecs429.text.CustomTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.utils.AppUtils;

public class TermDocumentIndexer {
	private DiskPositionalIndex diskPositionalIndex;
	private DocumentClassification documentClassification;

	public static void main(String[] args) {
		TermDocumentIndexer tdi = new TermDocumentIndexer();
		Scanner sc = new Scanner(System.in);
		System.out.println("Select the options:");
		System.out.println("1 for Milestone1");
		System.out.println("2 for Milestone2");
		System.out.println("3 for Milestone3");
		String option = sc.nextLine();
		if (option.equalsIgnoreCase("1"))
			tdi.milestone1();
		else if (option.equalsIgnoreCase("2"))
			tdi.milestone2();
		else if (option.equalsIgnoreCase("3"))
			tdi.milestone3();
		else
			System.out.println("Invalid entry. Exiting the application");
		sc.close();
	}

	private void milestone3() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Select the options:");
		System.out.println("1. Bayesian");
		System.out.println("2. Rocchio");
		System.out.println("3. kNN");
		String op = sc.nextLine();
		documentClassification = new DocumentClassification();
		int option = 0;
		if (op.equalsIgnoreCase("1")) {
			option = 1;

		} else if (op.equalsIgnoreCase("2")) {
			option = 2;
		} else if (op.equalsIgnoreCase("3")) {
			option = 3;
		}
		try {
			documentClassification.startDiskIndexing(option);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void milestone2() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Select the options:");
		System.out.println("1. To build a disk index");
		System.out.println("2. Query on the existing disk index");
		String op = sc.nextLine();
		if (op.equalsIgnoreCase("1")) {
			buildDiskIndex();
		} else if (op.equalsIgnoreCase("2")) {

			System.out.println("Select the options:");
			System.out.println("1. Boolean Query");
			System.out.println("2. Ranked Query");

			String op2 = sc.nextLine();
			String corpusDir = readFromCorpus();
			String fileType = getFileExtension(corpusDir);
			DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusDir).toAbsolutePath(), fileType);
			try {
				diskPositionalIndex = new DiskPositionalIndex(corpusDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (op2.equalsIgnoreCase("1")) {
				System.out.println("Enter a query");
				String querysc = sc.nextLine();
				List<Posting> postings = QueryService.processBooleanQueries(querysc, diskPositionalIndex);
				printQueryResults(corpus, querysc, postings);
			} else if (op2.equalsIgnoreCase("2")) {
				System.out.println("Enter a query");
				String querysc = sc.nextLine();
				List<RankedDocument> rankedPostings = new ArrayList<>();
				try {
					rankedPostings = QueryService.processRankedQueries(querysc, diskPositionalIndex,
							AppUtils.getCorpusSize(corpusDir));
				} catch (IOException e) {
					e.printStackTrace();
				}
				for (RankedDocument rd : rankedPostings) {
//					if (fileType.equalsIgnoreCase("json") || fileType.equalsIgnoreCase(".json")) {
//						
//						System.out.println("(Doc ID: " + rd.getDocumentId() + " Title: "
//								+ new JsonFileDocument(rd.getDocumentId(),
//										corpus.getDocument(rd.getDocumentId()).getFilePath()).getTitle()
//								+ " Accumulator: " + rd.getAccumulator() + " )");
//					} else {
					System.out.println(
							"(Doc ID: " + rd.getDocumentId() + " " + "Accumulator: " + rd.getAccumulator() + " )");
//					}
				}
				System.out.println("For Query ( " + querysc + " ) Output Size: " + rankedPostings.size());
			}

		} else {
			System.out.println("Invalid entry. Exiting the application");
		}
	}

	private void buildDiskIndex() {
		String corpusDir = readFromCorpus();
		String fileType = getFileExtension(corpusDir);
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusDir).toAbsolutePath(), fileType);
		System.out.println("Corpus Indexing started...");
		Index index = indexCorpus(corpus, fileType);
		DiskIndexWriter diw = new DiskIndexWriter();
		System.out.println("Building disk index...");
		try {
			long startTime = System.currentTimeMillis();
			diw.writeVocabList((PositionalInvertedIndex) index, corpusDir);
			diw.writeIndex((PositionalInvertedIndex) index, corpusDir);
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Time taken to build disk Index: " + totalTime + " milliseconds");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void milestone1() {
		processIndexingAndSearch(readFromCorpus());
	}

	// Create a DocumentCorpus to load documents from the project directory.
	// F:\CECS429_529\corpus
	private static String readFromCorpus() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Corpus Directory");
		String corpusDirectory = sc.nextLine();
		return corpusDirectory;
	}

	private static void processIndexingAndSearch(String corpusDirectory) {
		String fileType = getFileExtension(corpusDirectory);
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusDirectory).toAbsolutePath(),
				fileType);
		// Index the documents of the corpus.
		long startTime = System.currentTimeMillis();
		Index index = indexCorpus(corpus, fileType);
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time taken to index: " + totalTime + " milliseconds");
		searchQuery(corpus, index);
	}

	private static void searchQuery(DocumentCorpus corpus, Index index) {
		Boolean shouldEnd = true;
//		CustomTokenProcessor customTokenProcessor = new CustomTokenProcessor();
		Scanner scanner = new Scanner(System.in);
		while (shouldEnd) {
			// We aren't ready to use a full query parser; for now, we'll only support
			// single-term queries.
			System.out.println("Enter query to search");
			String query = scanner.nextLine();
			if (isSpecialQuery(query)) {
				processSpecialQuery(query, index);
			} else {
				System.out.println("Query: (" + query + ") found in the following documents");
//				for (String eachQuery : customTokenProcessor.processToken(query)) {
				BooleanQueryParser booleanQueryParser = new BooleanQueryParser();
				List<Posting> postings = booleanQueryParser.parseQuery(query).getPostings(index);
				printQueryResults(corpus, query, postings);
			}
			System.out.println("Want to search a new query? (Y/N)");
			String newQuery = scanner.nextLine();
			if (newQuery.equalsIgnoreCase("N")) {
				shouldEnd = false;
			}
		}
	}

	private static void printQueryResults(DocumentCorpus corpus, String query, List<Posting> postings) {
		if (CollectionUtils.isNotEmpty(postings)) {
			for (Posting p : postings) {
				System.out.println("Document " + p.getDocumentId());
			}
		} else {
			postings = new ArrayList<>();
		}
//				}
		System.out.println("For Query ( " + query + " ) Output Size: " + postings.size());
	}

	@SuppressWarnings({ "static-access" })
	private static void processSpecialQuery(String query, Index index) {
		String[] splQuery = query.split(" ");
		if (splQuery.length == 1 && splQuery[0].equalsIgnoreCase(":q")) {
			System.exit(0);
		} else if (splQuery.length == 1 && splQuery[0].equalsIgnoreCase(":vocab")) {
			List<String> vocabList = index.getVocabulary();
			int size = 1000;
			if (vocabList.size() < 1000)
				size = vocabList.size();
			IntStream.range(0, size).mapToObj(i -> vocabList.get(i)).forEach(System.out::println);
		} else if (splQuery.length == 2 && splQuery[0].equalsIgnoreCase(":stem")) {
			CustomTokenProcessor tk = new CustomTokenProcessor();
			System.out.println("Stemmed term: " + tk.getStem(splQuery[1]));
		} else if (splQuery.length == 2 && splQuery[0].equalsIgnoreCase(":index")) {
			processIndexingAndSearch(splQuery[1]);
		} else {
			System.out.println("Invalid Special Query");
		}
	}

	private static boolean isSpecialQuery(String query) {
		if (query.startsWith(":")) {
			return true;
		}
		return false;
	}

	public static String getFileExtension(String path) {
		String fileName = new String();
		File[] files = new File(path).listFiles();

		for (File file : files) {
			if (file.isFile()) {
				fileName = file.getName();
				break;
			}
		}
		if (fileName.isEmpty())
			return fileName;
		return fileName.split("\\.")[1];
	}

	public static Index indexCorpus(DocumentCorpus corpus, String fileExtension) {
		CustomTokenProcessor customTokenProcessor = new CustomTokenProcessor();
//		InvertedIndex index = new InvertedIndex();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		EnglishTokenStream es;
		System.out.println("Index Processing started for " + corpus.getCorpusSize() + " Documents");

		for (Document d : corpus.getDocuments()) {
			int position = 0;
			if (fileExtension.equalsIgnoreCase("json")) {
				JsonFileDocument jsonDoc = new JsonFileDocument(d.getId(), d.getFilePath());
				es = new EnglishTokenStream(jsonDoc.getContent());
			} else {
				es = new EnglishTokenStream(d.getContent());
			}
			for (String token : es.getTokens()) {

				List<String> term = customTokenProcessor.processToken(token);

				if (term.size() <= 1)
					index.addTerm(term.get(0), d.getId(), position);
				else {
					for (String eachTerm : term)
						index.addTerm(eachTerm, d.getId(), position);
				}
//				if(isIndexFull(index)) {
//					DiskIndexingAlgorithm.processSpimi(fileExtension, index);
//				}
				position++;
			}
		}
		System.out.println("Index Processing Ended");
		return index;
	}

//	private static boolean isIndexFull(PositionalInvertedIndex index) {
//		if(index)
//		return false;
//	}
}
