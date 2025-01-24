package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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


    /**Methods that a commit class should have:
     * 1.constructor
     * 2.write a commit into a file under its subdir
     * 3.read a commit from file */


    /**This method is the constructor for the initial commit. */
    Commit(String message) {
        this.message = message;
        // init commit
        if (message.equals("initial commit") && Utils.isEmptyDirectory(COMMIT_DIR)) {
            this.timestamp = 0L;
            this.hashID = this.generateHashID();
        } else {
            System.out.println("This constructor is solely for initial commit. " +
                    "More information about this commit needed.");
            System.exit(0);
        }
    }

    /* Constructor for normal commits. */
    Commit(String message, Commit curCommit) {
        this.message = message;
        this.timestamp = Instant.now().getEpochSecond();
        this.firstParentID = curCommit.hashID;

        this.files = new HashMap<>(curCommit.files); // shallow copy, for kv are String
        HashMap<String,String> stagingFiles = Repository.readObjectFromIndex();
        if (stagingFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        for (Map.Entry<String, String> entry: stagingFiles.entrySet()) {
            if (entry.getValue().equals("REMOVE")) {
                this.files.remove(entry.getKey());
            } else {
                this.files.put(entry.getKey(), entry.getValue());
            }
        }

        this.hashID = generateHashID();
    }

    /* Return the hashID of the commit.*/
    String getCommitID() {
        return this.hashID;
    }

    HashMap<String, String> getFiles() {
        HashMap<String, String> files = new HashMap<>(this.files);
        return files;
    }

    String getFirstParentID() {
        return this.firstParentID;
    }

    String getSecondParentID() {
        return this.secondParentID;
    }

    String getMessage() {
        return this.message;
    }

    String getLog() {
        String entry = "===";
        String commitID = "commit " + this.hashID;
        String merge = "";
        if (this.secondParentID != null) {
            String firstParentID_short = firstParentID.substring(0,6);
            String secondParentID_short = secondParentID.substring(0,6);
            merge = "Merge: " + firstParentID_short + " " + secondParentID_short;
        }
        String date = this.formatTimeStamp();

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(entry).append("\n")
                .append(commitID).append("\n");
        if (!merge.isEmpty()) {
            logBuilder.append(merge).append("\n");
        }
        logBuilder.append(date).append("\n")
                .append(this.message).append("\n\n");

        return logBuilder.toString();
    }

    /**Persistence: a method that writes the commit object into file,
     * in the subdirectory by its first 2 id numbers - Hash Table. */
    void save() {
        if (this.hashID == null || hashID.length() < 2) {
           System.out.println("HashID of the commit is shorter than 2.");
           System.exit(0);
        }
        String firstTwoID = this.hashID.substring(0,2);
        File subDir = Utils.join(COMMIT_DIR, firstTwoID);
        subDir.mkdir(); // mkdir() will check whether the dir exists
        File commit_FILE = Utils.join(subDir, this.hashID); // the commit file use hashID as file name
        if (commit_FILE.exists()) {
            System.out.println("Same commit file already exists.");
            System.exit(0);
        }
        Utils.writeObject(commit_FILE, this);
    }

    /**Persistence: Given the commit ID, this method returns the commit object
     * read from the files. It enters the subdirectory first, as the commits
     * distribute as in a hash table. */
    static Commit load(String commitID) {
        String firstTwoID = commitID.substring(0,2);
        File subDir = Utils.join(COMMIT_DIR, firstTwoID);
        if (subDir.exists() && subDir.isDirectory()) {
            File commit_FILE = Utils.join(subDir, commitID);
            Commit commit = Utils.readObject(commit_FILE, Commit.class);
            return commit;
        } else {
            System.out.println("No commit with that id exists.");
            System.exit(0);
            return null;
        }
    }

    /* This method checks whether the fileName-blob is already tracked by the commit. */
    boolean containsBlob(String fileName, String blobID) {
        String trackedBlob = this.files.get(fileName);
        return trackedBlob != null && (blobID.equals(trackedBlob));
    }

    String formatTimeStamp() {
        ZonedDateTime zonedDateTime = Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy Z");
        return "Date: " + zonedDateTime.format(formatter);
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
