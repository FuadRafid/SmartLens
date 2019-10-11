package com.muhtasim.fuadrafid.smartlens.ocr;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class WordChecker
{
    private Set<String> wordsSet;

    public WordChecker(Context context)
    {

        wordsSet = new HashSet<>();
//Read text from file

        try {
            InputStreamReader isr = new InputStreamReader(context.getAssets().open("words.txt"));
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                wordsSet.add(line);
            }
        }
        catch (IOException e) {

        }
    }

    public boolean contains(String word)
    {
        return wordsSet.contains(word);
    }
}