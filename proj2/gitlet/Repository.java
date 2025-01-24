package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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
            System.out.println("A Gitlet version-control system already exists " +
                    "in the current directory.");
            System.exit(0);
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
        initCommit.save();

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
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Blob blob = new Blob(file);
        String blobID = blob.getHashID();
        String commitID = getHEADcommitID();
        Commit commit = Commit.load(commitID);
        HashMap<String, String> stagedFile = readObjectFromIndex();

        if (commit.containsBlob(fileName, blobID)) {
            stagedFile.remove(fileName);
        } else {
            stagedFile.put(fileName, blobID);
            blob.save();
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
        Commit curCommit = Commit.load(getHEADcommitID()); // the current commit pointed by HEAD
        Commit newCommit = new Commit(message, curCommit);
        newCommit.save();
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
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        writeObjectIntoIndex(stagingMap);
    }

    /**Starting at the current head commit, display information about each commit
     * backwards along the commit tree until the initial commit,
     * following the first parent commit links,
     * ignoring any second parents found in merge commits. */
    static void log() {
        StringBuilder logMessage = new StringBuilder();
        Commit curCommit = getCurCommit();
        log_helper(logMessage, curCommit);
        System.out.println(logMessage);
    }

    private static void log_helper(StringBuilder logMessage, Commit curCommit) {
        logMessage.append(curCommit.getLog());
        if (curCommit.getFirstParentID() != null) {
            Commit nextCommit = Commit.load(curCommit.getFirstParentID());
            log_helper(logMessage, nextCommit);
        }
    }

    /**Like log, except displays information about all commits ever made.
     * The order of the commits does not matter. */
    static void globalLog() {
        File[] subDirs = commits_DIR.listFiles();
        if (subDirs != null) {
            for (File subDir : subDirs) {
                List<String> commitIDs = Utils.plainFilenamesIn(subDir);
                for (String commitID : commitIDs) {
                    Commit commit = Commit.load(commitID);
                    System.out.println(commit.getLog());
                }
            }
        }
    }

    /**Prints out the ids of all commits that have the given commit message, one per line.
     * If there are multiple such commits, it prints the ids out on separate lines.*/
    static void find(String message) {
        StringBuilder findMessage = new StringBuilder();
        File[] subDirs = commits_DIR.listFiles();
        if (subDirs != null) {
            for (File subDir : subDirs) {
                List<String> commitIDs = Utils.plainFilenamesIn(subDir);
                for (String commitID : commitIDs) {
                    Commit commit = Commit.load(commitID);
                    if (commit.getMessage().equals(message)) {
                        findMessage.append(commit.getCommitID()).append("\n");
                    }
                }

            }
        }

        if (findMessage.isEmpty()) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        System.out.println(findMessage);
    }

    /**Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.*/
    static void status() {
        printBranches();
        printStagedAndRemovedFiles();
        printModificationsAndUntracked();
    }

    private static void printBranches() {
        // get all branches in order
        List<String> branches = plainFilenamesIn(heads_DIR);
        branches.sort(null);

        // get current branch
        boolean detached = true;
        String currentBranch = getCurrentBranchName();
        if (currentBranch != null) {
            detached = false;
        }

        //print
        System.out.println("=== Branches ===");
        for (String branch : branches) {
            if (!detached && branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
    }

    private static String getCurrentBranchName() {
        String currentBranch = null;
        String HEADcontent = Utils.readContentsAsString(HEAD_FILE);
        if (HEADcontent.length() < 6) {
            throw error("HEAD file content is invalid: " + HEADcontent);
        }

        String firstLetters = HEADcontent.substring(0,4);
        if (firstLetters.equals("ref:")) {
            currentBranch = HEADcontent.replace("ref: refs/heads/", "").trim();
        }
        return currentBranch;
    }

    private static void printStagedAndRemovedFiles() {
        TreeSet<String> stagedFiles = new TreeSet<>();
        TreeSet<String> removedFiles = new TreeSet<>();

        HashMap<String, String> stagingMap = readObjectFromIndex();
        if (stagingMap != null) {
            for (Map.Entry<String, String> entry: stagingMap.entrySet()) {
                String fileName = entry.getKey();
                String action = entry.getValue();
                if (action.equals("REMOVE")) {
                    removedFiles.add(fileName);
                } else {
                    stagedFiles.add(fileName);
                }
            }
        }

        System.out.println("=== Staged Files ===");
        if (!stagedFiles.isEmpty()) {
            for (String file : stagedFiles) {
                System.out.println(file);
            }
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        if (!removedFiles.isEmpty()) {
            for (String file : removedFiles) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    private static void printModificationsAndUntracked () {
        Commit currentCommit = getCurCommit();
        HashMap<String, String> commitMap = currentCommit.getFiles();
        HashMap<String, String> stagingMap = readObjectFromIndex();
        HashMap<String, String> working_DIR_files = getWorkingDirFiles();

        TreeSet<String> modifiedFiles = new TreeSet<>();

        for (Map.Entry<String, String> entry : commitMap.entrySet()) {
            String fileName = entry.getKey();
            String commit_BlobID = entry.getValue();
            // Tracked in the current commit, changed in the working directory, but not staged
            if ((working_DIR_files.containsKey(fileName)
                    && !working_DIR_files.get(fileName).equals(commit_BlobID))
                    && !stagingMap.containsKey(fileName)) {
                modifiedFiles.add(fileName + " (modified)");
            }
            // Not staged for removal, but tracked in the current commit and deleted from the working directory.
            if (!working_DIR_files.containsKey(fileName)
                    && (!stagingMap.containsKey(fileName)
                    || !stagingMap.get(fileName).equals("REMOVE"))) {
                modifiedFiles.add(fileName + " (deleted)");
            }
        }

        for (Map.Entry<String, String> entry: stagingMap.entrySet()) {
            String fileName = entry.getKey();
            String staging_BlobID = entry.getValue();
            if (!staging_BlobID.equals("REMOVE")) {
                // Staged for addition, but deleted in the working directory
                if (!working_DIR_files.containsKey(fileName)) {
                    modifiedFiles.add(fileName + " (deleted)");
                }
                // Staged for addition, but with different contents than in the working directory
                if (!working_DIR_files.get(fileName).equals(staging_BlobID)) {
                    modifiedFiles.add(fileName + " (modified)");
                }
            }
        }

        // print modifications
        System.out.println("=== Modifications Not Staged For Commit ===");
        if (!modifiedFiles.isEmpty()) {
            for (String file : modifiedFiles) {
                System.out.println(file);
            }
        }
        System.out.println();

        // files present in the working directory but neither staged for addition nor tracked
        TreeSet<String> untrackedFiles = new TreeSet<>();
        for (String fileName: working_DIR_files.keySet()) {
            if (!stagingMap.containsKey(fileName) && !commitMap.containsKey(fileName)) {
                untrackedFiles.add(fileName);
            }
        }
        System.out.println("=== Untracked Files ===");
        if (!untrackedFiles.isEmpty()) {
            for (String file : untrackedFiles) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    /**Takes the version of the file as it exists in the head commit
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.*/
    static void checkoutFileName(String fileName) {
        Commit currentCommit = getCurCommit();
        checkoutFile(fileName, currentCommit);
    }

    /**Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.*/
    static void checkoutCommitID(String commitID, String fileName) {
        // check error is handled in Commit class
        Commit target = Commit.load(commitID);
        checkoutFile(fileName, target);
    }

    /**Takes the version of the file as it exists in the given commit,
     * and puts it in the working directory. */
    private static void checkoutFile(String fileName, Commit commit) {
        HashMap<String, String> commitFiles = commit.getFiles();
        // check error
        if (!commitFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String blobID = commitFiles.get(fileName);
        Blob.copyContentToFile(fileName, blobID);
    }

    /**All the files in the target branch, no matter whether exit in current working dir,
     * would be written into the working dir.
     * All the files tracked in the working dir would be overwritten or deleted;
     * untracked files, if not being overwritten, would be reserved.*/
    static void checkoutBranch(String branchName) {
        // error checking
        List<String> branchNames = Utils.plainFilenamesIn(heads_DIR);
        if (!branchNames.contains(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        String currentBranchName = getCurrentBranchName();
        if (currentBranchName.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit targetCommit = getCommitFromBranch(branchName);
        HashMap<String,String> targetMap = targetCommit.getFiles();

        Commit currentCommit = getCurCommit();
        HashMap<String, String> commitMap = currentCommit.getFiles();
        HashMap<String, String> stagingMap = readObjectFromIndex();
        HashMap<String, String> working_DIR_files = getWorkingDirFiles();
        // check untracked files that would be overwritten
        for (String fileName : working_DIR_files.keySet()) {
            if (!stagingMap.containsKey(fileName) && !commitMap.containsKey(fileName)
                    && targetMap.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        checkoutCommit(targetCommit);

        String HEADcontent = "ref: refs/heads/" + branchName;
        Utils.writeContents(HEAD_FILE, HEADcontent);
    }

    /**A helper method for checkout branch and reset.
     * It overwrites the working dir and cleans the staging area.
     * ps. HEAD updating is not handled here!*/
    private static void checkoutCommit(Commit targetCommit) {
        HashMap<String,String> targetMap = targetCommit.getFiles();
        HashMap<String, String> working_DIR_files = getWorkingDirFiles();

        // copy files in the target commit into working dir
        for (Map.Entry<String, String> entry: targetMap.entrySet()) {
            String fileName = entry.getKey();
            String blobID = entry.getValue();
            if (working_DIR_files.containsKey(fileName)) {
                if (!blobID.equals(working_DIR_files.get(fileName))) {
                    Blob.copyContentToFile(fileName, blobID);
                }
            } else {
                Blob.copyContentToFile(fileName, blobID);
            }
        }
        // delete files tracked by current commit but not the target commit
        Commit currentCommit = getCurCommit();
        HashMap<String, String> commitMap = currentCommit.getFiles();
        for (String fileName: commitMap.keySet()) {
            if (!targetMap.containsKey(fileName)) {
                File file = Utils.join(CWD, fileName);
                Utils.restrictedDelete(file);
            }
        }

        HashMap<String, String> stagingMap = new HashMap<>();
        writeObjectIntoIndex(stagingMap);

    }


    /* A helper method that returns the pointed commit in a branch
    given by the name of the branch. */
    private static Commit getCommitFromBranch(String branchName) {
        File head_FILE = Utils.join(heads_DIR, branchName);
        String commitID = readContentsAsString(head_FILE);
        Commit targetCommit = Commit.load(commitID);
        return targetCommit;
    }

    /**A helper method that turns all the files in the current working directory,
     * in fileName-blob pair hash map. */
    private static HashMap<String, String> getWorkingDirFiles() {
        List<String> CWD_files = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> working_DIR_files = new HashMap<>();
        for (String fileName: CWD_files) {
            File file = Utils.join(CWD, fileName);
            if (file.isFile()) {
                Blob blob = new Blob(file);
                String blobID = blob.getHashID();
                working_DIR_files.put(fileName, blobID);
            }
        }
        return working_DIR_files;
    }


    /**A helper method that updates HEAD and branch pointer after
     * making a new commit.*/
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

    /**A helper method that returns the commit pointed by HEAD.*/
    private static Commit getCurCommit() {
        return Commit.load(getHEADcommitID());
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
