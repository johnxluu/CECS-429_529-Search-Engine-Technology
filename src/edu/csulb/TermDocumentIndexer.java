package edu.csulb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.indexes.DiskIndexWriter;
import cecs429.indexes.Index;
import cecs429.indexes.PositionalInvertedIndex;
import cecs429.indexes.Posting;
import cecs429.queries.BooleanQueryParser;
import cecs429.text.CustomTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class TermDocumentIndexer {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Select the options:");
		System.out.println("1 for Milestone1");
		System.out.println("2 for Milestone2");
		String option = sc.nextLine();
		if(option.equalsIgnoreCase("1")) milestone1();
		else if(option.equalsIgnoreCase("2")) milestone2();
		else System.out.println("Invalid entry. Exiting the application");
		sc.close();
	}

	private static void milestone2() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Select the options:");
		System.out.println("1. To build a disk index");
		System.out.println("2. Query on the existing disk index");
		String op = sc.nextLine();
		if(op.equalsIgnoreCase("1")) {
			String corpusDir = readFromCorpus(sc);
			String fileType = getFileExtension(corpusDir);
			DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusDir).toAbsolutePath(),
					fileType);
			System.out.println("Corpus Indexing started...");
			Index index = indexCorpus(corpus, fileType);
			DiskIndexWriter diw = new DiskIndexWriter();
			System.out.println("Building disk index...");
			
			try {
				long startTime = System.currentTimeMillis();
				diw.writeIndex((PositionalInvertedIndex) index, corpusDir);
				long endTime = System.currentTimeMillis();
				long totalTime = endTime - startTime;
				System.out.println("Time taken to build disk Index: " + totalTime + " milliseconds");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(op.equalsIgnoreCase("2")) {
			
		} else {
			System.out.println("Invalid entry. Exiting the application");
		}
	}

	private static void milestone1() {
		Scanner sc = new Scanner(System.in);
		// Create a DocumentCorpus to load documents from the project directory.
		// F:\CECS429_529\corpus
		processIndexingAndSearch(readFromCorpus(sc));
	}

	private static String readFromCorpus(Scanner sc) {
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
		CustomTokenProcessor customTokenProcessor = new CustomTokenProcessor();
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
				if (CollectionUtils.isNotEmpty(postings)) {
					for (Posting p : postings) {
						System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
					}
				} else {
					postings = new ArrayList<>();
				}
//				}
				System.out.println("For Query ( " +query+ " ) Output Size: "+postings.size());
			}
			System.out.println("Want to search a new query? (Y/N)");
			String newQuery = scanner.nextLine();
			if (newQuery.equalsIgnoreCase("N")) {
				shouldEnd = false;
			}
		}
	}

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
				System.out.println("Stemmed term: "+tk.getStem(splQuery[1]));
		} else if(splQuery.length == 2 && splQuery[0].equalsIgnoreCase(":index")) {
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
				position++;
			}
		}
		System.out.println("Index Processing Ended");
		return index;
	}
}
