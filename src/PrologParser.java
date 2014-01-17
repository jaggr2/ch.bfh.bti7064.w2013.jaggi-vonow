import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

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
                if(line.trim().length() <= 0) {  // filtere Leerzeilen
                    line = br.readLine();
                    continue;
                }

                String firstChar = line.trim().substring(0,1); // Filtere Kommentarzeilen
                if(firstChar.equals("%")) {
                    line = br.readLine();
                    continue;
                }

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
                /* sample moves */

                sb.append(line);
                //sb.append(System.lineSeparator());
                line = br.readLine();
            }
            entireFile = sb.toString();
        } finally {
            br.close();
        }


        /*
        NeaState konstante = new NeaState("Konstante", "[[a-z]{1}[a-zA-Z0-9_]*|[0-9]+]{1}");
        NeaState variable = new NeaState("Variable", "[A-Z_]{1}[a-zA-Z0-9_]*");
        NeaState struktur = new NeaState("Stuktur", "[a-z_]+\\(\\Anweisung\\)");
        NeaState anweisung = new NeaState("Anweisung", "([\\Variable|\\Konstante|\\Struktur|\\Liste]*[,|]{0,1}])+");
        NeaState liste = new NeaState("Liste", "\\[\\Anweisung*\\]");
        NeaState zuweisung = new NeaState("Zuweisung", "[\\Strukur|\\Variable|\\Konstante]:-\\Anweisung");
        NeaState prologterm = new NeaState("Term", "[\\Struktur|\\Zuweisung]{1}\\.");
        */

        // define the NEA Automat
        NeaState konstante = new NeaState("Konstante")  {
            @Override
            public boolean parseToken(String token) throws ParseException {
                return token.matches("[[a-z]{1}[a-zA-Z0-9_]*|[0-9]+]");
            }
        };

        NeaState variable = new NeaState("Variable") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                return token.matches("[A-Z_][a-zA-Z0-9_]*");
            }
        };

        NeaState struktur = new NeaState("Struktur") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                if(!token.matches("[a-z_]+\\(.+")) {
                    return false;
                }

                String containingToken = getContainingToken(token, '(',')');
                if(containingToken == null) {
                    return false;
                }

                return parseWithOneOfFollowingStatesLeft(containingToken);
            }
        };

        NeaState anweisung = new NeaState("Anweisung") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                for(String subToken : splitListSave(token, ",", 0)) {
                    if(parseWithOneOfFollowingStatesLeft(subToken)) {
                        return true;
                    }
                }

                return false;
            }
        };

        NeaState liste = new NeaState("Liste") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                if(!token.matches("\\[.*\\]")) {
                    return false;
                }

                if(token.equals("[]")) {
                    return true; // empty list
                }

                return parseWithOneOfFollowingStatesLeft(token.substring(1, token.length() - 1));
            }
        };

        NeaState zuweisung = new NeaState("Zuweisung") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                if(!token.matches(".*:-.*")) {
                    return false;
                }

                List<String> parts = splitListSave(token, ":-", 0);
                if(parts.size() != 2) {
                    return false;
                }

                return parseWithOneOfFollowingStatesLeft(parts.get(0)) && parseWithOneOfFollowingStatesRight(parts.get(1));
            }
        };

        NeaState term = new NeaState("Term") {
            @Override
            public boolean parseToken(String token) throws ParseException {
                if(!token.matches(".*\\.")) {
                    return false;
                }

                return parseWithOneOfFollowingStatesLeft(token.substring(0, token.length() - 1));
            }
        };


        struktur.addValidFollingStateOneLeft(anweisung);
        anweisung.addValidFollingStateOneLeft(variable);
        anweisung.addValidFollingStateOneLeft(konstante);
        anweisung.addValidFollingStateOneLeft(struktur);
        anweisung.addValidFollingStateOneLeft(liste);
        liste.addValidFollingStateOneLeft(anweisung);
        zuweisung.addValidFollingStateOneLeft(struktur);
        zuweisung.addValidFollingStateOneLeft(variable);
        zuweisung.addValidFollingStateOneLeft(konstante);
        zuweisung.addValidFollingStateOneRight(anweisung);
        term.addValidFollingStateOneLeft(struktur);
        term.addValidFollingStateOneLeft(zuweisung);


        String tokens[] = entireFile.split("\\.");


        for(int i = 0; i < tokens.length; i++) {
            try
            {
                Boolean result = term.parseToken(tokens[i].trim() + ".");
                System.out.print("Result " + i + " for " + tokens[i] + " -> ");
                System.out.println(result);
            }
            catch (ParseException ex) {
                System.out.println("Result " + i + " for " + tokens[i] + " -> Exception:");
                ex.printStackTrace();
            }


        }
	}

}