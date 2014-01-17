import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2014 blastbeat syndicate gmbh
 * Author: Roger Jaggi <roger.jaggi@blastbeatsyndicate.com>
 * Date: 17.01.14
 * Time: 10:34
 */
public abstract class NeaState {

    public String getName() {
        return name;
    }

    private String name;

    public NeaState(String name) {
        this.name = name;
    }

    public String getContainingToken(String term, char openingChar, char closingChar) {
        int[] pos = findSourroundingChars(term, openingChar, closingChar);
        if(pos.length > 0) {
            return term.substring(pos[0],pos[1]);
        }
        else {
            return null;
        }
    }

    public int[] findSourroundingChars(String term, char openingChar, char closingChar) {
        int stack = 0;
        int posFirstChar = -1;
        int posLastChar = -1;
        for(int i = 0; i < term.length(); i++) {
            if(term.charAt(i) == openingChar) {
                stack += 1;
                if(posFirstChar != -1) {
                    posFirstChar = i;
                }
            }
            if(term.charAt(i) == closingChar) {
                stack -= 1;
                if(stack == 0) {
                    posLastChar = i;
                }
            }
        }

        if(posFirstChar != -1 && posLastChar != -1) {
            return new int[]{posFirstChar, posLastChar};
        }
        else {
            return new int[]{};
        }
    }

    /**
     * parses a Token
     * @param token the token
     * @return returns true if valid
     */
    public abstract boolean parseToken(String token);
    /*{
        /*
        Boolean isChoice
        for(int i = 0; i < term.length(); i++) {
            if(term.charAt(i) == '[') {

            }


        }

        if(token)


    } */


}
