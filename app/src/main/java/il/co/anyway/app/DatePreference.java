package il.co.anyway.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

public class DatePreference extends DialogPreference {
    private int lastDate = 0;
    private int lastMonth = 0;
    private int lastYear = 0;
    private String dateValue;
    private CharSequence mSummary;
    private DatePicker picker = null;

    public static int getYear(String dateval) {
        String[] pieces = dateval.split("/");
        return (Integer.parseInt(pieces[2]));
    }

    public static int getMonth(String dateval) {
        String[] pieces = dateval.split("/");
        return (Integer.parseInt(pieces[1]));
    }

    public static int getDate(String dateval) {
        String[] pieces = dateval.split("/");
        return (Integer.parseInt(pieces[0]));
    }

    public DatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText("אישור");
        setNegativeButtonText("ביטול");
    }

    //@SuppressLint("NewApi")
    @Override
    protected View onCreateDialogView() {
        picker = new DatePicker(getContext());

        // Make sure we're running on Honeycomb or higher to disable the calendar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            picker.setCalendarViewShown(false);
        }

        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        // (-1) - month is stored as a number between 1-12, the picker save the month in range of 0-11
        picker.updateDate(lastYear, lastMonth-1, lastDate);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastYear = picker.getYear();
            // (+1) - month is stored as a number between 1-12, the picker save the month in range of 0-11
            lastMonth = picker.getMonth()+1;
            lastDate = picker.getDayOfMonth();

            String dateValue = String.valueOf(lastDate) + "/"
                    + String.valueOf(lastMonth) + "/"
                    + String.valueOf(lastYear);

            if (callChangeListener(dateValue)) {
                persistString(dateValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        dateValue = null;

        if (restoreValue) {
            if (defaultValue == null) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                String formatted = format1.format(cal.getTime());
                dateValue = getPersistedString(formatted);
            } else {
                dateValue = getPersistedString(defaultValue.toString());
            }
        } else {
            dateValue = defaultValue.toString();
        }
        lastYear = getYear(dateValue);
        lastMonth = getMonth(dateValue);
        lastDate = getDate(dateValue);
    }

    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        dateValue = text;

        persistString(text);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public String getText() {
        return dateValue;
    }

    public CharSequence getSummary() {
        return mSummary;
    }

    public void setSummary(CharSequence summary) {
        if (summary == null && mSummary != null || summary != null
                && !summary.equals(mSummary)) {
            mSummary = summary;
            notifyChanged();
        }
    }
}
