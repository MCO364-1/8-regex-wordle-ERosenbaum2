import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void properName() {
        assertTrue(Main.properName("Bob"));
        assertTrue(Main.properName("Smith"));
        assertTrue(Main.properName("Joey"));
        assertFalse(Main.properName("bob"));
    }

    @Test
    void integer() {
        assertTrue(Main.integer("12"));
        assertTrue(Main.integer("43.23"));
        assertTrue(Main.integer("-34.5"));
        assertTrue(Main.integer("+98.7"));
        assertTrue(Main.integer("0"));
        assertTrue(Main.integer("0.0230"));
        assertFalse(Main.integer("023"));
    }

    @Test
    void ancestor() {
        assertTrue(Main.ancestor("father"));
        assertTrue(Main.ancestor("mother"));
        assertTrue(Main.ancestor("grandfather"));
        assertTrue(Main.ancestor("grandmother"));
        assertTrue(Main.ancestor("great-grandfather"));
        assertTrue(Main.ancestor("great-grandmother"));
        assertTrue(Main.ancestor("great-great-grandfather"));
        assertTrue(Main.ancestor("great-great-grandmother"));
        assertFalse(Main.ancestor("notanancestor"));
    }

    @Test
    void palindrome() {
        assertTrue(Main.palindrome("asdfggfdsa"));
        assertTrue(Main.palindrome("aSdFGgfDsA"));
        assertFalse(Main.palindrome("aghjkllkjhga"));
        assertFalse(Main.palindrome("palindrome"));
    }

    private String guess;
    private List<Main.WordleResponse> round1;
    private List<String> possibleMatches;

    public void setUpWordleGuess(String wordGuess) {
        String targetWord = "SHLEP";
        guess = wordGuess;
        round1 = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            char letter = guess.charAt(i);
            if(letter == targetWord.charAt(i)) {
                round1.add(new Main.WordleResponse(letter, i, Main.LetterResponse.CORRECT_LOCATION));
            } else if(targetWord.indexOf(letter) != -1) {
                round1.add(new Main.WordleResponse(letter, i, Main.LetterResponse.WRONG_LOCATION));
            } else {
                round1.add(new Main.WordleResponse(letter, i, Main.LetterResponse.WRONG_LETTER));
            }
        }
        List<List<Main.WordleResponse>> allResponses = new LinkedList<>();
        allResponses.add(round1);
        possibleMatches = Main.wordleMatches(allResponses);
    }

    @Test
    public void upperCaseDictionary() {
        Set<String> dictionary = Main.loadDictionary();
        for(String word: dictionary) {
            assertEquals(word.toUpperCase(), word);
        }
    }

    @Test
    public void regexMatching() {
        setUpWordleGuess("TRAIN");
        for(String possibleMatch: possibleMatches) {
            for(Main.WordleResponse response: round1) {
                if(response.resp == Main.LetterResponse.WRONG_LETTER) {
                    assertNotEquals(response.c, possibleMatch.charAt(response.index));
                    assertFalse(possibleMatch.contains(String.valueOf(response.c)));
                }
                if(response.resp == Main.LetterResponse.WRONG_LOCATION) {
                    assertNotEquals(response.c, possibleMatch.charAt(response.index));
                    assertTrue(possibleMatch.contains(String.valueOf(response.c)));
                }
                if(response.resp == Main.LetterResponse.CORRECT_LOCATION) {
                    assertEquals(response.c, possibleMatch.charAt(response.index));
                }
            }
        }
    }

    @Test
    public void regexMatchingMultipleRounds() {
        String targetWord = "SHLEP";
        setUpWordleGuess("TRAIN");
        guess = "HELLO";
        List<Main.WordleResponse> round2 = new LinkedList<>();
        for(int i = 0; i < 5; i++) {
            char letter = guess.charAt(i);
            if(letter == targetWord.charAt(i)) {
                round2.add(new Main.WordleResponse(letter, i, Main.LetterResponse.CORRECT_LOCATION));
            } else if(targetWord.indexOf(letter) != -1) {
                round2.add(new Main.WordleResponse(letter, i, Main.LetterResponse.WRONG_LOCATION));
            } else {
                round2.add(new Main.WordleResponse(letter, i, Main.LetterResponse.WRONG_LETTER));
            }
        }
        List<List<Main.WordleResponse>> allResponses = new LinkedList<>();
        allResponses.add(round1);
        allResponses.add(round2);
        possibleMatches = Main.wordleMatches(allResponses);
        for(String possibleMatch: possibleMatches) {
            for(Main.WordleResponse response: round2) {
                if(response.resp == Main.LetterResponse.WRONG_LETTER) {
                    assertNotEquals(response.c, possibleMatch.charAt(response.index));
                    assertFalse(possibleMatch.contains(String.valueOf(response.c)));
                }
                if(response.resp == Main.LetterResponse.WRONG_LOCATION) {
                    assertNotEquals(response.c, possibleMatch.charAt(response.index));
                    assertTrue(possibleMatch.contains(String.valueOf(response.c)));
                }
                if(response.resp == Main.LetterResponse.CORRECT_LOCATION) {
                    assertEquals(response.c, possibleMatch.charAt(response.index));
                }
            }
        }
    }

    @Test
    public void previousGuessRemoved() {
        setUpWordleGuess("TRAIN");
        assertFalse(possibleMatches.contains(guess));
    }

    @Test
    public void winningGuess() {
        setUpWordleGuess("SHLEP");
        assertEquals(0, possibleMatches.size());
    }
}