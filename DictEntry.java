package invertedIndex;
public class DictEntry {

    public int doc_freq = 0; // number of documents that contain the term
    public int term_freq = 0; //number of times the term is mentioned in the collection
//=====================================================================
    //public HashSet<Integer> postingList;
    Posting pList = null;
    Posting last = null;
//------------------------------------------------

    boolean postingListContains(int i) {
        boolean found = false;
        Posting p = pList;
        while (p != null) {
            if (p.docId == i) {
                return true;
            }
            p = p.next;
        }
        return found;
    }
//------------------------------------------------

    int getPosting(int i) {
        int found = 0;
        Posting p = pList;
        while (p != null) {
            if (p.docId >= i) {
                if (p.docId == i) {
                    return p.dtf;
                } else {
                    return 0;
                }
            }
            p = p.next;
        }
        return found;
    }
//------------------------------------------------

    void addPosting(int i) {
        // pList = new Posting(i);
        if (pList == null) {
            pList = new Posting(i);
            last = pList;
        } else {
            last.next = new Posting(i);
            last = last.next;
        }
    }
// implement insert (int docId) method


    void insert(int docId) {
        // Check if the posting list already contains the docId
        if (!postingListContains(docId)) {
            // If not, add a new posting with the docId
            addPosting(docId);
            // Increment the document frequency since this is a new document for the term
            doc_freq++;
        }
        // Increment the term frequency for each occurrence of the term
        term_freq++;
    }


    DictEntry() {
        //  postingList = new HashSet<Integer>();
    }

    DictEntry(int df, int tf) {
        doc_freq = df; 
        term_freq = tf;
    }

}
