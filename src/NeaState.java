import com.sun.javaws.exceptions.InvalidArgumentException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Copyright 2014 blastbeat syndicate gmbh
 * Author: Roger Jaggi <roger.jaggi@blastbeatsyndicate.com>
 * Date: 17.01.14
 * Time: 10:34
 */
public abstract class NeaState {



    private List<NeaState> validFollingStateLeft = new ArrayList<NeaState>();
    private List<NeaState> validFollingStateRight = new ArrayList<NeaState>();

    public String getName() {
        return name;
    }

    private String name;

    public List<NeaState> getValidFollingStateLeft() {
        return validFollingStateLeft;
    }

    public List<NeaState> getValidFollingStateRight() {
        return validFollingStateRight;
    }

    public void addValidFollingStateOneLeft(NeaState state) {
        Objects.requireNonNull(state);

        this.validFollingStateLeft.add(state);
    }

    public void addValidFollingStateOneRight(NeaState state) {
        Objects.requireNonNull(state);

        this.validFollingStateRight.add(state);
    }

    public String getListOfFollingStatesLeft() {
        StringBuilder sb = new StringBuilder();
        for(NeaState state : this.getValidFollingStateLeft()) {
            sb.append(state.getName() + ",");
        }
        return sb.toString();
    }

    public String getListOfFollingStatesRight() {
        StringBuilder sb = new StringBuilder();
        for(NeaState state : this.getValidFollingStateRight()) {
            sb.append(state.getName() + ",");
        }
        return sb.toString();
    }

    public NeaState(String name) {
        this.name = name;
    }

    public String getContainingToken(String term, char openingChar, char closingChar) throws ParseException {
        int[] pos = findSourroundingChars(term, openingChar, closingChar, 0);
        if(pos.length > 0) {
            return term.substring(pos[0] + 1,pos[1]).trim();
        }
        else {
            return null;
        }
    }

    public int[] findSourroundingChars(String term, char openingChar, char closingChar, int startPos) throws ParseException {
        int stack = 0;
        int posFirstChar = -1;
        int posLastChar = -1;
        for(int i = startPos; i < term.length(); i++) {
            if(term.charAt(i) == openingChar) {
                stack += 1;
                if(posFirstChar == -1) {
                    posFirstChar = i;
                }
            }
            if(term.charAt(i) == closingChar) {
                stack -= 1;
                if(stack == 0) {
                    if(posFirstChar != -1) {
                        return new int[]{posFirstChar, i};
                    }
                    else {
                        throw new ParseException("Closing Char " + closingChar + " ohne öffnendes Char " + openingChar + " in " + term + " gefunden!", 0);
                    }
                }
            }
        }

        return new int[]{};
    }

    public List<String> splitListSave(String term, String splitToken, int startPos) {
        int stack = 0;
        List<String> result = new ArrayList<String>();
        int lasPos = 0;
        int correctCount = 0;

        for(int i = startPos; i < term.length(); i++) {
            if(term.charAt(i) == '(' || term.charAt(i) == '[') {
                stack += 1;
            }
            if(term.charAt(i) == ')' || term.charAt(i) == ']') {
                stack -= 1;
            }
            if(term.charAt(i) == splitToken.charAt(correctCount) && stack == 0) {

                if((correctCount + 1) == splitToken.length()) {
                    result.add(term.substring(lasPos, i - (correctCount)).trim());
                    lasPos = i + 1;
                }
                else {
                    correctCount += 1;
                }

            }
            else {
                correctCount = 0;
            }

        }

        result.add(term.substring(lasPos).trim());

        return (stack == 0 ? result : new ArrayList<String>());
    }

    public boolean parseWithOneOfFollowingStatesLeft(String containingToken) throws ParseException {
        for(NeaState state : this.getValidFollingStateLeft()) {
            if(state.parseToken(containingToken)) {
                return true;
            }
        }
        throw new ParseException("None of the folling States [" + getListOfFollingStatesLeft() + "] of State [" + this.name + "] returned true for: " + containingToken ,0);
    }

    public boolean parseWithOneOfFollowingStatesRight(String containingToken) throws ParseException {
        for(NeaState state : this.getValidFollingStateRight()) {
            if(state.parseToken(containingToken)) {
                return true;
            }
        }
        throw new ParseException("None of the folling States [" + getListOfFollingStatesRight() + "] of State [" + this.name + "] returned true for: " + containingToken ,0);
    }

    /**
     * parses a Token
     * @param token the token
     * @return returns true if valid
     */
    public abstract boolean parseToken(String token) throws ParseException;



}
