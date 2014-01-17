import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Author: jaggr2, vonop1
 */
public class PrologParser {

	public static void main (String args[]) throws IOException, ParseException {

        if(args.length != 1) {
            System.out.println("ERROR: Bitte als einzigster Kommandozeilenparameter eine Prolog-Datei angeben.");
            return;
        }

        String entireFile = "";
        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                // filter empty lines
                if(line.trim().length() <= 0) {  // filtere Leerzeilen
                    line = br.readLine();
                    continue;
                }

                // filter comments beginning with %
                String firstChar = line.trim().substring(0,1); // Filtere Kommentarzeilen
                if(firstChar.equals("%")) {
                    line = br.readLine();
                    continue;
                }

                // filter comments of type /* sample comment */
                Integer start = 0;
                Integer end = line.indexOf("/*");
                String front;
                while(end >= 0) {
                    front = line.substring(start, end);

                    start = line.indexOf("*/", end);
                    if(start <= 0) {
                        throw new ParseException("Unclosing /* comment",0);
                    }
                    line = front + line.substring(start + 2);
                    end = line.indexOf("/*", start);
                }

                sb.append(line);
                line = br.readLine();
            }
            entireFile = sb.toString();
        } finally {
            br.close();
        }

        // define each State of the NEA machine
        NeaState konstante = new NeaState("Konstante")  {
            @Override
            public boolean parseToken(String token) throws ParseException {
                if(token.matches("([a-z][a-zA-Z0-9_]*|[0-9]+|!)")) {
                    System.out.println(token);
                    return true;
                }
                if(token.matches("'.*'")) {
                    System.out.println(token);
                    return true;
                }
                return false;
            }
        };

        NeaState variable = new NeaState("Variable") {
            @Override
            public boolean parseToken(String token) throws ParseException {

                if(token.matches("[A-Z_][a-zA-Z0-9_]*")) {
                    System.out.println(token);
                    return true;
                }
                return false;
            }
        };

        NeaState struktur = new NeaState("Struktur") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                if(!token.matches("[a-z_]+\\(.+")) {
                    return false;
                }

                System.out.println(token.substring(0, token.indexOf('(')));
                System.out.println("(");
                String containingToken = getContainingToken(token, '(',')');
                Boolean returnValue = parseWithOneOfFollowingStatesLeft(containingToken);
                System.out.println(")");

                return returnValue;
            }
        };

        NeaState anweisung = new NeaState("Anweisung") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                Boolean allTrue = true;

                List<String> tokenList = splitListSave(token, ",", 0);
                for(int i = 0; i < tokenList.size(); i++) {
                    allTrue = allTrue && parseWithOneOfFollowingStatesLeft(tokenList.get(i));
                    if( i < (tokenList.size() - 1)) { System.out.println(","); }
                }

                return allTrue;
            }
        };

        NeaState liste = new NeaState("Liste") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                if(!token.matches("\\[.*\\]")) {
                    return false;
                }

                if(token.equals("[]")) {
                    System.out.println("[");
                    System.out.println("]");
                    return true; // empty list
                }

                Boolean allTrue = true;
                System.out.println("[");
                List<String> tokenList = splitListSave(token.substring(1, token.length() - 1), "|", 0);
                for(int i = 0; i < tokenList.size(); i++) {
                    allTrue = allTrue && parseWithOneOfFollowingStatesLeft(tokenList.get(i));
                    if( i < (tokenList.size() - 1)) { System.out.println("|"); }
                }
                System.out.println("]");

                return allTrue;
            }
        };

        NeaState zuweisung = new NeaState("Zuweisung") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                if(!token.matches(".*:-.*")) {
                    return false;
                }

                Boolean allTrue = true;
                List<String> parts = splitListSave(token, ":-", 0);
                if (parts.size() == 2) {
                    allTrue = allTrue && parseWithOneOfFollowingStatesLeft(parts.get(0));
                    System.out.println(":-");
                    allTrue = allTrue && parseWithOneOfFollowingStatesRight(parts.get(1));
                    return allTrue;
                }
                return false;

            }
        };

        NeaState term = new NeaState("Term") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                return token.matches(".*\\.") && parseWithOneOfFollowingStatesLeft(token.substring(0, token.length() - 1));
            }
        };

        // add state machine connection
        struktur.addValidFollingStateOneLeft(anweisung);
        anweisung.addValidFollingStateOneLeft(liste);
        anweisung.addValidFollingStateOneLeft(variable);
        anweisung.addValidFollingStateOneLeft(konstante);
        anweisung.addValidFollingStateOneLeft(struktur);
        liste.addValidFollingStateOneLeft(anweisung);
        zuweisung.addValidFollingStateOneLeft(struktur);
        zuweisung.addValidFollingStateOneLeft(variable);
        zuweisung.addValidFollingStateOneLeft(konstante);
        zuweisung.addValidFollingStateOneRight(anweisung);
        term.addValidFollingStateOneLeft(zuweisung);
        term.addValidFollingStateOneLeft(struktur);

        String tokens[] = entireFile.split("\\.");

        for(int i = 0; i < tokens.length; i++) {
            try
            {
                Boolean result = term.parseToken(tokens[i].trim() + ".");
                if(!result) {
                    throw new ParseException("Invalid row " + i + "!",0);
                }
                System.out.println(".");
            }
            catch (ParseException ex) {
                System.out.println("Result " + i + " for " + tokens[i] + " -> Exception:");
                ex.printStackTrace();
            }
        }
	}

}