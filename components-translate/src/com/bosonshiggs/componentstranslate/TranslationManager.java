package com.bosonshiggs.ComponentsTranslate;

import android.content.Context;
import android.content.SharedPreferences;

public class TranslationManager {
    private static final String PREFERENCES_NAME = "TranslationManager";

    protected void saveTranslation(Context context, String originalText, String translatedText) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(translatedText, originalText); // Tradução para texto original
        editor.commit();
    }

    protected String getOriginalText(Context context, String translatedText) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(translatedText, translatedText); // Retorna o original ou o próprio texto se não encontrado
    }
}

