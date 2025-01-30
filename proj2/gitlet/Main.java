package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Li Yanzhuo
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
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init": {
                Repository.init();
                break;
            }
            case "add": {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String fileName = args[1];
                Repository.add(fileName);
                break;
            }
            case "commit": {
                if (args.length == 1 || args[1].equals("")) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String message = args[1];

                if ((message.startsWith("\"") && message.endsWith("\""))
                        || (message.startsWith("'") && message.endsWith("'"))) {
                    message = message.substring(1, message.length() - 1);
                }
                Repository.commit(message);
                break;
            }
            case "rm": {
                if (args.length == 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
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
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length > 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                String message = args[1];
                if ((message.startsWith("\"") && message.endsWith("\""))
                        || (message.startsWith("'") && message.endsWith("'"))) {
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
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String fileName = args[2];
                    Repository.checkoutFileName(fileName);
                    break;
                }
                // java gitlet.Main checkout [commit id] -- [file name]
                if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    String commitID = args[1];
                    String fileName = args[3];
                    Repository.checkoutCommitID(commitID, fileName);
                    break;
                }
                // java gitlet.Main checkout [branch name]
                if (args.length == 2) {
                    String branchName = args[1];
                    Repository.checkoutBranch(branchName);
                    break;
                }
                System.out.println("Incorrect operands.");
                System.exit(0);
                break;
            }
            case "branch": {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                    break;
                }
                String branchName = args[1];
                Repository.branch(branchName);
                break;
            }
            case "rm-branch": {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                    break;
                }
                String branchName = args[1];
                Repository.rmBranch(branchName);
                break;
            }
            case "reset": {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                    break;
                }
                String commitID = args[1];
                Repository.reset(commitID);
                break;
            }
            case "merge": {
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                    break;
                }
                String givenBranchName = args[1];
                Repository.merge(givenBranchName);
                break;
            }
            default: {
                System.out.println("No command with that name exists.");
                System.exit(0);
                break;
            }
        }
    }
}
