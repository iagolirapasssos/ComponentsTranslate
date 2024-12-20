package com.bosonshiggs.ComponentsTranslate;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.util.FileStreamReadOperation;

import org.json.JSONObject;
import org.json.JSONException;

import android.util.Log;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@DesignerComponent(
    version = 3,
    versionName = "1.4",
    description = "Extension to translate components using a JSON file or URL with translations.",
    iconName = "icon.png"
)
public class ComponentsTranslate extends AndroidNonvisibleComponent {

    private JSONObject translations = new JSONObject(); // Armazena as traduções carregadas
    private String targetLanguage = "en"; // Idioma padrão
    private boolean loggingEnabled = false; // Logs habilitados por padrão

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

    @SimpleProperty(description = "Returns the target language.",
    				category = PropertyCategory.BEHAVIOR)
    public String TargetLanguage() {
        return this.targetLanguage;
    }

    @DesignerProperty(
        editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
        defaultValue = "true"
    )
    @SimpleProperty(description = "Enable or disable logging for debugging purposes.")
    public void LoggingEnabled(boolean enabled) {
        this.loggingEnabled = enabled;
    }

    @SimpleProperty(description = "Returns whether logging is enabled.",
    				category = PropertyCategory.BEHAVIOR)
    public boolean LoggingEnabled() {
        return this.loggingEnabled;
    }

    @SimpleFunction(description = "Load translations from a JSON file or URL.")
    public void LoadTranslations(String source) {
        if (source.startsWith("http://") || source.startsWith("https://")) {
            loadTranslationsFromUrl(source);
        } else {
            loadTranslationsFromFile(source);
        }
    }
    
    private void loadTranslationsFromFile(final String fileName) {
        try {
            String fileNameBar = "//" + fileName;
            new FileStreamReadOperation(form, this, "LoadTranslations", fileNameBar, FileScope.Asset, true) {
                @Override
                public boolean process(String contents) {
                    return processTranslations(contents, fileNameBar);
                }

                @Override
                public void onError(IOException e) {
                    logError("Error reading file: " + e.getMessage());
                    form.runOnUiThread(() -> OnJsonLoadError("File not found or cannot be read."));
                }
            }.run();
        } catch (Exception e) {
            logError("Unexpected error: " + e.getMessage());
            OnJsonLoadError("Unexpected error: " + e.getMessage());
        }
    }

    private void loadTranslationsFromUrl(String urlString) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    String contents = new Scanner(inputStream).useDelimiter("\\A").next();
                    inputStream.close();
                    processTranslations(contents, urlString);
                } else {
                    logError("HTTP error code: " + responseCode);
                    form.runOnUiThread(() -> OnJsonLoadError("Failed to load translations from URL."));
                }
            } catch (IOException e) {
                logError("Error loading URL: " + e.getMessage());
                form.runOnUiThread(() -> OnJsonLoadError("Failed to load translations from URL."));
            }
        }).start();
    }

    private boolean processTranslations(String contents, String source) {
        try {
            translations = new JSONObject(contents);
            logInfo("Translations loaded successfully from: " + source);
            form.runOnUiThread(() -> OnTranslationsLoaded(true, "Translations loaded successfully."));
            return true;
        } catch (JSONException e) {
            logError("Error parsing JSON: " + e.getMessage());
            form.runOnUiThread(() -> OnJsonLoadError("Invalid JSON format."));
            return false;
        }
    }
    
    @SimpleFunction(description = "Translate a given text based on the loaded JSON file.")
	public String ReplaceTexts(String text) {
		try {
		    // Verifica se o texto traduzido é conhecido
		    TranslationManager translationManager = new TranslationManager();
		    String originalText = translationManager.getOriginalText(form.getApplicationContext(), text);
		    
		    // Busca a tradução
		    String translatedText = getTranslation(originalText);

		    // Salva a nova tradução
		    translationManager.saveTranslation(form.getApplicationContext(), originalText, translatedText);

		    // Log da tradução, se habilitado
		    logInfo("Translation completed: " + originalText + " -> " + translatedText);
		    return translatedText;
		} catch (Exception e) {
		    logError("Error translating text: " + text + " | " + e.getMessage());
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
		    logError("Error getting translation for: " + originalText + " | " + e.getMessage());
		}
		return originalText; // Retorna o texto original se não houver tradução
	}
	
	@SimpleFunction(description = "Generates a direct download URL for a Google Drive file from its shared URL.")
    public String GoogleDriveUrlDirect(String sharedLink) {
        if (sharedLink == null || !sharedLink.contains("drive.google.com")) {
            return "Invalid Google Drive link.";
        }

        try {
            // Extract the file ID from the shared link
            String fileId = null;
            if (sharedLink.contains("/file/d/")) {
                fileId = sharedLink.split("/file/d/")[1].split("/")[0];
            } else if (sharedLink.contains("id=")) {
                fileId = sharedLink.split("id=")[1].split("&")[0];
            }

            if (fileId != null) {
                return "https://drive.google.com/uc?export=download&id=" + fileId;
            } else {
                return "Could not extract file ID from the link.";
            }
        } catch (Exception e) {
            return "Error processing the link: " + e.getMessage();
        }
    }


    private void logInfo(String message) {
        if (loggingEnabled) {
            Log.i("ComponentsTranslate", message);
        }
    }

    private void logError(String message) {
        if (loggingEnabled) {
            Log.e("ComponentsTranslate", message);
        }
    }

    @SimpleEvent(description = "Event fired when translations are successfully loaded.")
    public void OnTranslationsLoaded(boolean success, String message) {
        EventDispatcher.dispatchEvent(this, "OnTranslationsLoaded", success, message);
    }

    @SimpleEvent(description = "Event fired when there is an error loading the JSON file or URL.")
    public void OnJsonLoadError(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "OnJsonLoadError", errorMessage);
    }
}

