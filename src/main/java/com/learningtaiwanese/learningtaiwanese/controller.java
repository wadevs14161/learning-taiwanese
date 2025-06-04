package com.learningtaiwanese.learningtaiwanese;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Controller
public class controller {
    private final List<Map<String, String>> questions = new ArrayList<>();
    private final Random random = new Random();

    // Load questions from CSV file
    public controller() {
        String csvFilePath = "src/main/resources/data/words_with_options.csv";
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
            // Read the header
            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new IOException("CSV file is empty or invalid");
            }
            
            // Read the data rows
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                Map<String, String> record = new HashMap<>();
                for (int i = 0; i < headers.length && i < row.length; i++) {
                    record.put(headers[i], row[i]);
                }
                questions.add(record);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        }
    }

    @GetMapping("/") // Maps HTTP GET requests to the root URL
    public String index(Model model) { // Model is for passing data to the template
        if (questions.isEmpty()) {
            model.addAttribute("error", "No questions available");
            return "index";
        }

        // Select a random question
        Map<String, String> question = questions.get(random.nextInt(questions.size()));
        List<String> options = new ArrayList<>();
        options.add(question.get("Option1"));
        options.add(question.get("Option2"));
        options.add(question.get("Meaning"));
        Collections.shuffle(options); // Randomize order
        model.addAttribute("question", question);
        model.addAttribute("options", options);
        return "index";
    }
}
