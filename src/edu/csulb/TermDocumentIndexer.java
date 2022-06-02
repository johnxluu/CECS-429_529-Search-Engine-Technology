package edu.csulb;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.indexes.Index;
import cecs429.indexes.InvertedIndex;
import cecs429.indexes.Posting;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class TermDocumentIndexer {
	public static void main(String[] args) {
		BasicTokenProcessor processor = new BasicTokenProcessor();
		Scanner sc = new Scanner(System.in);  // Create a Scanner object
	    System.out.println("Enter Corpus Directory");
	    String corpusDirectory = sc.nextLine();  // Read user input
	    Boolean shouldEnd = true;
	    String fileType;
		fileType = getFileExtension(corpusDirectory);
		// Create a DocumentCorpus to load .txt documents from the project directory.
	    // F:\CECS429_529\corpus
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(corpusDirectory).toAbsolutePath(), fileType);
		// Index the documents of the corpus.
		long startTime = System.currentTimeMillis();
		Index index = indexCorpus(corpus);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time taken to index: "+totalTime + " milliseconds");
		
		while(shouldEnd) {
			// We aren't ready to use a full query parser; for now, we'll only support single-term queries.
			System.out.println("Enter query to search");
		    String query = sc.nextLine(); 
		    
		    System.out.println("Query: ("+ query +") found in the following documents");
			for (Posting p : index.getPostings(query.toLowerCase())) {
				System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
			}
			System.out.println("Want to search a new query? (Y/N)");
			String newQuery = sc.nextLine();
				if(newQuery.equalsIgnoreCase("N")) {
					shouldEnd = false;
				}
		}
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
	
	private static Index indexCorpus(DocumentCorpus corpus) {
		HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		InvertedIndex index = new InvertedIndex();
		// First, build the vocabulary hash set.
		System.out.println("Index Processing started for "+corpus.getCorpusSize()+" Documents");

		for (Document d : corpus.getDocuments()) {
			// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
			// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
			// and adding them to the HashSet vocabulary.
			JsonFileDocument jsonDoc = new JsonFileDocument(d.getId(), d.getFilePath());
			EnglishTokenStream es = new EnglishTokenStream(jsonDoc.getContent());
			
			for(String token:es.getTokens()) {
				String term = processor.processToken(token);
				index.addTerm(term,d.getId());
			}
		}
		System.out.println("Index Processing Ended");
		// Constuct a TermDocumentMatrix once you know the size of the vocabulary.
		// THEN, do the loop again! But instead of inserting into the HashSet, add terms to the index with addPosting.
		return index;
	}
}
