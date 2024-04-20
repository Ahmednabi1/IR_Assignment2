package invertedIndex;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {

    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();
        //|**  change it to your collection directory
        String files = "D:/fcai/Third Year/Second Semester/Information Retrieval/Assignments/Assignment 2/ASS2/tmp11/rl/collection/";

        java.io.File file = new java.io.File(files);

        String[] fileList = file.list();

        fileList = index.sort(fileList);
        index.setN(fileList.length);

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }
        index.buildIndex(fileList);
        index.printDictionary();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String option;
        do {
            System.out.println("Choose search method:");
            System.out.println("1: single words only");
            System.out.println("2: bi-words included");
            System.out.println("0: Exit");
            System.out.print("Enter option: ");
            option = in.readLine();
            switch (option) {
                case "1":
                    searchUsingIntersect(index, in);
                    break;
                case "2":
                    searchWithBiWord(index, in);
                    break;
                case "0":
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        } while (!option.equals("0"));
    }

    private static void searchUsingIntersect(Index5 index, BufferedReader in) throws IOException {
        String phrase;
        do {
            System.out.println("Enter search phrase [or type 'exit' to return to main menu]:");
            phrase = in.readLine();
            if (!phrase.equalsIgnoreCase("exit")) {
                String searchResult = index.find_24_01(phrase);
                System.out.println("Search result = \n" + searchResult);
            }
        } while (!phrase.equalsIgnoreCase("exit"));
    }

    private static void searchWithBiWord(Index5 index, BufferedReader in) throws IOException {
        String phrase;
        do {
            System.out.println("Enter search phrase [or type 'exit' to return to main menu]:");
            phrase = in.readLine();
            if (!phrase.equalsIgnoreCase("exit")) {
                String searchResult = index.find(phrase);
                System.out.println("Search result = \n" + searchResult);
            }
        } while (!phrase.equalsIgnoreCase("exit"));
    }
}
