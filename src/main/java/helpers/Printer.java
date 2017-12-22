package helpers;

/**
 * This class is used to notify users about what's going on.
 * It prints messages with a priority greater than a threshold specified by the user.
 */
public class Printer 
{
	public static final int OK=0;
	public static final int MEDIUM=1;
	public static int minLevelOfPrintedMessages=0;
	
	public static void out(String string,int level)
	{
		System.out.println(string);
	}
}
