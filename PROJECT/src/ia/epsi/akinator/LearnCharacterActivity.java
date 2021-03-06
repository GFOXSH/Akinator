package ia.epsi.akinator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Management.Algorithm;
import Management.GameStatsManager;
import Management.JsonOdm;
import Management.JsonReader;
import Management.JsonSingleton;
import Management.JsonWriter;

public class LearnCharacterActivity extends Activity {

	// Declaration
	RadioButton buttonYes, buttonNo;
	Button buttonSave, buttonCancel;
	EditText characterName;
	EditText characterQuestion;
	TextView textViewFelicitation,textViewCharacter,textViewQuestionCharacter,TextViewResponse;
	RadioButton radioYes,radioNo;
	private JsonOdm jsonOdm;
	private JsonWriter jsonWriter;
	private JsonReader jsonReader;
	private JsonSingleton jsonSingleton;
	private HashMap<String, String> hashMapQuestionResponse;
	private Algorithm algorithm = new Algorithm(this.getBaseContext());
    private GameStatsManager statsManager;


	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_learn_character);

		// Assignement
		Intent intent = getIntent();
		hashMapQuestionResponse = (HashMap<String, String>) intent
				.getSerializableExtra("responses");
		buttonSave = (Button) findViewById(R.id.buttonSaveCharacter);
		buttonCancel = (Button) findViewById(R.id.buttonCancel);
		jsonOdm = new JsonOdm(getApplicationContext());
		jsonWriter = new JsonWriter(getApplicationContext());
		jsonReader = new JsonReader(getApplicationContext());
		jsonSingleton = JsonSingleton.getInstance(getApplicationContext());
        statsManager= new GameStatsManager(getApplicationContext());

		characterName = (EditText) findViewById(R.id.editTextNameCharacter);
		characterQuestion = (EditText) findViewById(R.id.EditTextQuestion);
		buttonYes = (RadioButton) findViewById(R.id.radioYes);
		buttonNo = (RadioButton) findViewById(R.id.radioNo);
		textViewFelicitation = (TextView)findViewById(R.id.textViewFelicitation);
		textViewCharacter = (TextView)findViewById(R.id.textViewCharacter);
		textViewQuestionCharacter = (TextView)findViewById(R.id.textViewQuestionCharacter);
		TextViewResponse = (TextView)findViewById(R.id.TextViewResponse);
		radioYes = (RadioButton)findViewById(R.id.radioYes);
		radioNo = (RadioButton)findViewById(R.id.radioNo);
		
		//mise en place du font
		Typeface typeFace=Typeface.createFromAsset(getAssets(),"brush.ttf");
		this.characterName.setTypeface(typeFace);
		this.characterQuestion.setTypeface(typeFace);
		this.buttonYes.setTypeface(typeFace);
		this.buttonNo.setTypeface(typeFace);
		this.buttonSave.setTypeface(typeFace);
		this.buttonCancel.setTypeface(typeFace);
		this.textViewFelicitation.setTypeface(typeFace);
		this.textViewCharacter.setTypeface(typeFace);
		this.textViewQuestionCharacter.setTypeface(typeFace);
		this.TextViewResponse.setTypeface(typeFace);
		this.radioYes.setTypeface(typeFace);
		this.radioNo.setTypeface(typeFace);


		// Button click
		this.buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LearnCharacterActivity.this,
						EndGameActivity.class);
				startActivity(intent);
			}
		});
		buttonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Get response for the new question
				String responseToNewQuestion = "inconnu";
				if (buttonYes.isChecked()) {
					responseToNewQuestion = "oui";
				} else {
					responseToNewQuestion = "non";
				}

				// TESTS
				String newQuestionKey = characterQuestion.getText().toString();
				String newQuestionValue = characterQuestion.getText().toString();

				// Reset the JSON's with values from start
				jsonSingleton.initializeJSONs();

				// Here test if personage doesn't already exists
				try {
					if (!jsonOdm.isCharacterAlreadyExists(characterName.getText()
							.toString().toUpperCase())) {
						fillNewPerso(newQuestionKey, newQuestionValue, responseToNewQuestion);
						
					} else {
						fillPersoWhichAlreadyExists(newQuestionKey, newQuestionValue, responseToNewQuestion);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Intent intent = new Intent(LearnCharacterActivity.this,
						EndGameActivity.class);
				startActivity(intent);

                //SAVE THE LOST GAME
                statsManager.insertGame(characterName.getText()
                        .toString(),new Date(),false);

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private void fillPersoWhichAlreadyExists(String newQuestionKey, String newQuestionValue, String responseToNewQuestion) throws JSONException, IOException
	{
		//If a personage already exists, we need to fill other informations unknows before and entered by the user
		//Get this perso 
		JSONObject personage = null;
		String persoName;
		personage = jsonOdm.getCharacterByName(characterName.getText().toString());
		if(personage == null){
			personage = jsonOdm.getCharacterByName(characterName.getText().toString().toUpperCase()); 
			persoName = characterName.getText().toString().toUpperCase();
		}else{
			persoName = characterName.getText().toString();
		}
		if(personage != null){
			//Fill it new properties if needed (different or named "inconnu")
			for (Map.Entry<String, String> entry : hashMapQuestionResponse.entrySet()) {
				String questionKey = entry.getKey();
				String response = entry.getValue();
				//Response comparison
				String responsePerso = personage.getString(questionKey);
				if(!responsePerso.equals(algorithm.getResponseByCode(response))){
					personage.put(questionKey,algorithm.getResponseByCode(response));
				}
			}
			Log.i("THE FUCKING PERSONNAGE WITH NEW VALUES",personage.toString());
			//json Character with this perso
			//First delete perso to refill it
			jsonOdm.deleteCharacterByName(persoName);
			jsonOdm.insertCharacter(personage);
			JSONArray jsonPersonnageWithNewCharacterFilled = jsonOdm.getJsonCharacter();
			//Write personnages
			jsonWriter.writeJsonIntoInternalStorage(jsonPersonnageWithNewCharacterFilled.toString(), "personnages.json");
			//Go on
		}
	}
	
	private void fillNewPerso(String newQuestionKey, String newQuestionValue, String responseToNewQuestion){
		// Add new character to JSON with his responses
		JSONObject newCharacter = new JSONObject();
		// Fill character with question responded before
		for (Map.Entry<String, String> entry : hashMapQuestionResponse
				.entrySet()) {
			String questionKey = entry.getKey();
			String response = entry.getValue();
			try {
				// Test here "prob oui" , "prob non"
				newCharacter.put(questionKey,
						algorithm.getResponseByCode(response));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// Fille character with other question unresponded
		JSONArray questionsFromMemory = jsonSingleton
				.getJsonQuestions();
		ArrayList<String> arrayTampon = new ArrayList<String>();
		try {
			JSONObject questions = questionsFromMemory.getJSONObject(0);
			// Log.i("QUESTIONS ", questions.toString());

			Iterator<?> keys = questions.keys();
			boolean isAlreadyInCharacter = false;
			while (keys.hasNext()) {
				if (isAlreadyInCharacter) {
					isAlreadyInCharacter = false;
				}
				String questionKey = (String) keys.next();
				Iterator<?> keysOnPerso = newCharacter.keys();
				while (keysOnPerso.hasNext()
						&& !isAlreadyInCharacter) {
					String questionKeyPerso = (String) keysOnPerso
							.next();
					if (questionKey.equals(questionKeyPerso)) {
						isAlreadyInCharacter = true;
					}
				}
				// If the question isn't already defined for the new
				// character, add it
				if (!isAlreadyInCharacter) {
					arrayTampon.add(questionKey);
				}
			}
			// Add all missing keys for the new personage
			for (String key : arrayTampon) {
				newCharacter.put(key, "inconnu");
			}

		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// Put personage name
		try {
			newCharacter.put("Personnage", characterName.getText()
					.toString().toUpperCase());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Insert question
		try {
			jsonOdm.insertQuestion(newQuestionKey, newQuestionValue);// Insertion
																		// OK
																		// in
																		// internal
																		// storage
			// Fill new question and its response
			newCharacter.put(newQuestionKey, responseToNewQuestion);

			// Fill the new question key for all characters already
			// in json personnages
			JSONArray arrayPersoTampon = new JSONArray();
			JSONArray arrayPersonnagesInMemory = new JSONArray(
					jsonReader
							.readJSONfromInternalStorage("personnages.json"));
			for (int i = 0; i < arrayPersonnagesInMemory.length(); i++) {
				JSONObject perso = arrayPersonnagesInMemory
						.getJSONObject(i);
				perso.put(newQuestionKey, "inconnu");

				arrayPersoTampon.put(perso);
			}
			jsonSingleton
					.setJsonPeronnages(arrayPersoTampon);
            Log.i("LEARN ACTIVITY CHARACTER ARRAY TAMPON FROM MEMORY : ",
                    arrayPersoTampon.toString()    );
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Insert new character
		jsonOdm.insertCharacter(newCharacter);// TODO Check
												// insertion

		try {
			// Write new json personnages
			jsonWriter.writeJsonIntoInternalStorage(jsonOdm
					.getJsonCharacter().toString(),
					"personnages.json");
			// Write new json questions
			jsonWriter.writeJsonIntoInternalStorage(jsonOdm
					.getJsonQuestions().toString(),
					"questions.json");
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Log.i("LEARN ACTIVITY PERSONNAGES FROM INTERNAL STORAGE",
					jsonReader
							.readJSONfromInternalStorage("personnages.json"));
			Log.i("LEARN ACTIVITY QUESTIONS FROM INTERNAL STORAGE",
					jsonReader
							.readJSONfromInternalStorage("questions.json"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
