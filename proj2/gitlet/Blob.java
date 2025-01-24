package gitlet;


import java.io.File;
import java.io.Serializable;

/**This class represents a Blob that will be stored in a file.
 * Because each blob has a unique hash ID, we will use it
 * as the name of the file which the blob object serialized to.
 * All blob objects are serialized within the BLOB_DIR under the GITLET_DIR.
 * The first two characters of their hash ID will be used as the subdirectory,
 * to enhance time and space efficiency.
 * It has helper methods to read the blob object from files given its hash ID,
 * and write the blob object into a file to persistent its change.
 *
 * @author Li Yanzhuo
 * */
public class Blob implements Serializable {
    static final File BLOB_DIR = Utils.join(Repository.GITLET_DIR, "blobs");
    private String hashID;
    private byte[] content;

    /* TODO: fill in the rest of the class. */

    /* constructor of the Blob class */
    Blob(File file) {
        this.content = Utils.readContents(file);
        this.hashID = Utils.sha1(this.content);
    }

    String getHashID() {
        return this.hashID;
    }

    byte[] getContent() {
        return this.content;
    }

    /**Persistence: a method that writes the blob object into file,
     * in the subdirectory by its first 2 id numbers - Hash Table. */
    void save() {
        if (this.hashID == null || hashID.length() < 2) {
            System.out.println("HashID of the blob is shorter than 2.");
            System.exit(0);
        }
        String firstTwoID = this.hashID.substring(0,2);
        File subDir = Utils.join(BLOB_DIR, firstTwoID);
        subDir.mkdir(); // mkdir() will check whether the dir exists
        File blob_FILE = Utils.join(subDir, this.hashID);
        /**
        if (blob_FILE.exists()) {
            System.out.println("Same blob file already exists.");
            System.exit(0);
        }
         */
        Utils.writeObject(blob_FILE, this);
    }

    /**Persistence: Given the blob ID, this method returns the blob object
     * read from the files. It enters the subdirectory first, as the blobs
     * distribute as in a hash table. */
    static Blob load(String blobID) {
        String firstTwoID = blobID.substring(0,2);
        File subDir = Utils.join(BLOB_DIR, firstTwoID);
        if (subDir.exists() && subDir.isDirectory()) {
            File blob_FILE = Utils.join(subDir, blobID);
            Blob blob = Utils.readObject(blob_FILE, Blob.class);
            return blob;
        } else {
            throw Utils.error("Can't find the subdirectory of the blob: "+ blobID);
        }
    }

    static void copyContentToFile(String fileName, String blobID) {
        Blob blob = Blob.load(blobID);
        File target_FILE = Utils.join(Repository.CWD, fileName);
        Utils.writeContents(target_FILE, blob.getContent());
    }
}
