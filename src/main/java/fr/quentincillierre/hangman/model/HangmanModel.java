package fr.quentincillierre.hangman.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class HangmanModel {
    private final String wordToGuess;
    private final int maxWrongs;
    private int currentWrongs;
    private Set<Character> guessedLetter;

    public HangmanModel(String wordToGuess) {
        this.wordToGuess = wordToGuess.toUpperCase();
        this.maxWrongs = 10;
        this.currentWrongs = 0;
        this.guessedLetter = new LinkedHashSet<>();
    }

    

    public Set<Character> getGuessedLetter() {
        return guessedLetter;
    }

    public int getCurrentWrongs() {
        return currentWrongs;
    }

    public int getMaxWrongs() {
    return maxWrongs;
}

     public String getWordToGuess(){
        return this.wordToGuess;
     }

    public void tryLetter(Character letter){
        letter = Character.toUpperCase(letter);
        if (this.guessedLetter.contains(letter)){
            return;
        }
        if (!wordToGuess.contains(letter.toString())){
            currentWrongs++;
        }
        guessedLetter.add(letter);
    }

    public String getHiddenWord(){
        StringBuilder hiddenWord = new StringBuilder();

        for (int i = 0; i < wordToGuess.length(); i++){
            char letter = wordToGuess.charAt(i);
            if (!Character.isLetter(letter)) {
                hiddenWord.append(letter);
            } else if (guessedLetter.contains(letter)){
                hiddenWord.append(letter);
            } else {
                hiddenWord.append('_');
            }
            if (i < wordToGuess.length() - 1) {
                hiddenWord.append(' ');
            }
        }
        return hiddenWord.toString();
    }

    public boolean isWin(){
        for (Character letter : this.wordToGuess.toCharArray()){
            if (!this.guessedLetter.contains(letter))
                return false;
        }
        return true;
    }

    public boolean isLose(){
        return currentWrongs >= maxWrongs;
    }

    public static void main(String[] args){
        HangmanModel game = new HangmanModel("java");
        game.tryLetter('R');
        System.out.println(game.getHiddenWord());
        game.tryLetter('A');
        System.out.println(game.getHiddenWord());
        System.out.println(game.isWin() ? "WIN" : "KEEP PUSHING");
        game.tryLetter('v');
        game.tryLetter('j');
        System.out.println(game.isWin() ? "WIN" : "KEEP PUSHING");
        game.tryLetter('g');
        game.tryLetter('g');
        game.tryLetter('g');
        game.tryLetter('g');
        game.tryLetter('g');
        game.tryLetter('g');
        game.tryLetter('g');
        game.tryLetter('g');
        System.out.println(game.isLose() ? "LOSE" : "KEEP PUSHING");
        game.tryLetter('g');
        System.out.println(game.isLose() ? "LOSE" : "KEEP PUSHING");

        System.out.println(game.getGuessedLetter());

        System.out.println();




    }



}
