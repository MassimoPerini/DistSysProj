package utils;

import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

/**
 * Created by higla on 24/12/2017.
 /**
 * This class is useful to print debug messages. It is a singleton-like (the method setLevel changes the instance) and should be initialized before being used.
 * It contains four levels of debugging: verbose (lv 3), normal (lv 2), only errors (lv 1), nothing (lv 0). Every message with a level lower is printed.
 * This class should not be used to print normal messages to the user, just debug and error messages.
 */
public class Debug {

    private static UUID uuid;

        /**
         * Nothing is printed
         */
        public static final int LEVEL_NOTHING = 0;

        /**
         * Only errors are printed
         */
        public static final int LEVEL_ERRORS = 1;

        /**
         * Debug messages and errors are printed
         */
        public static final int LEVEL_NORMAL = 2;

        /**
         * Everything is printed
         */
        public static final int LEVEL_VERBOSE = 3;

        /**
         * Singleton instance
         */
        private static Debug internalInstance = null;

        /**
         * current level, by default set to nothing
         */
        private final int level;

        /**
         * private constructor to create the singleton instance
         * @param debLevel the desired level
         */
        private Debug(int debLevel)
        {
            this.level = debLevel;
        }

        /**
         * public method to create the instance of the singleton
         * @param debLevel the desired level
         * @throws IllegalArgumentException if the level is not one of the decided ones
         * @return the instance
         */
        public static Debug instance(int debLevel) throws IllegalArgumentException
        {
            if(debLevel < LEVEL_NOTHING || debLevel > LEVEL_VERBOSE)
            {
                throw new IllegalArgumentException("Level not recognised");
            }

            if(internalInstance == null)
                internalInstance = new Debug(debLevel);

            return internalInstance;
        }

        /**
         * Method to get the singleton instance, if not already initialized it's created with the default level LEVEL_NOTHING
         * @return the singleton instance
         */
        public static Debug getInstance()
        {
            if(internalInstance == null)
                internalInstance = new Debug(LEVEL_NOTHING);

            return internalInstance;
        }

        /**
         * setter to change debug level, may be useful to have a certain level of debug in one part of code and a different one in a different part
         * @param debLevel the new desired level
         * @throws IllegalArgumentException if the level is not one of the decided ones
         */
        public static void setLevel(int debLevel) throws IllegalArgumentException
        {
            if(debLevel < LEVEL_NOTHING || debLevel > LEVEL_VERBOSE)
            {
                throw new IllegalArgumentException("Level not recognised");
            }
            internalInstance = new Debug(debLevel);
        }


        /**
         * internal private function to decorate the text of a message and print it or not in relation with the level
         * @param text that will be printed
         * @param header
         * @param printStream the stream out of where the text will be printed
         * @param desiredLv of the debug
         */
        private static void printText(String text, String header, PrintStream printStream, int desiredLv)
        {
            if(getInstance().level >= desiredLv)
                printStream.println("---" + header + ": " + text);
        }

        /**
         * * internal private function to decorate the exception and print it or not in relation with the level
         * @param e the exception
         * @param header
         * @param printStream the stream out of where the text will be printed
         * @param desiredLv the level of debug
         */
        private static void printException(Exception e, String header, PrintStream printStream, int desiredLv)
        {
            if(getInstance().level >= desiredLv) {
                printStream.println("---" + header + "exception: ");
                printStream.println("--- Exception Message: " + e.getMessage());
                printStream.println("--- Stack Trace: ");
            }
        }

        /**
         * internal private function to decorate the exception and the relative text and print it or not in relation with the level
         * @param e the exception
         * @param header
         * @param printStream the stream out of where the text will be printed
         * @param desiredLv the level of debug
         */
        private static void printException(Exception e, String text, String header, PrintStream printStream, int desiredLv)
        {
            if(getInstance().level >= desiredLv) {
                printStream.println("---" + header + "exception: ");
                printStream.println("--- Added message: " + text);
                printStream.println("--- Exception Message: " + e.getMessage());
                printStream.println("--- Stack Trace: " + getStackTrace(e));
            }
        }

        /**
         * prints the desired debug message if the level is set to LEVEL_NORMAL
         * @param text the message to be printed
         */
        public static void printVerbose(String text)
        {
            printText(text, "DEBUG-VERB", System.out, LEVEL_VERBOSE);
        }

        /**
         * prints the message and stack trace of the exception
         * @param e the exception that should be visualized
         */
        public static void printVerbose(Exception e)
        {
            printException(e, "DEBUG-VERB", System.out, LEVEL_VERBOSE);
        }

        /**
         * prints the message and stack trace of the exception, plus a message chosen by the user of the function
         * @param e the exception that should be visualized
         */
        public static void printVerbose(String text, Exception e)
        {
            printException(e, text, "DEBUG-VERB", System.out, LEVEL_VERBOSE);
        }

        /**
         * prints the desired debug message if the level is set to LEVEL_NORMAL
         * @param text the message to be printed
         */
        public static void printDebug(String text)
        {
            printText(text, "DEBUG", System.out, LEVEL_NORMAL);
        }

        /**
         * prints the message and stack trace of the exception
         * @param e the exception that should be visualized
         */
        public static void printDebug(Exception e)
        {
            printException(e, "DEBUG", System.out, LEVEL_NORMAL);
        }

        /**
         * prints the message and stack trace of the exception, plus a message chosen by the user of the function
         * @param e the exception that should be visualized
         */
        public static void printDebug(String text, Exception e)
        {
            printException(e, text, "DEBUG", System.out, LEVEL_NORMAL);
        }

        /**
         * prints the desired debug message if the level is set to LEVEL_ERRORS
         * @param text the message to be printed
         */
        public static void printError(String text)
        {
            printText(text, "ERROR", System.err, LEVEL_ERRORS);
        }

        /**
         * prints the message and stack trace of the exception
         * @param e the exception that should be visualized
         */
        public static void printError(Exception e)
        {
            printException(e, "ERROR", System.err, LEVEL_ERRORS);
        }

        /**
         * prints the message and stack trace of the exception, plus a message chosen by the user of the function
         * @param e the exception that should be visualized
         */
        public static void printError(String text, Exception e)
        {
            printException(e, text, "ERROR", System.err, LEVEL_ERRORS);
        }


        private static String getStackTrace(Throwable error)
        {
            StringBuilder stringBuilder = new StringBuilder();

            for (StackTraceElement stackTraceElement : error.getStackTrace()) {

                stringBuilder.append(stackTraceElement.toString() + "\n");

            }
            return stringBuilder.toString();
        }
    public static void printFile(String string, String nameFile)
    {
        try{
            nameFile += ".txt";
            FileWriter writer=new FileWriter(nameFile, false);
            writer.write( string+"\n" );
            writer.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File wasn't found!");
        }
        catch(IOException e)
        {
            System.out.println("Err");
        }
    }
    public static UUID getUuid() {
        return uuid;
    }

    public static void setUuid(UUID uuid) {
        Debug.uuid = uuid;
    }
}
