import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    //1
    public static boolean properName(String s) {
        return s.matches("[A-Z][a-z]+");
    }

    //2
    public static boolean integer(String s) {
        return s.matches("[+-]?(0|[1-9]\\d*)(\\.\\d+)?");
    }

    //3
    public static boolean ancestor(String s) {
        return s.matches("mother|father|(great-)*grand*(mother|father)");
    }

    //4
    public static boolean palindrome(String s) {
        s = s.toLowerCase();
        return s.matches("(.)(.)(.)(.)(.)\\5\\4\\3\\2\\1");
    }

    //5
    static class WordleResponse {
        char c;
        int index;
        LetterResponse resp;

        public WordleResponse(char c, int index, LetterResponse resp) {
            this.c = c;
            this.index = index;
            this.resp = resp;
        }
    }

    enum LetterResponse {
        CORRECT_LOCATION, // Green
        WRONG_LOCATION,   // Yellow
        WRONG_LETTER      // Gray
    }

    public static HashSet<String> loadDictionary() {
        HashSet<String> dictionaryWords = new HashSet<>();
        File file = new File("words.txt");
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (scanner.hasNextLine()) {
            dictionaryWords.add(scanner.nextLine().toUpperCase());
        }
        scanner.close();
        return dictionaryWords;
    }

    public static List<String> wordleMatches(List<List<WordleResponse>> responses) {
        HashSet<String> dictionary = loadDictionary();//List of all words in the wordle dictionary
        LinkedList<String> possibleMatches = new LinkedList<>(dictionary);//List of all current possible matches
        HashSet<String> guessedWords = new HashSet<>();//List of all previous guesses so they can be removed from the possible matches
        HashSet<Character> grayLetters = new HashSet<>();
        Map<Character, Set<Integer>> yellowLetters = new HashMap<>();
        Map<Integer, Character> greenLetters = new HashMap<>();
        for(List<WordleResponse> round: responses) {
            char[] guessChars = new char[5];
            for(int i = 0; i < 5; i++) {
                guessChars[i] = round.get(i).c;
            }
            String guessAsString = new String(guessChars);//Converting the 5 character guess into a string
            guessedWords.add(guessAsString);
            for(WordleResponse response: round) {
                guessedWords.add(String.valueOf(response.c));
                if(response.resp == LetterResponse.CORRECT_LOCATION) {
                    greenLetters.put(response.index, response.c);
                } else if(response.resp == LetterResponse.WRONG_LOCATION) {
                    if(!yellowLetters.containsKey(response.c)) {
                        yellowLetters.put(response.c, new HashSet<>());
                    }
                    yellowLetters.get(response.c).add(response.index);
                } else {
                    grayLetters.add(response.c);
                }
            }
            StringBuilder regex = new StringBuilder();
            Set<Character> yellowChars = yellowLetters.keySet();
            for(Character yellowLetter: yellowChars) {
                regex.append("(?=.*").append(yellowLetter).append(")");//The yellow letters should appear somewhere in the word
                Set<Integer> wrongIndexes = yellowLetters.get(yellowLetter);//Set that keeps track of yellow indexes
                for(Integer index: wrongIndexes) {
                    regex.append("(?!^.{").append(index).append("}").append(yellowLetter).append(")");//The yellow letters should not appear at the index it was found
                }
            }
            regex.append("^");
            for(int i = 0; i < 5; i++) {
                if(greenLetters.containsKey(i)) {
                    regex.append(greenLetters.get(i));//Match the green letter at this position
                } else {
                    if(grayLetters.isEmpty()) {
                        regex.append(".");//Match any letter if there are no gray letters
                    } else {
                        regex.append("[^");//Match any letter except for the gray letters
                        for(char grayLetter: grayLetters) {
                            regex.append(grayLetter);
                        }
                        regex.append("]");
                    }
                }
            }
            regex.append("$");
            LinkedList<String> filteredMatches = new LinkedList<>();//New list containing the possible matches after the round
            for(String word: possibleMatches) {
                if(word.matches(regex.toString())) {//Check if each word in the dictionary matches the regex
                    filteredMatches.add(word);
                }
            }
            possibleMatches = filteredMatches;
            possibleMatches.removeAll(guessedWords);//Remove the words that were already guessed
        }
        return possibleMatches;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HashSet<String> dictionary = loadDictionary();
        List<String> dictionaryWordsList = new LinkedList<>(dictionary);
        Random random = new Random();
        String targetWord = dictionaryWordsList.get(random.nextInt(dictionaryWordsList.size())).toUpperCase();
        List<List<WordleResponse>> allResponses = new LinkedList<>();
        System.out.println("Welcome to Wordle.");
        System.out.println("Try to guess the 5 letter word in 6 attempts.");
        System.out.println("You will get feedback on each guess: ");
        System.out.println("GREEN: Letter is correct and in the right position");
        System.out.println("YELLOW: Letter is in the word but in the wrong position");
        System.out.println("GRAY: Letter is not in the word");
        int round = 0;
        boolean hasWon = false;
        while(round < 6 && !hasWon) {
            String currentGuess = getGuess(round, scanner);
            while(currentGuess.length() != 5) {
                System.out.println("Please enter exactly 5 letters!");
                currentGuess = getGuess(round, scanner);
            }
            List<WordleResponse> responses = new LinkedList<>();
            for(int i = 0; i < 5; i++) {
                char guessedChar = currentGuess.charAt(i);
                if(guessedChar == targetWord.charAt(i)) {
                    responses.add(new WordleResponse(guessedChar, i, LetterResponse.CORRECT_LOCATION));
                } else if(targetWord.indexOf(guessedChar) != -1) {
                    responses.add(new WordleResponse(guessedChar, i, LetterResponse.WRONG_LOCATION));
                } else {
                    responses.add(new WordleResponse(guessedChar, i, LetterResponse.WRONG_LETTER));
                }
            }
            System.out.println("Feedback: ");
            for(WordleResponse response: responses) {
                String color;
                if(response.resp == LetterResponse.CORRECT_LOCATION) {
                    color = "GREEN";
                } else if(response.resp == LetterResponse.WRONG_LOCATION) {
                    color = "YELLOW";
                } else {
                    color = "GRAY";
                }
                System.out.println(response.c + " = " + color);
            }
            allResponses.add(responses);
            List<String> possibleMatches = wordleMatches(allResponses);
            possibleMatches.remove(currentGuess);
            System.out.println("Possible words remaining: " + possibleMatches.size());
            System.out.println("Words remaining:");
            for(String w: possibleMatches) {
                System.out.println(w);
            }
            if(currentGuess.equals(targetWord)) {
                hasWon = true;
                System.out.println("Congratulations! You've won in " + (round + 1) + " attempts! The word was: " + targetWord);
            }
            round++;
        }
        if(!hasWon) {
            System.out.println("Game Over:( The word was: " + targetWord);
        }
        scanner.close();
    }

    private static String getGuess(int attempts, Scanner scanner) {
        System.out.println("Attempt " + (attempts + 1) + "/6");
        System.out.print("Enter your guess (5 letters): ");
        return scanner.nextLine().toUpperCase();
    }
}