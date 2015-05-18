package il.co.anyway.app;


import android.test.AndroidTestCase;

import com.google.android.gms.maps.model.LatLng;

import il.co.anyway.app.models.Accident;

public class accidentTest extends AndroidTestCase {
    @SuppressWarnings("unused")
    public static final String LOG_TAG = accidentTest.class.getSimpleName();

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
}
