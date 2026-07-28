package fr.quentincillierre.hangman.model;

import fr.quentincillierre.hangman.Difficulty;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class WordRepository {

    private final String fileName;

    // Holds word -> manually written clue (from the "WORD|clue" file format),
    // populated during loadWords() so it can be looked up after a word is chosen.
    private final Map<String, String> manualClues = new HashMap<>();

    public WordRepository(String fileName) {
        this.fileName = fileName;
    }

    public String getRandomWord() {
        return getRandomWord(Difficulty.EASY);
    }

    public String getRandomWord(Difficulty difficulty) {
        List<String> words = loadWords(this.fileName);
        if (words.isEmpty() && !"/words.txt".equals(this.fileName)) {
            words = loadWords("/words.txt");
        }

        List<String> filtered = filterWords(words, difficulty);
        if (filtered.isEmpty() && !words.isEmpty()) {
            filtered.addAll(words);
        }

        if (filtered.isEmpty()) {
            throw new IllegalStateException("No words available for category " + this.fileName + " at difficulty " + difficulty);
        }

        Random random = new Random();
        String selected = filtered.get(random.nextInt(filtered.size()));

        // Prefer a manually written clue from the file, if one was provided.
        String clue = manualClues.get(selected);
        if (clue == null || clue.isBlank()) {
            clue = generateClueFor(selected);
        }

        return selected + "|" + clue;
    }

    // Generate a simple, word-related clue for the selected resource word.
    // Only used as a FALLBACK when no manual clue was written in the file.
    private String generateClueFor(String word) {
        if (word == null || word.isBlank()) return "No clue available";
        String key = word.toUpperCase();

        // Small curated maps of common words to more meaningful clues.
        Map<String, String> musicClues = new HashMap<>();
        musicClues.put("GUITAR", "A stringed instrument often used in rock");
        musicClues.put("PIANO", "A keyboard instrument with hammers");
        musicClues.put("DRUMS", "Percussion instruments you hit");
        musicClues.put("VIOLIN", "A bowed string instrument");
        musicClues.put("TRUMPET", "A brass instrument with valves");
        musicClues.put("SAXOPHONE", "A woodwind instrument used in jazz");
        musicClues.put("FLUTE", "A woodwind instrument you blow across");
        musicClues.put("CELLO", "A low-pitched bowed string instrument");
        musicClues.put("HARP", "A large plucked string instrument");
        musicClues.put("JAZZ", "A music genre that originated in the US");
        musicClues.put("OPERA", "Dramatic singing performance art");
        musicClues.put("RAP", "Rhythmic spoken lyrics over a beat");

        Map<String, String> foodClues = new HashMap<>();
        foodClues.put("PIZZA", "Italian flatbread usually topped with cheese");
        foodClues.put("BURGER", "A sandwich with a ground-beef patty");
        foodClues.put("SPAGHETTI", "Long thin pasta often served with sauce");
        foodClues.put("ICE CREAM", "A frozen sweet dairy dessert");
        foodClues.put("SUSHI", "Japanese dish of rice and raw fish");
        foodClues.put("TACO", "A folded tortilla with fillings");
        foodClues.put("PASTA", "Generic Italian noodles or shapes");
        foodClues.put("SALAD", "A dish of mixed raw vegetables");
        foodClues.put("STEAK", "A cooked slice of beef");
        foodClues.put("CHOCOLATE", "Sweet made from cacao beans");

        Map<String, String> countryClues = new HashMap<>();
        countryClues.put("FRANCE", "European country known for Paris");
        countryClues.put("JAPAN", "Island nation in East Asia");
        countryClues.put("GERMANY", "European country known for Berlin");
        countryClues.put("BRAZIL", "Largest country in South America");
        countryClues.put("CANADA", "Large North American country north of the US");
        countryClues.put("AUSTRALIA", "Island continent and country");
        countryClues.put("CHINA", "Most populous country in the world");
        countryClues.put("INDIA", "South Asian country with a large population");
        countryClues.put("MEXICO", "North American country known for mariachi");
        countryClues.put("ITALY", "European country famous for pasta and Rome");

        Map<String, String> animalClues = new HashMap<>();
        animalClues.put("ELEPHANT", "Largest land animal");
        animalClues.put("DOLPHIN", "Intelligent marine mammal");
        animalClues.put("KANGAROO", "Hops and carries young in a pouch");
        animalClues.put("TIGER", "Large striped big cat");
        animalClues.put("LION", "King of the jungle");
        animalClues.put("GIRAFFE", "Tallest land animal with a long neck");
        animalClues.put("PENGUIN", "Flightless bird that swims");
        animalClues.put("RABBIT", "Long-eared hopping mammal");

        // Check category-specific maps first
        if ("/music.txt".equals(this.fileName) && musicClues.containsKey(key)) {
            return musicClues.get(key);
        }
        if ("/food.txt".equals(this.fileName) && foodClues.containsKey(key)) {
            return foodClues.get(key);
        }
        if ("/country.txt".equals(this.fileName) && countryClues.containsKey(key)) {
            return countryClues.get(key);
        }
        if ("/animal.txt".equals(this.fileName) && animalClues.containsKey(key)) {
            return animalClues.get(key);
        }

        // If not found in curated maps, try simple heuristics
        String lettersOnly = key.replaceAll("[^A-Z]", "");
        int letters = lettersOnly.length();
        if (key.contains(" ")) {
            // Multi-word entries: give a short descriptor
            return "Related phrase — " + letters + " letters";
        }

        // Default: show starts-with and letter count which is still helpful
        return String.format("Starts with '%s' — %d letters", key.substring(0, 1), letters);
    }

    private List<String> loadWords(String resourceName) {
        List<String> words = new ArrayList<>();
        InputStream is = getClass().getResourceAsStream(resourceName);
        if (is != null) {
            try (Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(is)))) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    String word;
                    String clue = null;

                    // Support "WORD|clue" format — split off the manual clue
                    // BEFORE storing the word, so it's never mixed into the
                    // word text itself.
                    if (line.contains("|")) {
                        String[] parts = line.split("\\|", 2);
                        word = parts[0].trim().toUpperCase();
                        clue = parts.length > 1 ? parts[1].trim() : null;
                    } else {
                        word = line.toUpperCase();
                    }

                    if (!word.isEmpty()) {
                        words.add(word);
                        if (clue != null && !clue.isEmpty()) {
                            manualClues.put(word, clue);
                        }
                    }
                }
            }
        }
        return words;
    }

    private List<String> filterWords(List<String> words, Difficulty difficulty) {
        List<String> filtered = new ArrayList<>();
        for (String word : words) {
            int letters = word.replaceAll("[^A-Z]", "").length();
            if (difficulty == Difficulty.EASY && letters <= 5) {
                filtered.add(word);
            } else if (difficulty == Difficulty.MEDIUM && letters >= 6 && letters <= 8) {
                filtered.add(word);
            } else if (difficulty == Difficulty.HARD && letters >= 9) {
                filtered.add(word);
            }
        }
        return filtered;
    }
}