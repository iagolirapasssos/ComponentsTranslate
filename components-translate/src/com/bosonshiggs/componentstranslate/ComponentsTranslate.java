package com.bosonshiggs.ComponentsTranslate;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.util.FileStreamReadOperation;

import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

@DesignerComponent(
    version = 2,
    versionName = "1.3",
    description = "Extension to translate components using a JSON file with translations.",
    iconName = "icon.png",
    category = ComponentCategory.EXTENSION,
    nonVisible = true
)
@SimpleObject(external = true)
public class ComponentsTranslate extends AndroidNonvisibleComponent {

    private JSONObject translations = new JSONObject(); // Armazena as traduções carregadas
    private String targetLanguage = "en"; // Idioma padrão

    public ComponentsTranslate(ComponentContainer container) {
        super(container.$form());
    }

    @DesignerProperty(
        editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
        defaultValue = "en"
    )
    @SimpleProperty(description = "Set the target language for translations (e.g., 'en' for English, 'es' for Spanish).")
    public void TargetLanguage(String language) {
        this.targetLanguage = language;
    }

    @SimpleProperty(description = "Returns the default language.",
                    category = PropertyCategory.BEHAVIOR)
    public String TargetLanguage() {
        return this.targetLanguage;
    }

    @SimpleFunction(description = "Load translations from a JSON file named 'translations.json' located in the assets directory.")
    public void LoadTranslations(final String fileName) {
        try {
            String fileNameBar = "//" + fileName;

            new FileStreamReadOperation(form, this, "LoadTranslations", fileNameBar, FileScope.Asset, true) {
                @Override
                public boolean process(String contents) {
                    try {
                        translations = new JSONObject(contents);
                        Log.i("ComponentsTranslate", "Translations loaded successfully from: " + fileNameBar);
                        form.runOnUiThread(() -> OnTranslationsLoaded(true, "Translations loaded successfully."));
                        return true;
                    } catch (JSONException e) {
                        Log.e("ComponentsTranslate", "Error parsing JSON: " + e.getMessage());
                        form.runOnUiThread(() -> OnJsonLoadError("Invalid JSON format."));
                        return false;
                    }
                }

                @Override
                public void onError(IOException e) {
                    Log.e("ComponentsTranslate", "Error reading file: " + e.getMessage());
                    form.runOnUiThread(() -> OnJsonLoadError("File not found or cannot be read."));
                }
            }.run();
        } catch (Exception e) {
            Log.e("ComponentsTranslate", "Unexpected error: " + e.getMessage());
            OnJsonLoadError("Unexpected error: " + e.getMessage());
        }
    }

    @SimpleFunction(description = "Translate a given text based on the loaded JSON file.")
	public String ReplaceTexts(String text) {
		try {
		    // Verifica se o texto traduzido é conhecido
		    String originalText = new TranslationManager().getOriginalText(form.getApplicationContext(), text);
		    
		    // Busca a tradução
		    String translatedText = getTranslation(originalText);

		    // Salva a nova tradução
		    new TranslationManager().saveTranslation(form.getApplicationContext(), originalText, translatedText);

		    Log.i("ComponentsTranslate", "Translation completed: " + text + " -> " + translatedText);
		    return translatedText;
		} catch (Exception e) {
		    Log.e("ComponentsTranslate", "Error translating text: " + text, e);
		    return text; // Retorna o texto original em caso de erro
		}
	}

    private String getTranslation(String originalText) {
		try {
		    if (translations.has(originalText)) {
		        JSONObject languageMap = translations.getJSONObject(originalText);
		        if (languageMap.has(targetLanguage)) {
		            return languageMap.getString(targetLanguage);
		        }
		    }
		} catch (JSONException e) {
		    Log.e("ComponentsTranslate", "Error getting translation for: " + originalText, e);
		}
		return originalText; // Retorna o texto original se não houver tradução
	}

    
    private String getOriginalText(String translatedText) {
		try {
		    // Itera por todas as chaves do JSON para encontrar o texto original
		    for (Object keyObj : translations.keySet()) {
		        String key = keyObj.toString(); // Converte a chave para String
		        JSONObject languageMap = translations.getJSONObject(key);

		        for (Object langObj : languageMap.keySet()) {
		            String lang = langObj.toString(); // Converte a chave do idioma para String
		            if (languageMap.getString(lang).equals(translatedText)) {
		                return key; // Retorna o texto original encontrado
		            }
		        }
		    }
		} catch (JSONException e) {
		    Log.e("ComponentsTranslate", "Error getting original text for: " + translatedText, e);
		}
		return null; // Retorna null se o texto original não for encontrado
	}

    @SimpleEvent(description = "Event fired when translations are successfully loaded.")
    public void OnTranslationsLoaded(boolean success, String message) {
        EventDispatcher.dispatchEvent(this, "OnTranslationsLoaded", success, message);
    }

    @SimpleEvent(description = "Event fired when there is an error loading the JSON file.")
    public void OnJsonLoadError(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "OnJsonLoadError", errorMessage);
    }
}

