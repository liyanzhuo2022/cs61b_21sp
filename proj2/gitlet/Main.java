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
            }



            // TODO: FILL THE REST IN
        }
    }
}
