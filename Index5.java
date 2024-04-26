package invertedIndex;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.*;
public class Index5 {

    int N = 0;
    public Map<Integer, SourceRecord> sources;
    public HashMap<String, DictEntry> index;

    public Index5() {
        sources = new HashMap<>();
        index = new HashMap<>();
    }

    public void setN(int n) {
        N = n;
    }
    //---------------------------------------------
    public void printPostingList(Posting p) {
        System.out.print("[");
        boolean firstItem = true;
        while (p != null) {
            if (!firstItem) {
                System.out.print(", ");
            }
            System.out.print(p.docId);
//            for(int i = 0; i < p.positions.size(); i++)
//            {
//                System.out.println(p.positions.get(i) + ", ");
//            }

            p = p.next;
            firstItem = false;
        }
        System.out.println("]");
    }

    //---------------------------------------------
    public void buildPositionalIndex(String[] files)
    {
        int fid = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fid)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, ""));
                }
                String ln;
                int flen = 0;
                while ((ln = file.readLine()) != null) {
                    flen = positionalIndex(ln , fid , flen);
                }
                sources.get(fid).length = flen;


            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
        printDictionary();
    }

    //---------------------------------------------
    public int positionalIndex(String ln , int fid , int flen)
    {
        String[] words = ln.split("\\W+"); // Split the line into words
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            flen += 1;
            if (stopWord(word)) {
                continue; // Skip stop words
            }
            word = stemWord(word);
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;

                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
                index.get(word).pList.positions.add(flen);
            }
            else{
                index.get(word).last.dtf += 1;
                index.get(word).pList.positions.add(flen);
            }
            index.get(word).term_freq += 1;
        }
        return flen;


    }

    //---------------------------------------------
    public ArrayList<Posting> positionalIntersect(Posting p1 , Posting p2)
    {
        ArrayList<Posting> answer = new ArrayList<Posting>();
        Posting a = null;
        while(p1 != null && p2 != null)
        {
            if(p1.docId == p2.docId)
            {

                var pp1 = p1.positions;
                var pp2 = p2.positions;
                if(pp1 != null && pp2 != null)
                {
                    for (Integer first : pp1) {
                        for (Integer second : pp2) {
                            if ((second - first) == 1) {
                                a = new Posting(p1.docId);
                                a.positions.add(first);
                                a.positions.add(second);
                                answer.add(a);
                            } else if (second > first) {
                                break;
                            }
                        }
                    }
                }

            }
            else if(p1.docId < p2.docId)
            {
                p1 = p1.next;
            }
            else{
                p2 = p2.next;
            }
            p1 = p1.next;
            p2 = p2.next;

        }

        return answer;
    }

    //---------------------------------------------
    public String findWithPosition(String query)
    {
        String result = "";
        String[] words = query.split("\\W+");
        if(words.length == 1)
        {
            String singleWord = words[0].toLowerCase();
            if(index.containsKey(singleWord)) {
                Posting posting = index.get(singleWord).pList;

                while (posting != null) {
                    result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
                    posting = posting.next;
                }
            }
        }
        else{
            ArrayList<ArrayList<Posting>> allPostings = new ArrayList<  >();
            for(int i = 0; i < words.length - 1; i++) {
                Posting p1 = index.get(words[i]).pList;
                Posting p2 = index.get(words[i + 1]).pList;

                allPostings.add(positionalIntersect(p1, p2));

            }
            for(int i = 0; i < allPostings.size(); i++)
            {
                for(int j = 0; j < allPostings.get(i).size(); j++)
                {
                    result += "\t" + allPostings.get(i).get(j).docId + " - " + sources.get(allPostings.get(i).get(j).docId).title + " - " + sources.get(allPostings.get(i).get(j).docId).length + "\n";
                }
            }

        }
        return result;
    }

    //---------------------------------------------
    public void printDictionary() {
        Iterator<Map.Entry<String, DictEntry>> it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DictEntry> pair = it.next();
            String term = pair.getKey();
            DictEntry dd = pair.getValue();
            System.out.print("** [" + term + "," + dd.doc_freq + "] =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }

    //---------------------------------------------
    public void buildIndex(String[] files) {
        int fid = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fid)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, ""));
                }
                String ln;
                int flen = 0;
                while ((ln = file.readLine()) != null) {
                    flen += indexOneLine(ln, fid);
                }
                sources.get(fid).length = flen;

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
        printDictionary();
    }

    //---------------------------------------------
    public int indexOneLine(String ln, int fid) {
        int flen = 0;
        String[] words = removeStopWords(ln).split("\\W+"); // Split the cleaned line into words
        flen += words.length;

        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            word = stemWord(word); // Stem the word
            // Index single word
            indexTerm(word, fid);

            // Index biword combination
            if (i < words.length - 1) {
                String biWord = word + "_" + words[i + 1].toLowerCase();
                indexTerm(biWord, fid);
            }
        }
        return flen;
    }


    //---------------------------------------------
    boolean stopWord(String word) {
        return word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")
                || word.length() < 2;
    }

    //---------------------------------------------
    /**
     * Stem a word using a Stemmer (not implemented in this code snippet).
     * @param word The word to be stemmed.
     * @return The stemmed word.
     */
    String stemWord(String word) { //skip for now
        return word; // Return the word as is (no stemming applied)
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //---------------------------------------------
    /**
     * Find the intersection of two posting lists.
     * @param pL1 The first posting list.
     * @param pL2 The second posting list.
     * @return The intersection of the two posting lists.
     */
    public Posting intersect(Posting pL1, Posting pL2) {
        Posting answer = null;
        Posting lastAnswer = null;

        while (pL1 != null && pL2 != null) {
            if (pL1.docId == pL2.docId) {
                if (answer == null) {
                    answer = new Posting(pL1.docId);
                    lastAnswer = answer;
                } else {
                    lastAnswer.next = new Posting(pL1.docId);
                    lastAnswer = lastAnswer.next;
                }
                pL1 = pL1.next;
                pL2 = pL2.next;
            } else if (pL1.docId < pL2.docId) {
                pL1 = pL1.next;
            } else {
                pL2 = pL2.next;
            }
        }
        return answer;
    }

    //---------------------------------------------
    /**
     * Find documents containing all terms in the given phrase.
     * @param phrase The search phrase containing multiple terms.
     * @return A string containing documents matching the search criteria.
     */
    public String find_24_01(String phrase) {
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        Posting posting = index.get(words[0].toLowerCase()).pList;
        int i = 1;
        // Iterate through the remaining words in the phrase to find the intersection of posting lists
        while (i < len) {
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }
        // Append document information to the result string
        while (posting != null) {
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next; // Move to the next node in the posting list
        }
        return result; // Return the result string containing matching documents
    }

    //---------------------------------------------
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false; // Initialize the sorted flag
        String sTmp; // Temporary variable for string swapping

        // Continue sorting until the array is fully sorted
        if(words != null) {
            while (!sorted) {
                sorted = true; // Assume array is sorted unless proven otherwise
                for (int i = 0; i < words.length - 1; i++) {
                    int compare = words[i].compareTo(words[i + 1]); // Compare adjacent strings
                    if (compare > 0) { // If comparison indicates out-of-order, swap strings
                        sTmp = words[i];
                        words[i] = words[i + 1];
                        words[i + 1] = sTmp;
                        sorted = false; // Set sorted flag to false since swap occurred
                    }
                }
            }
        }
        return words; // Return the sorted array
    }

    //---------------------------------------------
    private void indexTerm(String term, int fid) {

        if (!index.containsKey(term)) {
            index.put(term, new DictEntry());
        }

        DictEntry entry = index.get(term);

        if (!entry.postingListContains(fid)) {
            entry.doc_freq++;
            entry.addPosting(fid);
        }
        entry.term_freq++;
    }

    //---------------------------------------------
    public String findSingleWord(String word) {
        StringBuilder result = new StringBuilder();

        DictEntry entry = index.get(word.toLowerCase());
        if (entry != null) {
            Posting posting = entry.pList;
            while (posting != null) {
                result.append("\t").append(sources.get(posting.docId).title).append(" - ").append(sources.get(posting.docId).length).append("\n");
                posting = posting.next;
            }
        }
        return result.toString();
    }

    //---------------------------------------------
    public String removeStopWords(String phrase) {
        StringBuilder cleanedPhrase = new StringBuilder();
        String[] parts = phrase.split("\"");

        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 0) {
                String[] words = parts[i].split("\\s+");
                for (String word : words) {
                    if (!stopWord(word)) {
                        cleanedPhrase.append(word).append(" ");
                    }
                }
            } else {
                cleanedPhrase.append("\"").append(removeStopWordsInsideQuotes(parts[i])).append("\" ");
            }
        }

        return cleanedPhrase.toString().trim();
    }
    //---------------------------------------------
    public String removeStopWordsInsideQuotes(String phrase) {
        StringBuilder cleanedPhrase = new StringBuilder();
        String[] words = phrase.split("\\s+");
        for (String word : words) {
            if (!stopWord(word)) {
                cleanedPhrase.append(word).append(" ");
            }
        }
        return cleanedPhrase.toString().trim();
    }
    //---------------------------------------------
    public String find(String phrase) {
        StringBuilder result = new StringBuilder();
        String cleanedPhrase = removeStopWords(phrase);

        if (cleanedPhrase.contains("\"")) {
            String[] parts = cleanedPhrase.split("\"");

            // search for single words outside the quotation marks
            String[] singleWords = parts[0].trim().toLowerCase().split("\\s+");
            for (String word : singleWords) {
                result.append("Word '").append(word).append("' found in document:\n");
                result.append(findSingleWord(word)).append("\n");
            }

            // Search for biwords inside the quotation marks
            String insideQuotes = parts[1].trim().toLowerCase();
            String[] words = insideQuotes.split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                String biWord = words[i] + "_" + words[i + 1];
                result.append("Biword '").append(biWord).append("' found in document:\n");
                result.append(findSingleWord(biWord)).append("\n");
            }
        } else {
            // If no quotation marks, search for biwords
            String[] words = cleanedPhrase.trim().toLowerCase().split("\\s+");
            for (int i = 0; i < words.length - 1; i++) {
                String biWord = words[i] + "_" + words[i + 1];
                result.append("Biword '").append(biWord).append("' found in document:\n");
                result.append(findSingleWord(biWord)).append("\n");
            }
        }

        return result.toString();
    }
    //---------------------------------------------
//=====================================================================








    /**
     * Checks if the storage file exists.
     * @param storageName The name of the storage file.
     * @return True if the storage file exists, false otherwise.
     */
    public boolean storageFileExists(String storageName){
        // Create a new File object with the specified storage path and file name
        java.io.File f = new java.io.File("F:/College/3rd - 2nd term/IR/IR_Assignment2/collection/"+storageName);
        // Check if the file exists and is not a directory
        if (f.exists() && !f.isDirectory())
            return true; // Return true if the file exists
        return false; // Return false if the file does not exist or is a directory
    }

    //----------------------------------------------------
    /**
     * Creates a new storage file with the specified name.
     * @param storageName The name of the storage file to be created.
     */
    public void createStore(String storageName) {
        try {
            // Create the full path to the storage file
            String pathToStorage = "F:/College/3rd - 2nd term/IR/IR_Assignment2/collection/"+storageName;
            // Create a new FileWriter object for writing to the storage file
            Writer wr = new FileWriter(pathToStorage);
            // Write "end" to the file and then close it
            wr.write("end" + "\n");
            wr.close();
        } catch (Exception e) {
            // Print stack trace if an exception occurs during file creation
            e.printStackTrace();
        }
    }

    //----------------------------------------------------
    /**
     * Loads the index from the specified storage file into memory.
     * @param storageName The name of the storage file containing the index.
     * @return The loaded index as a HashMap of String keys to DictEntry values.
     */
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            // Create the full path to the storage file
            String pathToStorage = "D:/fcai/Third Year/Second Semester/Information Retrieval/Assignments/Assignment 2/ASS2/tmp11/rl/collection/"+storageName;
            // Initialize sources and index HashMaps
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            // Create a BufferedReader to read from the storage file
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            // Read lines from the file until "section2" is encountered or end of file
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break; // Exit the loop if "section2" is found
                }
                // Split the line by comma
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]); // Parse the first element as integer
                try {
                    // Create a new SourceRecord object from the line's components
                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    // Add the SourceRecord to the sources HashMap
                    sources.put(fid, sr);
                } catch (Exception e) {
                    // Print error message and stack trace if an exception occurs
                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // Continue reading lines for index data until "end" is encountered or end of file
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("end")) {
                    break; // Exit the loop if "end" is found
                }
                // Split the line by semicolon
                String[] ss1 = ln.split(";");
                // Split the first part of the line by comma
                String[] ss1a = ss1[0].split(",");
                // Split the second part of the line by colon
                String[] ss1b = ss1[1].split(":");
                // Create a new DictEntry object from the first part's components
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                // Process the postings data and update the index accordingly
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
        } catch (Exception e) {
            // Print stack trace if an exception occurs during file loading
            e.printStackTrace();
        }
        // Return the loaded index HashMap
        return index;
    }

}

//=====================================================================
