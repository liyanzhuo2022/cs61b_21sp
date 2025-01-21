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
            case "init":
                Repository.init();
                break;
            case "add":
                if (args.length != 2) {
                    throw Utils.error("Must have 2 arguments");
                }
                String fileName = args[1];
                Repository.add(fileName);
                break;
            case "commit":
                if (args.length == 1) {
                    throw Utils.error("Please enter a commit message.");
                }
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    messageBuilder.append(args[i]);
                    if (i < args.length - 1) {
                        messageBuilder.append(" ");
                    }
                }
                String message = messageBuilder.toString();
                Repository.commit(message);

            // TODO: FILL THE REST IN
        }
    }
}
