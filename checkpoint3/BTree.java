import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;

/**
 * B+Tree Structure
 * Key - StudentId
 * Leaf Node should contain [ key,recordId ]
 */
class BTree {

    /**
     * Pointer to the root node.
     */
    private BTreeNode root;
    /**
     * Number of key-value pairs allowed in the tree/the minimum degree of B+Tree
     **/
    private int t;

    BTree(int t) {
        this.root = null;
        this.t = t;
    }

    long search(long studentId) {
        /**
         * TODO:
         * Implement this function to search in the B+Tree.
         * Return recordID for the given StudentID.
         * Otherwise, print out a message that the given studentId has not been found in
         * the table and return -1.
         */
        return -1;
    }

    BTree insert(Student student, boolean writeToFile) {

        // ideally, we should check if the student already exists
        // but for now, we'll just insert it and add it to the csv

        // empty tree
        if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = student.studentId;
            root.values[0] = student.recordId;
            root.n = 1;
            return this;
        }

        // if root is full - split and make a new root
        if (root.n == t * 2) {
            BTreeNode newRoot = new BTreeNode(t, false);
            newRoot.children[0] = root;

            // now we split!
            BTreeNode newChild = new BTreeNode(t, root.leaf);

            // copy the last t keys and values to the new child
            for (int i = 0; i < t; i++) {
                newChild.keys[i] = root.keys[t + i];

                if (root.leaf) {
                    newChild.values[i] = root.values[t + i];
                }
            }

            // if we have an internal node, move the child pointers too
            if (!root.leaf) {
                for (int i = 0; i <= t; i++) {
                    newChild.children[i] = root.children[t + i];
                }
            } else {
                newChild.next = root.next; // maintain the leaf nodes
                root.next = newChild; // link the new child to the root
            }

            root.n = t;
            newRoot.children[1] = newChild;
            newRoot.keys[0] = root.keys[0];
            newRoot.n = 1;

            root = newRoot;
        }

        // we have space in the root? move to leaves!
        BTreeNode currentNode = root;
        while (!currentNode.leaf) {
            int i = currentNode.n - 1;
            while (i >= 0 && student.studentId < currentNode.keys[i]) {
                i--;
            }
            i++;

            // is the child full?
            if (currentNode.children[i].n == t * 2) {
                // once again, split!
                BTreeNode newChild = new BTreeNode(t, currentNode.children[i].leaf);
                for (int j = 0; j < t; j++) {
                    newChild.keys[j] = currentNode.children[i].keys[t + j];

                    if (currentNode.children[i].leaf) {
                        newChild.values[j] = currentNode.children[i].values[t + j];
                    }
                }
                if (!currentNode.children[i].leaf) {
                    for (int j = 0; j <= t; j++) {
                        newChild.children[j] = currentNode.children[i].children[t + j];
                    }
                } else {
                    newChild.next = currentNode.children[i].next; // maintain the leaf nodes
                    currentNode.children[i].next = newChild; // link the new child to the root
                }

                currentNode.children[i].n = t;

                for (int k = currentNode.n; k >= i + 1; k--) {
                    currentNode.children[k + 1] = currentNode.children[k];
                }
                currentNode.children[i + 1] = newChild;
                for (int k = currentNode.n - 1; k >= i; k--) {
                    currentNode.keys[k + 1] = currentNode.keys[k];
                }

                currentNode.keys[i] = currentNode.children[i].keys[0];
                currentNode.n++;

                if (student.studentId > currentNode.keys[i]) {
                    i++;
                }
            }
            currentNode = currentNode.children[i];
        }

        // we have space in the leaf! finally, we can insert!
        int i = currentNode.n - 1;
        while (i >= 0 && currentNode.keys[i] > student.studentId) {
            currentNode.keys[i + 1] = currentNode.keys[i];
            currentNode.values[i + 1] = currentNode.values[i];
            i--;
        }
        currentNode.keys[i + 1] = student.studentId;
        currentNode.values[i + 1] = student.recordId;
        currentNode.n++;

        // don't forget to add to the table :,)
        if (writeToFile) {
            try (FileWriter fw = new FileWriter("./Student.csv", true)) {
                fw.write(student.studentId + "," + student.studentName + "," +
                        student.major + "," + student.level + "," +
                        student.age + "," + student.recordId + "\n");
            } catch (Exception e) {
                System.err.println("Error writing to Student.csv: " + e.getMessage());
            }
        }

        return this;
    }

    boolean delete(long studentId) {
        /**
         * TODO:
         * Implement this function to delete in the B+Tree.
         * Also, delete in student.csv after deleting in B+Tree, if it exists.
         * Return true if the student is deleted successfully otherwise, return false.
         */
        return true;
    }

    List<Long> print() {

        List<Long> listOfRecordID = new ArrayList<>();

        if (root == null) {
            return listOfRecordID;
        }

        // start from the leftmost leaf (well, find it first)
        BTreeNode currentNode = root;
        while (!currentNode.leaf) {
            currentNode = currentNode.children[0];
        }

        // add all values!
        while (currentNode != null) {
            for (int i = 0; i < currentNode.n; i++) {
                listOfRecordID.add(currentNode.values[i]);
            }
            currentNode = currentNode.next;
        }

        return listOfRecordID;
    }
}
