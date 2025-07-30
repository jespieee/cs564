import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

/**
 * Main Application.
 */
public class BTreeMain {

    public static void main(String[] args) {

        /** Read the input file -- input.txt */
        Scanner scan = null;
        try {
            scan = new Scanner(new File("input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }

        /** Read the minimum degree of B+Tree first */

        int degree = scan.nextInt();

        BTree bTree = new BTree(degree);

        /** Reading the database student.csv into B+Tree Node */
        List<Student> studentsDB = getStudents();

        for (Student s : studentsDB) {
            bTree.insert(s, false);
        }
        // Printing tree for visual help troubleshooting with debug method
        //System.out.println("Initial tree:");
        //bTree.debugPrint();

        /** Start reading the operations now from input file */
        try {
            while (scan.hasNextLine()) {
                Scanner s2 = new Scanner(scan.nextLine());

                while (s2.hasNext()) {

                    String operation = s2.next();

                    switch (operation) {
                        case "insert": {

                            long studentId = Long.parseLong(s2.next());
                            String studentName = s2.next() + " " + s2.next();
                            String major = s2.next();
                            String level = s2.next();
                            int age = Integer.parseInt(s2.next());

                            // On Piazza Mark confirmed the approach of using 0 to random max int
                            long recordID;
                            if (s2.hasNext()) {
                                recordID = Long.parseLong(s2.next());
                            }
                            else {
                                Random rand = new Random();
                                recordID = rand.nextInt(Integer.MAX_VALUE);
                            }

                            Student s = new Student(studentId, age, studentName, major, level, recordID);
                            bTree.insert(s, true);
                            System.out.println(bTree.print());
                            break;
                        }
                        case "delete": {
                            long studentId = Long.parseLong(s2.next());

                            // Printing tree before delte
                            //System.out.println("Before attempting to delete " + studentId + ":");
                            //bTree.debugPrint();

                            boolean result = bTree.delete(studentId);
                            if (result)
                                System.out.println("Student deleted successfully.");
                            else
                                System.out.println("Student deletion failed.");

                            // Printing tree after delete
                            //System.out.println("After attempting to delete " + studentId + ":");
                            //bTree.debugPrint();

                            break;
                        }
                        case "search": {
                            long studentId = Long.parseLong(s2.next());
                            long recordID = bTree.search(studentId);
                            if (recordID != -1)
                                System.out.println("Student exists in the database at " + recordID);
                            else
                                System.out.println("Student does not exist.");
                            break;
                        }
                        case "print": {
                            List<Long> listOfRecordID = new ArrayList<>();
                            listOfRecordID = bTree.print();
                            System.out.println("List of recordIDs in B+Tree " + listOfRecordID.toString());
                        }
                        default:
                            System.out.println("Wrong Operation");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Student> getStudents() {

        List<Student> studentList = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File("Student.csv"))) {
            while (scanner.hasNextLine()) {
                // Remove trailing/leading spaces and skip blank lines
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split(",");
                if (tokens.length < 6) {
                    System.out.println("Skipping malformed line: " + line);
                    continue; // skip malformed lines
                }
                // Adding in try-catch for debugging in case we alter CSV unexpectedly
                try {
                    // Updated to use single token for name - CSV treats these as one token
                    // rather than two tokens split with a space.
                    long studentId = Long.parseLong(tokens[0].trim());
                    String studentName = tokens[1].trim();            // Name
                    String major = tokens[2].trim();                  // Major
                    String level = tokens[3].trim();                  // Level (FR, SO, JR, SR)
                    int age = Integer.parseInt(tokens[4].trim());     // Age
                    long recordID = Long.parseLong(tokens[5].trim()); // RecordID

                    Student student = new Student(studentId, age, studentName, major, level, recordID);
                    studentList.add(student);
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing line: " + line + " - " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Student.csv file not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("There was an error reading the Student.csv file: " + e.getMessage());
        }

        // Confirming expected number of students were loaded
        System.out.println("Loaded " + studentList.size() + " students from CSV");
        return studentList;
    }
}
