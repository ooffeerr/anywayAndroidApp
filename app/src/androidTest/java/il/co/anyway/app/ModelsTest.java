package il.co.anyway.app;


import android.test.AndroidTestCase;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

import il.co.anyway.app.models.Accident;
import il.co.anyway.app.models.AccidentCluster;
import il.co.anyway.app.models.Discussion;

public class ModelsTest extends AndroidTestCase {
    @SuppressWarnings("unused")
    public static final String LOG_TAG = ModelsTest.class.getSimpleName();

    public void testAccidentCreationID() {

        long set_id = 123456789;
        Accident a = new Accident().setId(set_id);
        assertEquals(set_id, a.getId());

        LatLng loc = new LatLng(0,1);
        a.setLocation(loc);
        assertEquals(loc.latitude, a.getLocation().latitude);
        assertEquals(loc.longitude, a.getLocation().longitude);

        a.setAddress("a");
        assertEquals("a", a.getAddress());

    }

    public void testAccidentClusterCreation() {

        int count = 50;
        LatLng location = new LatLng(32.32, 33.33);
        AccidentCluster accidentCluster = new AccidentCluster(count, location);

        assertEquals(count, accidentCluster.getCount());
        assertEquals(location.latitude, accidentCluster.getLocation().latitude);
        assertEquals(location.longitude, accidentCluster.getLocation().longitude);
    }

    public void testDiscussionCreation() {

        LatLng location = new LatLng(33.23, 42.54);
        int type = 15;
        long id = 4312355533L;
        Date created = new Date();
        String title = "ttttt";
        String identifier = "iiiii";

        Discussion d = new Discussion()
                .setCreated(created)
                .setId(id)
                .setLocation(location)
                .setTitle(title)
                .setIdentifier(identifier)
                .setType(type);

        assertEquals(location.latitude, d.getLocation().latitude);
        assertEquals(location.longitude, d.getLocation().longitude);
        assertEquals(type, d.getType());
        assertEquals(id, d.getId());
        assertEquals(created.getTime(), d.getCreated().getTime());
        assertEquals(title, d.getTitle());
        assertEquals(identifier, d.getIdentifier());

    }
}
