## ComponentsTranslate

The `ComponentsTranslate` extension enables dynamic translation of text in your App Inventor components using a JSON file containing translations. The extension supports multiple languages and allows you to switch between languages seamlessly.

---

### Features
- Translate text in components dynamically.
- Support for multiple languages using a JSON file.
- Switch between languages without restarting the app.
- Automatically map translated text back to its original content.

---

### How to Use

#### 1. Load the JSON File
- Create a JSON file with translations.
- Add the file to your **Assets** in App Inventor.

Example JSON file (`translations.json`):

```json
{
  "Welcome to the App": {
    "en": "Welcome to the App",
    "es": "Bienvenido a la Aplicación",
    "fr": "Bienvenue dans l'Application",
    "pt": "Bem-vindo ao Aplicativo"
  },
  "Submit": {
    "en": "Submit",
    "es": "Enviar",
    "fr": "Soumettre",
    "pt": "Enviar"
  },
  "Cancel": {
    "en": "Cancel",
    "es": "Cancelar",
    "fr": "Annuler",
    "pt": "Cancelar"
  }
}
```

#### 2. Add the Extension
- Download the `.aix` file of the extension and import it into your App Inventor project.

#### 3. Initialize the Extension
Use the `LoadTranslations` block to load the JSON file:
```plaintext
When Screen1.Initialize
    Call ComponentsTranslate.LoadTranslations("translations.json")
    Call ComponentsTranslate.TargetLanguage("pt")
```

#### 4. Translate Text Dynamically
Translate a given text using the `ReplaceTexts` function:
```plaintext
When Button1.Click
    Set Label1.Text to Call ComponentsTranslate.ReplaceTexts("Welcome to the App")
```

The text will be translated based on the current target language.

---

### Block Details

#### **LoadTranslations**
- **Description**: Loads a JSON file with translations from the app's assets.
- **Parameters**: `fileName` (String) - The name of the JSON file in the assets.

#### **ReplaceTexts**
- **Description**: Translates the given text based on the loaded translations.
- **Parameters**: `text` (String) - The text to be translated.
- **Returns**: Translated text (String).

#### **TargetLanguage**
- **Description**: Sets or retrieves the target language for translations.
- **Parameters**: `language` (String) - The language code (e.g., `en`, `pt`, `fr`).

---

### Error Handling

If a translation is not found, the original text will be returned. Events such as `OnTranslationsLoaded` and `OnJsonLoadError` provide feedback on the loading process.

---

### Example Usage

#### Workflow in App Inventor
1. **Set the Target Language**:
   ```plaintext
   Call ComponentsTranslate.TargetLanguage("es")
   ```

2. **Translate Text**:
   ```plaintext
   Call ComponentsTranslate.ReplaceTexts("Submit")
   ```

3. **Switch Languages**:
   ```plaintext
   Call ComponentsTranslate.TargetLanguage("fr")
   ```

#### Result
- For `es`: `"Enviar"`
- For `fr`: `"Soumettre"`

---

### Developer Notes

#### Persistent Storage
The extension uses `SharedPreferences` to map translated text back to its original content dynamically. This ensures translations work regardless of the current language.

#### JSON File Format
- Each key represents the original text.
- Each value contains a mapping of language codes to translations.

---

### Contributing

Contributions are welcome! Feel free to open issues or submit pull requests to improve the functionality or documentation.

---

### License

This extension is licensed under the MIT License.

