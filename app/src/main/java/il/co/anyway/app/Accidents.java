package il.co.anyway.app;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Accidents {

    private List<Accident> accidents;

    public Accidents() {
        accidents = new ArrayList<Accident>();
        addSampleAccidents();

    }

    public void addAccidentToList(Accident acc) {
        accidents.add(acc);
    }

    private void addSampleAccidents() {

        addAccidentToList(new Accident("תאונה1", "אין הסברים1", new LatLng(31.771918, 35.213355)));
        addAccidentToList(new Accident("תאונה2", "אין הסברים2", new LatLng(31.771918, 35.213355)));

        addAccidentToList(new Accident("תאונה3", "אין הסברים3", new LatLng(31.771358, 35.213527)));
        addAccidentToList(new Accident("תאונה4", "אין הסברים4", new LatLng(31.772178, 35.214900)));
        addAccidentToList(new Accident("תאונה5", "אין הסברים5", new LatLng(31.772315, 35.213023)));
        addAccidentToList(new Accident("תאונה6", "אין הסברים6", new LatLng(31.771266, 35.212298)));
        addAccidentToList(new Accident("תאונה7", "אין הסברים7", new LatLng(31.771079, 35.214321)));

    }

    public List<Accident> getAccidents() {
        return accidents;
    }
}
