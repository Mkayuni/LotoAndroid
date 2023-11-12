package com.example.lotoandroid;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordPairReader {
    private List<String> englishWords = new ArrayList<>();
    private List<String> chichewaWords = new ArrayList();

    public WordPairReader(Context context, String category) {
        String fileName = category + ".txt";
        readWordPairsFromTextFile(context, fileName);
    }

    private void readWordPairsFromTextFile(Context context, String fileName) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    englishWords.add(parts[0]);
                    chichewaWords.add(parts[1]);
                }
            }

            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getShuffledWordPairs() {
        if (!englishWords.isEmpty() && englishWords.size() == chichewaWords.size()) {
            List<String> wordPairs = new ArrayList<>();
            for (int i = 0; i < englishWords.size(); i++) {
                wordPairs.add(englishWords.get(i) + " - " + chichewaWords.get(i));
            }

            Collections.shuffle(wordPairs);
            return wordPairs;
        }
        return null; // No word pairs available
    }
}