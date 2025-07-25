import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main Application.
 */
public class BTreeMain {

    public static void main(String[] args) {

        /** Read the input file -- input.txt */
        Scanner scan = null;
        try {
            scan = new Scanner(new File("./input.txt"));
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
                            /**
                             * TODO: Write a logic to generate recordID if it is not provided
                             * If it is provided, use the provided value
                             */
                            long recordID = 41644; // TODO someone implement this logic

                            Student s = new Student(studentId, age, studentName, major, level, recordID);
                            bTree.insert(s, true);
                            System.out.println(bTree.print());
                            break;
                        }
                        case "delete": {
                            long studentId = Long.parseLong(s2.next());
                            boolean result = bTree.delete(studentId);
                            if (result)
                                System.out.println("Student deleted successfully.");
                            else
                                System.out.println("Student deletion failed.");

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
                String line = scanner.nextLine();
                String[] tokens = line.split(",");
                if (tokens.length < 6)
                    continue; // skip malformed lines

                long studentId = Long.parseLong(tokens[0].trim());
                String studentName = tokens[1].trim() + " " + tokens[2].trim();
                String major = tokens[3].trim();
                String level = tokens[4].trim();
                int age = Integer.parseInt(tokens[5].trim());
                long recordID = tokens.length > 6 ? Long.parseLong(tokens[6].trim()) : -1;

                Student student = new Student(studentId, age, studentName, major, level, recordID);
                studentList.add(student);
            }
        } catch (Exception e) {
            System.out.println("There was an error reading the Student.csv file: " + e.getMessage());
        }

        return studentList;
    }
}
