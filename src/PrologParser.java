import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PrologParser {

	public static void main (String args[]) throws IOException {

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

                sb.append(line);
                //sb.append(System.lineSeparator());
                line = br.readLine();
            }
            entireFile = sb.toString();
        } finally {
            br.close();
        }

        // define the NEA Automat
        NeaState kostante = new NeaState("Konstante") {
            @Override
            public boolean parseToken(String token) {
                return token.matches("[[a-z]{1}[a-zA-Z0-9_]*|[0-9]+]");
            }
        };

        NeaState variable = new NeaState("Variable") {
            @Override
            public boolean parseToken(String token) {
                return token.matches("[A-Z_][a-zA-Z0-9_]*");
            }
        };

        NeaState struktur = new NeaState("Struktur") {
            @Override
            public boolean parseToken(String token) {
                if(!token.matches("[a-z_]+\\(.+")) {
                    return false;
                }

                String containingToken = getContainingToken(token, '(',')');
                if(containingToken == null) {
                    return false;
                }


                return token.matches("[A-Z_][a-zA-Z0-9_]*");


            }
        };

        /*
        NeaState konstante = new NeaState("Konstante", "[[a-z]{1}[a-zA-Z0-9_]*|[0-9]+]{1}");
        NeaState variable = new NeaState("Variable", "[A-Z_]{1}[a-zA-Z0-9_]*");
        NeaState struktur = new NeaState("Stuktur", "[a-z_]+\\(\\Anweisung\\)");
        NeaState anweisung = new NeaState("Anweisung", "([\\Variable|\\Konstante|\\Struktur|\\Liste]*[,|]{0,1}])+");
        NeaState liste = new NeaState("Liste", "\\[\\Anweisung*\\]");
        NeaState zuweisung = new NeaState("Zuweisung", "[\\Strukur|\\Variable|\\Konstante]:-\\Anweisung");
        NeaState prologterm = new NeaState("Term", "[\\Struktur|\\Zuweisung]{1}\\.");

        struktur.addAllowedState(anweisung);
        anweisung.addAllowedState(variable);
        anweisung.addAllowedState(konstante);
        anweisung.addAllowedState(struktur);
        anweisung.addAllowedState(liste);
        liste.addAllowedState(anweisung);
        zuweisung.addAllowedState(struktur);
        zuweisung.addAllowedState(variable);
        zuweisung.addAllowedState(konstante);
        zuweisung.addAllowedState(anweisung);
        prologterm.addAllowedState(struktur);
        prologterm.addAllowedState(zuweisung);
          */
        String tokens[] = entireFile.split("\\.");

        //prologterm.parseToken(tokens[0]);


	}

}