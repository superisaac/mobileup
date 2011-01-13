/** %LICENSE% **/
package mobileup.util;

public class Log {
    private static ILogger logger=null;

    public static void registerLogger(ILogger log) {
	logger = log;
    }

    public static void e(String directive, String content) {
	if(logger != null) {
	    logger.e(directive, content);
	} else {
	    System.err.println("ERROR: " + directive + "> " + content);
	}
    }

    public static void w(String directive, String content) {
	if(logger != null) {
	    logger.w(directive, content);
	} else {
	    System.err.println("WARN: " + directive + "> " + content);
	}
    }

    public static void d(String directive, String content) {
	if(logger != null) {
	    logger.e(directive, content);
	} else {
	    System.out.println("DEBUG: " + directive + "> " + content);
	}
    }
    public static void p(String directive, byte[] content) {
	p(directive, content, content.length);
    }

    public static void p(String directive, byte[] content, int count) {
        System.out.print("PPRINT: " + directive + "> ");
        for(int i=0; i< count; i++) {
            byte c = content[i];
            System.out.print(String.format("\\x%x", c));
        }
        System.out.println();
    }
}
