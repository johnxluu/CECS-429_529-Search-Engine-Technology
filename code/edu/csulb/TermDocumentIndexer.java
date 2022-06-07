package edu.csulb;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.indexes.Index;
import cecs429.indexes.PositionalInvertedIndex;
import cecs429.indexes.Posting;
import cecs429.queries.BooleanQueryParser;
import cecs429.text.CustomTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class TermDocumentIndexer {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter Corpus Directory");
		String corpusDirectory = sc.nextLine();
		// Create a DocumentCorpus to load documents from the project directory.
		// F:\CECS429_529\corpus
		processIndexingAndSearch(corpusDirectory);
		sc.close();
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
				for (Posting p : postings) {
					System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
				}
//				}
				System.out.println(postings.size());
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
