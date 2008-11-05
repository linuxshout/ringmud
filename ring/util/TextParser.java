package ring.util;

/**
 * <p>Title: RingMUD Codebase</p>
 * <p>Description: RingMUD is a java codebase for a MUD with a working similar to DikuMUD</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: RaiSoft/Thermetics</p>
 * @author Jeff Hair
 * @version 1.0
 */

import java.util.*;

//THIS CLASS NEEDS TO BE REWRITTEN FROM SCRATCH FOR TWO PURPOSES:
//TO NOT HAVE TO COMPLY WITH WOLFMUD'S LICENSE AGREEMENT, AND TO CLEAN THIS DAMN THING UP!

public class TextParser {
  //Constants
  public static final int SCREEN_WIDTH = 80;

  public TextParser() {
  }

  public static int countParameters(String str) {
    //return zero for parameters with zero parameters; fixes a bug...
    if (str.indexOf("()") != -1) return 0;

    char[] chars = str.toCharArray();
    int commas = 0;

    for (int c = 0; c < chars.length; c++) {
      if (chars[c] == ',') {
        commas++;
      }
    }

    commas++; //account for that last parameter

    return commas;
  }

  //This method is used to replace the color tags with the correct ANSI codes.
  public static String parseOutgoingData(String data) {
    /*String data2 = data;
    data2 = data2.replace("[BLACK]", "");
    data2 = data2.replace("[BLUE]", "");
    data2 = data2.replace("[CYAN]", "");
    data2 = data2.replace("[GREEN]", "");
    data2 = data2.replace("[MAGENTA]", "");
    data2 = data2.replace("[RED]", "");
    data2 = data2.replace("[WHITE]", "");
    data2 = data2.replace("[YELLOW]", "");
    data2 = data2.replace("[B]", "");
    data2 = data2.replace("[R]", "");

    //Black.
    data = data.replace("[BLACK]", "\033[30m");

    //Blue.
    data = data.replace("[BLUE]", "\033[34m");

    //Cyan.
    data = data.replace("[CYAN]", "\033[36m");

    //Green.
    data = data.replace("[GREEN]", "\033[32m");

    //Magenta.
    data = data.replace("[MAGENTA]", "\033[35m");

    //Red.
    data = data.replace("[RED]", "\033[31m");

    //White.
    data = data.replace("[WHITE]", "\033[37m");

    //Yellow.
    data = data.replace("[YELLOW]", "\033[33m");

    //Bold.
    data = data.replace("[B]", "\033[1m");

    //Regular.
    data = data.replace("[R]", "\033[22m");

    //Count the characters. If they're greater than SCREEN_WIDTH, the lines need to be return-
    //spaced somehow.

    String output = data;
    String res = "";
    int charactersOnLine = data2.length();
    int inserts = (int)(charactersOnLine / SCREEN_WIDTH);
    for (int c = 0; c < inserts; c++) {
      System.out.println("output: " + output);
      if (charactersOnLine > SCREEN_WIDTH) {
        int charactersToGoBack = charactersOnLine - SCREEN_WIDTH;
        int indexOfNewLine = output.lastIndexOf(" ", (charactersOnLine - charactersToGoBack));
        String temp = output.substring(indexOfNewLine);
        System.out.println("Inserting at: " + indexOfNewLine);
        output = insert(output, "\r\n", indexOfNewLine);
        res += output;
        output = temp;
      }
    }

    System.out.println("RES: " + res);
    data = res;*/
    int characterCount = 0;
         int wordCount = 0;
         int screenWidth = 80;
         boolean command = false;
         boolean commandProcessed = false;
         String[] inlineCommands = new String[] {"BLACK", "BLUE", "CYAN", "GREEN", "MAGENTA", "RED", "WHITE", "YELLOW", "B", "R"};

         // set a tokeniser to parse the string
         StringTokenizer tok = new StringTokenizer(data, "\n[] \t", true);

         // buffer to hold outgoing data as we build it
         StringBuffer output = new StringBuffer("\033[37m");

         // loop through each of the tokens of the string to display.
         while (tok.hasMoreTokens()) {
      data = tok.nextToken();
     // System.out.println("CURRENTLY PARSING: <" + data + ">");

      // starting or ending a command ?
      if (data.equals("[")) {
       // System.out.println("It's a command.");
        command = true;
        continue;
      }
      if (data.equals("]")) {
       // System.out.println("Ending a command.");
        command = false;
        continue;
      }

      if (data.equals("\n")) {
       // System.out.println("Reseting everything?");
        data = "\r\n";
        characterCount = 0;
        wordCount = 0;
      }

      // an inline command ?
     // System.out.println("Is it an in-line command?");
      commandProcessed = false;
      if (command) {
       // System.out.println("Yes! Using the crappy thing to change to the ansi code!");
        for (int x = 0; x < inlineCommands.length; x++) {
          if (inlineCommands[x].equals(data)) {
            switch (x) {
              case 0:
                data = "\033[30m";
                break;

              case 1:
                data = "\033[34m";
                break;

              case 2:
                data = "\033[36m";
                break;

              case 3:
                data = "\033[32m";
                break;

              case 4:
                data = "\033[35m";
                break;

              case 5:
                data = "\033[31m";
                break;

              case 6:
                data = "\033[37m";
                break;

              case 7:
                data = "\033[33m";
                break;

              case 8:
                data = "\033[1m";
                break;

              case 9:
                data = "\033[22m";
                break;
            }
            commandProcessed = true;
           // System.out.println("Parsed the command, continuing on!");
            break;
          }
        }

        // if it wasn't a command display the text instead
        if (!commandProcessed) {
         // System.out.println("It wasn't a command so we're going to display it instead.");
          data = "[" + data + "]";
         // System.out.println("The new data string is: " + data);
          wordCount = data.length() + 2;
         // System.out.println("The word count is: " + wordCount);
        } else {
         // System.out.println("We've hit an else block... wordCount = 0.");
          wordCount = 0;
        }
      } else {
       // System.out.println("The other else block...");
        wordCount = data.length();
      //  System.out.println("The wordCount is: " + wordCount);
      }

      if (screenWidth != 0 &&
          (characterCount == 0 || characterCount == screenWidth) &&
        data.equals(" ")) {/*System.out.println("hit the one if statement...");*/ continue;}

     // System.out.println("Adding the wordCount to characterCount...");
      characterCount += wordCount;
     // System.out.println("characterCount is: " + characterCount);

      if (screenWidth != 0 && characterCount > screenWidth) {
      //  System.out.println("The line is too big, so we're indenting it now...");
        output.append("\r\n");
        characterCount = wordCount;
      //  System.out.println("We've finished indenting.");
      }

     // System.out.println("Appending <data> to <output>");
     output.append(data);
     // System.out.println("<output> so far: " + output);
     // System.out.println("----------------------------------------------");
     // System.out.println();
         }

          output.append("\033[37m");

    return output.toString();
}


  public static String indefiniteArticle(String text) {
    String[] tokens = text.split(" ");

    String plurality = (tokens[0].substring(tokens[0].length() - 1));

    System.out.println(plurality);

    return "meh";
  }

  //insert method.
  //This method inserts a string into a given position of another string.
  //IT WILL INSERT IN BETWEEN THE POSITION GIVEN AND THE ONE AFTER POS (POS + 1).
  public static String insert(String theString, String stringToBeInserted,
                              int pos) {
    String beforeString = theString.substring(0, pos + 1);
    String endString = theString.substring(pos + 1, theString.length() - 1);
    beforeString += stringToBeInserted;
    beforeString += endString;

    return beforeString;
  }

  public static void main(String[] args) {
    String s = "hi there my name is really big lets make it bigger yepp yep whooo!! long string that will hopefully be more than eighty characters when im done typing it i hope it is now whoooOO!!!";
    System.out.println(s);
    System.out.println(s.length());
    s = parseOutgoingData(s);
    System.out.println(s);
  }
}
