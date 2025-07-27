import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // Karlson, feel free to check/change this as needed.
    // I implemented it for testing delete, not sure if it handles all scenarios.
    long search(long studentId) {
        if (root == null) {
            System.out.println("The given studentId " + studentId + " has not been found in the table");
            return -1;
        }

        BTreeNode current = root;

        // Navigate to leaf node
        while (!current.leaf) {
            int i = 0;
            while (i < current.n && studentId > current.keys[i]) {
                i++;
            }
            current = current.children[i];
        }

        // Search in leaf node
        for (int i = 0; i < current.n; i++) {
            if (current.keys[i] == studentId) {
                return current.values[i];
            }
        }

        System.out.println("The given studentId " + studentId + " has not been found in the table");
        return -1;
    }

    // Helper method for searching with no output
    private long searchQuiet(long studentId) {
        if (root == null) {
            return -1;
        }

        BTreeNode current = root;

        // Navigate to leaf node
        while (!current.leaf) {
            int i = 0;
            while (i < current.n && studentId > current.keys[i]) {
                i++;
            }
            current = current.children[i];
        }

        // Search in leaf node
        for (int i = 0; i < current.n; i++) {
            if (current.keys[i] == studentId) {
                return current.values[i];
            }
        }

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
        if (root == null) {
            return false;
        }

        // Check if key exists without printing message
        long recordId = searchQuiet(studentId);
        if (recordId == -1) {
            return false;
        }

        delete(root, studentId);

        // If root becomes empty, make its first child as new root
        if (root.n == 0) {
            if (!root.leaf) {
                root = root.children[0];
            } else {
                root = null;
            }
        }

        // Delete from CSV
        deleteFromCSV(studentId);
        return true;
    }

    private void delete(BTreeNode node, long key) {
        int i = 0;
        while (i < node.n && key > node.keys[i]) {
            i++;
        }

        if (node.leaf) {
            // Case: node is a leaf
            if (i < node.n && node.keys[i] == key) {
                // Remove the key from leaf
                for (int j = i; j < node.n - 1; j++) {
                    node.keys[j] = node.keys[j + 1];
                    node.values[j] = node.values[j + 1];
                }
                node.n--;
            }
        } else {
            // Case: node is internal
            if (i < node.n && node.keys[i] == key) {
                // Key found in internal node
                deleteFromInternal(node, key, i);
            } else {
                // Key not found in internal node, go to appropriate child
                boolean isLastChild = (i == node.n);

                if (node.children[i].n < t) {
                    fill(node, i);
                }

                if (isLastChild && i > node.n) {
                    delete(node.children[i - 1], key);
                } else {
                    delete(node.children[i], key);
                }
            }
        }
    }

    private void deleteFromInternal(BTreeNode node, long key, int index) {
        if (node.children[index].n >= t) {
            // Get predecessor
            long predecessor = getPredecessor(node, index);
            node.keys[index] = predecessor;
            delete(node.children[index], predecessor);
        } else if (node.children[index + 1].n >= t) {
            // Get successor
            long successor = getSuccessor(node, index);
            node.keys[index] = successor;
            delete(node.children[index + 1], successor);
        } else {
            // Merge with sibling
            merge(node, index);
            delete(node.children[index], key);
        }
    }

    private long getPredecessor(BTreeNode node, int index) {
        BTreeNode current = node.children[index];
        while (!current.leaf) {
            current = current.children[current.n];
        }
        return current.keys[current.n - 1];
    }

    private long getSuccessor(BTreeNode node, int index) {
        BTreeNode current = node.children[index + 1];
        while (!current.leaf) {
            current = current.children[0];
        }
        return current.keys[0];
    }

    private void fill(BTreeNode node, int index) {
        // If previous sibling has more than t-1 keys, borrow from it
        if (index != 0 && node.children[index - 1].n >= t) {
            borrowFromPrev(node, index);
        }
        // If next sibling has more than t-1 keys, borrow from it
        else if (index != node.n && node.children[index + 1].n >= t) {
            borrowFromNext(node, index);
        }
        // If both siblings have t-1 keys, merge with sibling
        else {
            if (index != node.n) {
                merge(node, index);
            } else {
                merge(node, index - 1);
            }
        }
    }

    private void borrowFromPrev(BTreeNode node, int index) {
        BTreeNode child = node.children[index];
        BTreeNode sibling = node.children[index - 1];

        // Shift all keys and values in child to make room
        for (int i = child.n - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
            if (child.leaf) {
                child.values[i + 1] = child.values[i];
            }
        }

        if (!child.leaf) {
            // Shift child pointers
            for (int i = child.n; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
            // Move the rightmost child from sibling
            child.children[0] = sibling.children[sibling.n];
            // The key to move down is the parent key
            child.keys[0] = node.keys[index - 1];
            // Update parent key to be the rightmost key from sibling
            node.keys[index - 1] = sibling.keys[sibling.n - 1];
        } else {
            // For leaf nodes: move actual data
            child.keys[0] = sibling.keys[sibling.n - 1];
            child.values[0] = sibling.values[sibling.n - 1];
            // Update parent routing key to be the new smallest key in child
            node.keys[index - 1] = child.keys[0];
        }

        child.n++;
        sibling.n--;
    }

    private void borrowFromNext(BTreeNode node, int index) {
        BTreeNode child = node.children[index];
        BTreeNode sibling = node.children[index + 1];

        if (!child.leaf) {
            // Move parent key down to child
            child.keys[child.n] = node.keys[index];
            // Move leftmost child pointer from sibling
            child.children[child.n + 1] = sibling.children[0];
            // Update parent key to be the leftmost key from sibling
            node.keys[index] = sibling.keys[0];

            // Shift sibling's keys and children left
            for (int i = 1; i < sibling.n; i++) {
                sibling.keys[i - 1] = sibling.keys[i];
            }
            for (int i = 1; i <= sibling.n; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        } else {
            // For leaf nodes: move actual data
            child.keys[child.n] = sibling.keys[0];
            child.values[child.n] = sibling.values[0];

            // Shift sibling's data left
            for (int i = 1; i < sibling.n; i++) {
                sibling.keys[i - 1] = sibling.keys[i];
                sibling.values[i - 1] = sibling.values[i];
            }

            // Update parent routing key to be the new smallest key in sibling
            node.keys[index] = sibling.keys[0];
        }

        child.n++;
        sibling.n--;
    }

    private void merge(BTreeNode node, int index) {
        BTreeNode child = node.children[index];
        BTreeNode sibling = node.children[index + 1];

        // For internal nodes, pull down the parent key
        if (!child.leaf) {
            child.keys[child.n] = node.keys[index];
            child.n++;
        }

        // Copy all keys and values from sibling to child
        for (int i = 0; i < sibling.n; i++) {
            child.keys[child.n + i] = sibling.keys[i];
            if (child.leaf) {
                child.values[child.n + i] = sibling.values[i];
            }
        }

        // Copy child pointers for internal nodes
        if (!child.leaf) {
            for (int i = 0; i <= sibling.n; i++) {
                child.children[child.n + i] = sibling.children[i];
            }
        } else {
            // For leaf nodes, update the next pointer
            child.next = sibling.next;
        }

        // Update child's count
        child.n += sibling.n;

        // Remove the key from parent
        for (int i = index + 1; i < node.n; i++) {
            node.keys[i - 1] = node.keys[i];
        }

        // Remove the child pointer from parent
        for (int i = index + 2; i <= node.n; i++) {
            node.children[i - 1] = node.children[i];
        }

        node.n--;
    }

    private void deleteFromCSV(long studentId) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("Student.csv"));
            List<String> updatedLines = new ArrayList<>();

            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length > 0 && !parts[0].equals(String.valueOf(studentId))) {
                    updatedLines.add(line);
                }
            }

            Files.write(Paths.get("Student.csv"), updatedLines);
        } catch (IOException e) {
            System.out.println("Error updating CSV: " + e.getMessage());
        }
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

    // Debug methods to print and verify tree structure
    public void debugPrint() {
        System.out.println("=== B+Tree Structure ===");
        if (root == null) {
            System.out.println("Empty tree");
            return;
        }
        debugPrintNode(root, 0);
        System.out.println("========================");
    }

    private void debugPrintNode(BTreeNode node, int level) {
        String indent = "  ".repeat(level);
        System.out.print(indent + "Node(" + (node.leaf ? "LEAF" : "INTERNAL") + "): ");
        for (int i = 0; i < node.n; i++) {
            System.out.print(node.keys[i]);
            if (node.leaf) {
                System.out.print("(" + node.values[i] + ")");
            }
            if (i < node.n - 1) System.out.print(", ");
        }
        System.out.println();

        if (!node.leaf) {
            for (int i = 0; i <= node.n; i++) {
                debugPrintNode(node.children[i], level + 1);
            }
        }
    }
}
