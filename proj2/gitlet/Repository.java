package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  This is where the main logic of our program lives.
 *  This class will handle all the gitlet commands through setting up persistence,
 *  reading/writing from/to the correct file, and additional error checking.
 *  It will create the ./gitlet folder and sub directories.
 *  This class defers all the Commit/Blob specific logic to Commit/Blob class,
 *  like serialization and deserialization.
 *
 *
 *
 *  @author Li Yanzhuo
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static File index_FILE = join(GITLET_DIR, "index");
    public static File commits_DIR = join(GITLET_DIR, "commits");
    public static File blobs_DIR = join(GITLET_DIR, "blobs");
    public static File refs_DIR = join(GITLET_DIR, "refs");
    public static File heads_DIR = join(refs_DIR, "heads");
    public static File master_FILE = join(heads_DIR, "master");


    /* TODO: fill in the rest of this class. */

    /**The java gitlet.Main init will set up the persistence after checking errors:
     1.Create the .gitlet folder and the subdirectories if it doesn’t exist.
     (call .mkdir())
     2.Commit the initial commit:
     a./commits will add the initial commit file.
     b.the reference file HEAD and refs/master will change (the initial commit hash ID).
     * */
    static void init() {
        //error checking: if there is an existing .gitlet folder in the CWD
        if (GITLET_DIR.exists() && GITLET_DIR.isDirectory()) {
            throw Utils.error("A Gitlet version-control system already exists " +
                    "in the current directory.");
        }
        GITLET_DIR.mkdir();

        commits_DIR.mkdir();
        blobs_DIR.mkdir();
        refs_DIR.mkdir();
        heads_DIR.mkdir();

        makeInitCommit();
    }

    /**This helper method will create an initial commit object,
     * write it into the commits_DIR (a file under its subdirectory),
     * and initialize the master file, HEAD file.*/
    private static void makeInitCommit() {
        String message = "initial commit";
        Commit initCommit = new Commit(message);
        initCommit.writeCommitIntoFile();

        // initialize master file
        String commitID = initCommit.getCommitID();
        Utils.writeContents(master_FILE, commitID);
        // initialize HEAD file
        writeContents(HEAD_FILE, "ref: refs/heads/master");
        // initialize staging area
        HashMap<String, String> files = new HashMap<>();
        writeObjectIntoIndex(files);
    }

    /**The java gitlet.Main add [file name] modifies the staging area(/index)
     * and create and save blob objects(/blobs).
     * First, create blob object abc.
     * Then, compare the hash ID of blob abc in with the current commit (HEAD):
     * if the blob is tracked by current commit, remove it from staging area;
     * else, put it into the staging area and save the blob into file system.
     * */
    static void add(String fileName) {
        File file = Utils.join(CWD, fileName);
        if (!file.exists()) {
            throw Utils.error("File does not exist.");
        }

        Blob blob = new Blob(file);
        String blobID = blob.getHashID();
        String commitID = getHEADcommitID();
        Commit commit = Commit.readCommitFromFile(commitID);
        HashMap<String, String> stagedFile = readObjectFromIndex();

        if (commit.containsBlob(fileName, blobID)) {
            stagedFile.remove(fileName);
        } else {
            stagedFile.put(fileName, blobID);
            blob.writeBlobIntoFile();
        }
        writeObjectIntoIndex(stagedFile);
    }

    /**The java gitlet.Main commit [message] modifies the staging area (/index)
     * and /commit. [includes error checking]
     1.it should create a new commit
     2. it will clean the staging area
     3. it will change the references.
     */
    static void commit(String message) {
        Commit curCommit = Commit.readCommitFromFile(getHEADcommitID()); // the current commit pointed by HEAD
        Commit newCommit = new Commit(message, curCommit);
        newCommit.writeCommitIntoFile();
        HashMap<String, String> emptyMap = new HashMap<>();
        writeObjectIntoIndex(emptyMap);
        updatePointers(newCommit.getCommitID());
    }

    /**The java gitlet.Main rm [file name] modifies the staging area,
     * /commit and the working directory.
     1.check if the file is in staging area -- yes -- unstage the file: delete the key-value pair from the staging area (a map)
     2.check if the file is in current commit (HEAD) -- yes
     -- stage for removal: add the key-value pair to the staging area (a map), maybe assign the value as None or “REMOVE” as a label. [ps. the commit command will clean all the staging area after creating new commit]
     --check if the file is in the working directory -- yes -- delete it from the working directory (using Utils)
     3.if it is neither in staging area nor in current commit, print an error message.*/
    static void rm(String fileName) {
        HashMap<String, String> stagingMap = readObjectFromIndex();
        boolean trackedByStagingArea = stagingMap.containsKey(fileName);
        if (trackedByStagingArea) {
            stagingMap.remove(fileName);
        }

        Commit curCommit = getCurCommit();
        boolean trackedByCurCommit = curCommit.getFiles().containsKey(fileName);
        if (trackedByCurCommit) {
            stagingMap.put(fileName, "REMOVE");

            File rm_FILE = Utils.join(CWD, fileName);
            Utils.restrictedDelete(rm_FILE);
        }

        if (!trackedByStagingArea && !trackedByCurCommit) {
            throw Utils.error("No reason to remove the file.");
        }

        writeObjectIntoIndex(stagingMap);
    }

    static void log() {
        StringBuilder logMessage = new StringBuilder();
        Commit curCommit = getCurCommit();
        log_helper(logMessage, curCommit);
        System.out.println(logMessage);
    }

    private static void log_helper(StringBuilder logMessage, Commit curCommit) {
        logMessage.append(curCommit.getLog());
        if (curCommit.getFirstParentID() != null) {
            Commit nextCommit = Commit.readCommitFromFile(curCommit.getFirstParentID());
            log_helper(logMessage, nextCommit);
        }
    }

    private static Commit getCurCommit() {
        return Commit.readCommitFromFile(getHEADcommitID());
    }

    private static void updatePointers(String newCommitID) {
        String HEADcontent = Utils.readContentsAsString(HEAD_FILE);
        if (HEADcontent.length() < 6) {
            throw error("HEAD file content is invalid: " + HEADcontent);
        }

        String firstLetters = HEADcontent.substring(0,4);
        if (firstLetters.equals("ref:")) {
            String path = HEADcontent.substring(5).trim();
            File headFile = Utils.join(GITLET_DIR, path);
            Utils.writeContents(headFile, newCommitID);
        } else {
            // detached
            Utils.writeContents(HEAD_FILE, newCommitID);
        }
    }


    /* This is a helper method to get the commit ID pointed by the HEAD.*/
    private static String getHEADcommitID() {
        String HEADcontent = Utils.readContentsAsString(HEAD_FILE);
        if (HEADcontent.length() < 6) {
            throw error("HEAD file content is invalid: " + HEADcontent);
        }

        String firstLetters = HEADcontent.substring(0,4);
        if (firstLetters.equals("ref:")) {
            String path = HEADcontent.substring(5).trim();
            File headFile = Utils.join(GITLET_DIR, path);
            String hashID = Utils.readContentsAsString(headFile);
            return hashID;
        } else {
            return HEADcontent;
        }
    }

    /* Methods related to staging area. */

    /* Persistence: this is a helper method for write the map object into index file. */
    private static void writeObjectIntoIndex(HashMap<String, String> files) {
        writeObject(index_FILE, files);
    }

    /* Persistence: this is a helper method for read the map object from the index file. */
    static HashMap<String, String> readObjectFromIndex() {
        HashMap<String, String> files = Utils.readObject(index_FILE, HashMap.class);
        return files;
    }

    private static boolean stagingAreaContainsBlob(String fileName, String blobID) {
        HashMap<String, String> stagingFiles = readObjectFromIndex();
        String trackedBlob = stagingFiles.get(fileName);
        return trackedBlob != null && blobID.equals(trackedBlob);
    }
}
