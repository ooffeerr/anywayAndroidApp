package il.co.anyway.app;


import android.test.AndroidTestCase;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class utilityTest extends AndroidTestCase {

    @SuppressWarnings("unused")
    public static final String LOG_TAG = utilityTest.class.getSimpleName();

    public void testJsonParsing() {

        // check if one accident added
        String jsonToTest = "{\"markers\": [{\"address\": \"\", \"created\": \"2013-12-05T07:00:00\", \"description\": \"\", \"followers\": [], \"following\": null, \"id\": \"32013054220\", \"latitude\": 32.0829391544824, \"locationAccuracy\": 1, \"longitude\": 34.8021309172237, \"severity\": 3, \"subtype\": 3, \"title\": \"Accident\", \"type\": 1, \"user\": \"\"}]}";

        List<Accident> accidents = new ArrayList<>();
        try {
            accidents = Utility.getAccidentDataFromJson(jsonToTest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(1, accidents.size());


        // check if two accidents added
        jsonToTest = "{\"markers\": [{\"address\": \"\", \"created\": \"2013-12-05T07:00:00\", \"description\": \"\", \"followers\": [], \"following\": null, \"id\": \"32013054220\", \"latitude\": 32.0829391544824, \"locationAccuracy\": 1, \"longitude\": 34.8021309172237, \"severity\": 3, \"subtype\": 3, \"title\": \"Accident\", \"type\": 1, \"user\": \"\"},{\"address\": \"\", \"created\": \"2013-12-05T07:00:00\", \"description\": \"\", \"followers\": [], \"following\": null, \"id\": \"32013054220\", \"latitude\": 32.0829391544824, \"locationAccuracy\": 1, \"longitude\": 34.8021309172237, \"severity\": 3, \"subtype\": 3, \"title\": \"Accident\", \"type\": 1, \"user\": \"\"}]}";

        accidents = new ArrayList<>();
        try {
            accidents = Utility.getAccidentDataFromJson(jsonToTest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals(2, accidents.size());
    }

    public void testTimeStampCreation() {

        String ts = Utility.getTimeStamp("01/01/2013");
        assertEquals("1356991200", ts);

    }
}
