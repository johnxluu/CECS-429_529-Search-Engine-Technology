package cecs429.utils;

import java.io.File;

/**
 *  All common utility methods will be moved to this class
 *
 */
public class AppUtils {

	public static int getCorpusSize(String path) {
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

}

