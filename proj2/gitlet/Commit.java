package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Represents a gitlet commit object.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  This class represents a Commit that will be stored in a file.
 *  Because each commit has a unique hash ID, we will use it as
 *  the name of the file which the commit object serialized to.
 *  All commit objects are serialized within the COMMIT_FOLDER under the GITLET_FOLDER.
 *  The first two characters of their hash ID will be used as the subdirectory,
 *  to enhance time and space efficiency.
 *  It has helper methods to read the commit object from files given its hash ID,
 *  and write the commit object into a file to persistent its change.
 *  The commits will be a directed acyclic graph, the parentsID works as reference.
 *
 *  @author Li Yanzhuo
 */
public class Commit implements Serializable {
    /**
     * add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    static final File COMMIT_DIR = Utils.join(Repository.GITLET_DIR, "commits"); // the file path
    private String message;
    private String hashID;
    private String firstParentID;
    private String secondParentID;
    private long timestamp;
    private Map<String, String> files = new HashMap<>(); // store the filename and blobs

    /* TODO: fill in the rest of this class. */

    /**Methods that a commit class should have:
     * 1.constructor
     * 2.write a commit into a file under its subdir
     * 3.read a commit from file */


    /**This method is the constructor for the initial commit(???). */
    Commit(String message) {
        this.message = message;
        // init commit
        if (message.equals("initial commit") && Utils.isEmptyDirectory(COMMIT_DIR)) {
            this.timestamp = 0L;
            this.hashID = this.generateHashID();
        } else {
            throw Utils.error("This constructor is solely for initial commit. " +
                    "More information about this commit needed.");
        }
    }

    /**Persistence: a method that writes the commit object into file,
     * in the subdirectory by its first 2 id numbers - Hash Table. */
    void writeCommitIntoFile() {
        if (this.hashID == null || hashID.length() < 2) {
            throw Utils.error("HashID of the commit is shorter than 2.");
        }
        String firstTwoID = this.hashID.substring(0,2);
        File subDir = Utils.join(COMMIT_DIR, firstTwoID);
        subDir.mkdir(); // mkdir() will check whether the dir exists
        File commit_FILE = Utils.join(subDir, this.hashID); // the commit file use hashID as file name
        if (commit_FILE.exists()) {
            throw Utils.error("Same commit file already exists.");
        }
        Utils.writeObject(commit_FILE, this);
    }

    /* Return the hashID of the commit.*/
    String getCommitID() {
        return this.hashID;
    }

    /**Persistence: Given the commit ID, this method returns the commit object
     * read from the files. It enters the subdirectory first, as the commits
     * distribute as in a hash table. */
    static Commit readCommitFromFile(String commitID) {
        String firstTwoID = commitID.substring(0,2);
        File subDir = Utils.join(COMMIT_DIR, firstTwoID);
        if (subDir.exists() && subDir.isDirectory()) {
            File commit_FILE = Utils.join(subDir, commitID);
            Commit commit = Utils.readObject(commit_FILE, Commit.class);
            return commit;
        } else {
            throw Utils.error("Can't find the subdirectory of the commit: "+ commitID);
        }
    }

    /* This method checks whether the fileName-blob is already tracked by the commit. */
    boolean containsBlob(String fileName, String blobID) {
        String trackedBlob = this.files.get(fileName);
        return trackedBlob != null && (blobID.equals(trackedBlob));
    }


    /**A helper method that generate the hashID of a commit.
     * files, timestamp, message distinguish commits from each other. */
    private String generateHashID() {
        String timestampStr = String.valueOf(timestamp);
        String filesStr = getBlobsinString();
        String HashID = Utils.sha1(this.message, timestampStr, filesStr);
        return HashID;
    }

    private String getBlobsinString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : this.files.entrySet()) {
            String filename = entry.getKey();
            String hashID = entry.getValue();
            sb.append(filename).append(":").append(hashID).append(";");
        }
        return sb.toString();
    }

}
