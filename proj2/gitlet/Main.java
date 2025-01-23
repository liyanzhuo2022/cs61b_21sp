package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     *  init -- initialize the gitlet repo
     *  add [filename] -- add file to staging area
     *  commit [message] -- create a new commit
     */
    public static void main(String[] args) {
        // what if args is empty?
        if (args.length == 0) {
            throw Utils.error("Must have at least one argument.");
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init": {
                Repository.init();
                break;
            }
            case "add": {
                if (args.length != 2) {
                    throw Utils.error("Must have 2 arguments");
                }
                String fileName = args[1];
                Repository.add(fileName);
                break;
            }
            case "commit": {
                if (args.length == 1) {
                    throw Utils.error("Please enter a commit message.");
                }
                if (args.length > 2) {
                    throw Utils.error("Please quote the commit message.");
                }
                String message = args[1];
                if ((message.startsWith("\"") && message.endsWith("\"")) ||
                        (message.startsWith("'") && message.endsWith("'"))) {
                    message = message.substring(1, message.length() - 1);
                }
                Repository.commit(message);
                break;
            }
            case "rm": {
                if (args.length == 1) {
                    throw Utils.error("Please enter the file name.");
                }
                if (args.length > 2) {
                    throw Utils.error("Please enter just one file.");
                }
                String fileName = args[1];
                Repository.rm(fileName);
                break;
            }
            case "log": {
                Repository.log();
                break;
            }
            case "global-log": {
                Repository.globalLog();
                break;
            }
            case "find": {
                if (args.length == 1) {
                    throw Utils.error("Please enter a commit message.");
                }
                if (args.length > 2) {
                    throw Utils.error("Please quote the commit message.");
                }
                String message = args[1];
                if ((message.startsWith("\"") && message.endsWith("\"")) ||
                        (message.startsWith("'") && message.endsWith("'"))) {
                    message = message.substring(1, message.length() - 1);
                }
                Repository.find(message);
                break;
            }
            case "status": {
                Repository.status();
                break;
            }
            case "checkout": {
                // java gitlet.Main checkout -- [file name]
                if (args.length == 2) {
                    if (!args[0].equals("--")) {
                        throw Utils.error("Wrong formatting.");
                    }
                    String fileName = args[1];
                    Repository.checkoutFileName(fileName);
                    break;
                }
                // java gitlet.Main checkout [commit id] -- [file name]
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        throw Utils.error("Wrong formatting.");
                    }
                    String commitID = args[0];
                    String fileName = args[2];
                    Repository.checkoutCommitID(commitID, fileName);
                    break;
                }
                // java gitlet.Main checkout [branch name]
                if (args.length == 1) {
                    String branchName = args[0];
                    Repository.checkoutBranch(branchName);
                    break;
                }
                throw Utils.error("Wrong formatting.");
            }



            // TODO: FILL THE REST IN
        }
    }
}
