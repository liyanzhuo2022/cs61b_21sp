package gitlet;

import java.io.File;
import java.util.*;

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

    static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    static final File INDEX_FILE = join(GITLET_DIR, "index");
    static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    static final File REFS_DIR = join(GITLET_DIR, "refs");
    static final File BRANCHES_DIR = join(REFS_DIR, "heads");
    static final File MASTER_FILE = join(BRANCHES_DIR, "master");
    static final File REMOTES_BRANCHES = join(REFS_DIR, "remotes");
    static final File REMOTES_ADDRESS = join(GITLET_DIR, "remotes");


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
            System.out.println("A Gitlet version-control system already exists "
                    + "in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();

        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        REFS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        REMOTES_BRANCHES.mkdir();
        REMOTES_ADDRESS.mkdir();

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
        Utils.writeContents(MASTER_FILE, commitID);
        // initialize HEAD file
        writeContents(HEAD_FILE, "ref: refs/heads/master");
        // initialize staging area
        HashMap<String, String> files = new HashMap<>();
        saveStagingArea(files);
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
        HashMap<String, String> stagedFile = loadStagingArea();

        if (commit.containsBlob(fileName, blobID)) {
            stagedFile.remove(fileName);
        } else {
            stagedFile.put(fileName, blobID);
            blob.save();
        }
        saveStagingArea(stagedFile);
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
        saveStagingArea(emptyMap);
        updatePointers(newCommit.getCommitID());
    }

    /**The java gitlet.Main rm [file name] modifies the staging area,
     * /commit and the working directory.
     1.check if the file is in staging area -- yes -- unstage the file:
     delete the key-value pair from the staging area (a map)
     2.check if the file is in current commit (HEAD) -- yes
     -- stage for removal: add the key-value pair to the staging area (a map),
     maybe assign the value as None or “REMOVE” as a label.
     [ps. the commit command will clean all the staging area after creating new commit]
     --check if the file is in the working directory
     -- yes -- delete it from the working directory (using Utils)
     3.if it is neither in staging area nor in current commit, print an error message.*/
    static void rm(String fileName) {
        HashMap<String, String> stagingMap = loadStagingArea();
        boolean trackedByStagingArea = stagingMap.containsKey(fileName);
        if (trackedByStagingArea) {
            stagingMap.remove(fileName);
        }

        Commit curCommit = getCurCommit();
        boolean trackedByCurCommit = curCommit.getFiles().containsKey(fileName);
        if (trackedByCurCommit) {
            stagingMap.put(fileName, "REMOVE");

            File rmFile = Utils.join(CWD, fileName);
            Utils.restrictedDelete(rmFile);
        }

        if (!trackedByStagingArea && !trackedByCurCommit) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        saveStagingArea(stagingMap);
    }

    /**Starting at the current head commit, display information about each commit
     * backwards along the commit tree until the initial commit,
     * following the first parent commit links,
     * ignoring any second parents found in merge commits. */
    static void log() {
        StringBuilder logMessage = new StringBuilder();
        Commit curCommit = getCurCommit();
        logHelper(logMessage, curCommit);
        System.out.println(logMessage);
    }

    private static void logHelper(StringBuilder logMessage, Commit curCommit) {
        logMessage.append(curCommit.getLog());
        if (curCommit.getFirstParentID() != null) {
            Commit nextCommit = Commit.load(curCommit.getFirstParentID());
            logHelper(logMessage, nextCommit);
        }
    }

    /**Like log, except displays information about all commits ever made.
     * The order of the commits does not matter. */
    static void globalLog() {
        File[] subDirs = COMMITS_DIR.listFiles();
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
        File[] subDirs = COMMITS_DIR.listFiles();
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
        checkRepo();
        printBranches();
        printStagedAndRemovedFiles();
        printModificationsAndUntracked();
    }

    /**If a user inputs a command that requires being in an initialized Gitlet
     * working directory (i.e., one containing a .gitlet subdirectory),
     * but is not in such a directory*/
    private static void checkRepo() {
        if (!GITLET_DIR.exists() || !GITLET_DIR.isDirectory()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static void printBranches() {
        // get all branches in order
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
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

    /**Return the name of the current branch.*/
    private static String getCurrentBranchName() {
        String currentBranch = null;
        String headContent = Utils.readContentsAsString(HEAD_FILE);
        if (headContent.length() < 6) {
            throw error("HEAD file content is invalid: " + headContent);
        }

        String firstLetters = headContent.substring(0, 4);
        if (firstLetters.equals("ref:")) {
            currentBranch = headContent.replace("ref: refs/heads/", "").trim();
        }
        return currentBranch;
    }

    private static void printStagedAndRemovedFiles() {
        TreeSet<String> stagedFiles = new TreeSet<>();
        TreeSet<String> removedFiles = new TreeSet<>();

        HashMap<String, String> stagingMap = loadStagingArea();
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

    private static void printModificationsAndUntracked() {
        Commit currentCommit = getCurCommit();
        HashMap<String, String> commitMap = currentCommit.getFiles();
        HashMap<String, String> stagingMap = loadStagingArea();
        HashMap<String, String> workingDirFiles = getWorkingDirFiles();

        TreeSet<String> modifiedFiles = new TreeSet<>();

        for (Map.Entry<String, String> entry : commitMap.entrySet()) {
            String fileName = entry.getKey();
            String commitBlobID = entry.getValue();
            // Tracked in the current commit, changed in the working directory, but not staged
            if ((workingDirFiles.containsKey(fileName)
                    && !workingDirFiles.get(fileName).equals(commitBlobID))
                    && !stagingMap.containsKey(fileName)) {
                modifiedFiles.add(fileName + " (modified)");
            }
            // Not staged for removal, but tracked in the current commit
            // and deleted from the working directory.
            if (!workingDirFiles.containsKey(fileName)
                    && (!stagingMap.containsKey(fileName)
                    || !stagingMap.get(fileName).equals("REMOVE"))) {
                modifiedFiles.add(fileName + " (deleted)");
            }
        }

        for (Map.Entry<String, String> entry: stagingMap.entrySet()) {
            String fileName = entry.getKey();
            String stagingBlobID = entry.getValue();
            if (!stagingBlobID.equals("REMOVE")) {
                // Staged for addition, but deleted in the working directory
                if (!workingDirFiles.containsKey(fileName)) {
                    modifiedFiles.add(fileName + " (deleted)");
                }
                // Staged for addition, but with different contents than in the working directory
                if (!workingDirFiles.get(fileName).equals(stagingBlobID)) {
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
        for (String fileName: workingDirFiles.keySet()) {
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
        List<String> branchNames = Utils.plainFilenamesIn(BRANCHES_DIR);
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
        untrackedFailCase(targetCommit);
        checkoutCommit(targetCommit);

        String headContent = "ref: refs/heads/" + branchName;
        Utils.writeContents(HEAD_FILE, headContent);
    }

    /**A helper method for checkout, that handles failure case:
     * If a working file is untracked in the current branch
     * and would be overwritten by checkout, print message and exit*/
    private static void untrackedFailCase(Commit targetCommit) {
        HashMap<String, String> targetMap = targetCommit.getFiles();

        Commit currentCommit = getCurCommit();
        HashMap<String, String> commitMap = currentCommit.getFiles();
        HashMap<String, String> stagingMap = loadStagingArea();
        HashMap<String, String> workingDirFiles = getWorkingDirFiles();
        // check untracked files that would be overwritten
        for (String fileName : workingDirFiles.keySet()) {
            if (!stagingMap.containsKey(fileName) && !commitMap.containsKey(fileName)
                    && targetMap.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /**A helper method for checkout branch and reset.
     * It overwrites the working dir into the current commit
     * and cleans the staging area.
     * ps. HEAD updating is not handled here!*/
    private static void checkoutCommit(Commit targetCommit) {
        HashMap<String, String> targetMap = targetCommit.getFiles();
        HashMap<String, String> workingDirFiles = getWorkingDirFiles();

        // copy files in the target commit into working dir
        for (Map.Entry<String, String> entry: targetMap.entrySet()) {
            String fileName = entry.getKey();
            String blobID = entry.getValue();
            if (workingDirFiles.containsKey(fileName)) {
                if (!blobID.equals(workingDirFiles.get(fileName))) {
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
        saveStagingArea(stagingMap);

    }

    /**Creates a new branch with the given name, and points it at the current head commit.
     * This command does NOT immediately switch to the newly created branch
     * (just as in real Git). */
    static void branch(String branchName) {
        File newBranch = Utils.join(BRANCHES_DIR, branchName);
        // check whether already exists
        File[] branches = BRANCHES_DIR.listFiles();
        if (branches != null) {
            if (Arrays.asList(branches).contains(newBranch)) {
                System.out.println("A branch with that name already exists.");
                System.exit(0);
            }
        }

        String currentCommitID = getHEADcommitID();
        Utils.writeContents(newBranch, currentCommitID);
    }

    /**Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch,
     * or anything like that.*/
    static void rmBranch(String branchName) {
        String currentBranchName = getCurrentBranchName();
        if (currentBranchName.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        File targetBranch = Utils.join(BRANCHES_DIR, branchName);
        File[] branches = BRANCHES_DIR.listFiles();
        if (branches != null) {
            if (!Arrays.asList(branches).contains(targetBranch)) {
                System.out.println("A branch with that name does not exist.");
                System.exit(0);
            }
        }
        targetBranch.delete();
    }


    /**Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.*/
    static void reset(String commitID) {
        Commit targetCommit = Commit.load(commitID);
        untrackedFailCase(targetCommit);
        checkoutCommit(targetCommit); // handles working dir and staging area
        // update the head of current branch
        String currentBranchName = getCurrentBranchName();
        File currentBranch = Utils.join(BRANCHES_DIR, currentBranchName);
        writeContents(currentBranch, commitID);
    }

    /**Merge is buggy: can't pass 36a) merge-parent2 (0/44.444),
     * change the code for split point doesn't help with it.*/
    // TODO: debug merge!!!
    static void merge(String givenBranchName) {
        HashMap<String, String> stagingMap = loadStagingArea();
        if (!stagingMap.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        String currentBranchName = getCurrentBranchName();
        Commit curCommit = getCurCommit();
        if (currentBranchName.equals(givenBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Commit givenCommit = getCommitFromBranch(givenBranchName);
        Commit splitCommit = Commit.getSplitPoint(curCommit, givenCommit);
        if (splitCommit.getCommitID().equals(givenCommit.getCommitID())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitCommit.getCommitID().equals(curCommit.getCommitID())) {
            checkoutBranch(givenBranchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        untrackedFailCase(givenCommit);

        HashMap<String, String> splitMap = splitCommit.getFiles();
        HashMap<String, String> curMap = curCommit.getFiles();
        HashMap<String, String> givenMap = givenCommit.getFiles();

        HashSet<String> allFileNames = new HashSet<>();
        addKeysToSet(splitMap, allFileNames);
        addKeysToSet(curMap, allFileNames);
        addKeysToSet(givenMap, allFileNames);

        boolean conflicted = false;
        for (String fileName : allFileNames) {
            String notExist = "null";
            String splitID = splitMap.getOrDefault(fileName, notExist);
            String curID = curMap.getOrDefault(fileName, notExist);
            String givenID = givenMap.getOrDefault(fileName, notExist);

            if (!splitID.equals(curID) && !splitID.equals(givenID)
                    && !curID.equals(givenID)) {
                conflict(fileName, curID, givenID, stagingMap);
                conflicted = true;
            }

            if (splitID.equals(curID) && !splitID.equals(givenID)) {
                if (givenMap.containsKey(fileName)) {
                    checkoutFile(fileName, givenCommit);
                    stagingMap.put(fileName, givenMap.get(fileName));
                } else {
                    File rmFile = Utils.join(CWD, fileName);
                    Utils.restrictedDelete(rmFile);
                    stagingMap.put(fileName, "REMOVE");
                }
            }

            //? check whether the same name file exists in working dir
            if ((curID.equals(notExist) && givenID.equals(notExist))
                    || (splitID.equals(givenID) && curID.equals(notExist))) {
                File rmFile = Utils.join(CWD, fileName);
                Utils.restrictedDelete(rmFile);
            }
        }

        saveStagingArea(stagingMap);
        commit("Merged " + givenBranchName + " into " + currentBranchName + ".");
        if (conflicted) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**A helper method for merge to handle conflict cases.*/
    private static void conflict(String fileName, String curBlobID, String givenBlobID,
                                 HashMap<String, String> stagingMap) {
        File targetFile = Utils.join(CWD, fileName);

        String curContent;
        String givenContent;
        if (curBlobID.equals("null")) {
            curContent = "";
        } else {
            Blob curBlob = Blob.load(curBlobID);
            curContent = curBlob.getContentAsString();
        }
        if (givenBlobID.equals("null")) {
            givenContent = "";
        } else {
            Blob givenBlob = Blob.load(givenBlobID);
            givenContent = givenBlob.getContentAsString();
        }

        String content = "<<<<<<< HEAD\n" + curContent + "=======\n"
                + givenContent + ">>>>>>>\n";
        Utils.writeContents(targetFile, content);

        Blob targetBlob = new Blob(targetFile);
        String targetID = targetBlob.getHashID();
        targetBlob.save();
        stagingMap.put(fileName, targetID);
    }

    /**A helper method that will add all the keys in the map into the set.*/
    private static void addKeysToSet(HashMap<String, String> map, HashSet<String> allFileNames) {
        for (String fileName : map.keySet()) {
            allFileNames.add(fileName);
        }
    }

    // TODO: remote ec!!! REMOTE
    /** Saves the given login information under the given remote name. */
    static void addRemote(String remoteName, String remoteDirPath) {
        List<String> allRemoteNames = plainFilenamesIn(REMOTES_ADDRESS);
        if (allRemoteNames.contains(remoteName)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }

        String formattedPath = remoteDirPath.replace("/", File.separator);
        File newRemote = join(REMOTES_ADDRESS, remoteName);
        writeContents(newRemote, formattedPath);
    }

    /** Remove information associated with the given remote name. */
    static void rmRemote(String remoteName) {
        List<String> allRemoteNames = plainFilenamesIn(REMOTES_ADDRESS);
        if (!allRemoteNames.contains(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }

        File toDelete = join(REMOTES_ADDRESS, remoteName);
        restrictedDelete(toDelete);
    }








    /**A helper method that returns the pointed commit in a branch
     * given by the name of the branch. */
    private static Commit getCommitFromBranch(String branchName) {
        File headFile = Utils.join(BRANCHES_DIR, branchName);
        if (!headFile.exists() || !headFile.isFile()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String commitID = readContentsAsString(headFile);
        Commit targetCommit = Commit.load(commitID);
        return targetCommit;
    }

    /**A helper method that turns all the files in the current working directory,
     * in fileName-blob pair hash map. */
    private static HashMap<String, String> getWorkingDirFiles() {
        List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> workingDirFiles = new HashMap<>();
        for (String fileName: cwdFiles) {
            File file = Utils.join(CWD, fileName);
            if (file.isFile()) {
                Blob blob = new Blob(file);
                String blobID = blob.getHashID();
                workingDirFiles.put(fileName, blobID);
            }
        }
        return workingDirFiles;
    }

    /**A helper method that updates HEAD and branch pointer after
     * making a new commit.*/
    private static void updatePointers(String newCommitID) {
        String headContent = Utils.readContentsAsString(HEAD_FILE);
        if (headContent.length() < 6) {
            throw error("HEAD file content is invalid: " + headContent);
        }

        String firstLetters = headContent.substring(0, 4);
        if (firstLetters.equals("ref:")) {
            String path = headContent.substring(5).trim();
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

    /** This is a helper method to get the commit ID pointed by the HEAD.*/
    private static String getHEADcommitID() {
        String headContent = Utils.readContentsAsString(HEAD_FILE);
        if (headContent.length() < 6) {
            throw error("HEAD file content is invalid: " + headContent);
        }

        String firstLetters = headContent.substring(0, 4);
        if (firstLetters.equals("ref:")) {
            String path = headContent.substring(5).trim();
            File headFile = Utils.join(GITLET_DIR, path);
            String hashID = Utils.readContentsAsString(headFile);
            return hashID;
        } else {
            return headContent;
        }
    }


    /* Methods related to staging area. */

    /** Persistence: this is a helper method for write the map object into index file. */
    private static void saveStagingArea(HashMap<String, String> files) {
        writeObject(INDEX_FILE, files);
    }

    /** Persistence: this is a helper method for read the map object from the index file. */
    static HashMap<String, String> loadStagingArea() {
        HashMap<String, String> files = Utils.readObject(INDEX_FILE, HashMap.class);
        return files;
    }

    private static boolean stagingAreaContainsBlob(String fileName, String blobID) {
        HashMap<String, String> stagingFiles = loadStagingArea();
        String trackedBlob = stagingFiles.get(fileName);
        return trackedBlob != null && blobID.equals(trackedBlob);
    }
}
