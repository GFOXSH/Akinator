package Management;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JsonOdm, used to manage the different .json for the application
 * 
 * @author JOHN
 * 
 */
public class JsonOdm {
	// Get jsonSingleton
	private static JsonSingleton jsonSingleton = JsonSingleton.getInstance();

	/**
	 * findCharactersByQuestionKey return a collection of character according to
	 * the question key passed by parameter
	 * 
	 * @param question
	 *            a question
	 * @return
	 * @throws JSONException
	 */
	public JSONArray findCharactersByQuestionKey(String questionKey,String waitingResponse)
			throws JSONException {
		JSONArray charactersArray = new JSONArray();
		// Load question json Array
		JSONArray characters = jsonSingleton.getJsonPersonnages();

		// Get key for the question
		for (int i = 0; i < characters.length(); i++) {
			JSONObject character = characters.getJSONObject(i);
			String response = character.getString(questionKey);
			if (response.equals(waitingResponse)) {
				charactersArray.put(character);
			}
		}
		return charactersArray;
	}

	/**
	 * insertCharacter, insert a character and its questions
	 * @param characters a jsonObject character
	 * @throws IOException
	 */
	public void insertCharacter(JSONObject caracter){
		// Add character to Character JSON
		// Load question json Array
		jsonSingleton.getJsonPersonnages().put(caracter);
	}
	
	/**
	 * Used to insert a new question into JSONArray of questions
	 * @param key
	 * @param value
	 * @throws JSONException
	 */
	public void insertQuestion(String key, String value) throws JSONException{
		jsonSingleton.getJsonQuestions().getJSONObject(0).put(key,value);
	}

	/**
	 * getQuestion get a question by key from questions.json
	 * @param key a JSON key
	 * @return a toString question
	 * @throws JSONException 
	 */
	public String getQuestion(String key) throws JSONException {
		return jsonSingleton.getJsonQuestions().getJSONObject(0).getString(key);
	}
	
	/**
	 * Return a JSON Object character according to the name passed as parameter
	 * @param name
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getCharacterByName(String name) throws JSONException{
		JSONObject characterToReturn = null;
		for (int i = 0; i < jsonSingleton.getJsonPersonnages().length(); i++){
			JSONObject character = jsonSingleton.getJsonPersonnages().getJSONObject(i);
			String characterName = character.getString("Personnage");
			if(characterName.equals(name)){
				characterToReturn = character;
				break;
			}
		}
		return characterToReturn;
	}
	
	/**
	 * Used to delete a character by its name
	 * @param name
	 * @throws JSONException 
	 */
	public void deleteCharacterByName(String name) throws JSONException{
		JSONArray arrayTemp = new JSONArray();
		for (int i = 0; i < jsonSingleton.getJsonPersonnages().length(); i++){
			JSONObject character = jsonSingleton.getJsonPersonnages().getJSONObject(i);
			String characterName = character.getString("Personnage");
			if(!characterName.equals(name)){
				arrayTemp.put(character);
			}
		}
		JSONArray personnages = jsonSingleton.getJsonPersonnages();
		personnages = arrayTemp;
	}
	
	public void deleteCharacters(ArrayList<String> characters){
		
	}
}
