package edu.tjhsst.a2018mmin.onthedot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class ReminderListActivity extends AppCompatActivity {

    private ListView mListView;
    private Button mButton;
    private String name_add;
    private String time_add;
    private String duration_add;
    private String description_add;
    private ArrayList<Reminder> array;
    private AlarmManager alarmMgr;
    String userId;
    private ArrayList<PendingIntent> intentArray = new ArrayList<PendingIntent>();
    public static final String PREFS_NAME = "MyPrefs";
    public SharedPreferences prefs;
    private AlarmManager am;

    public class ReminderAdapter extends ArrayAdapter<Reminder> {
        public ReminderAdapter(Context context, ArrayList<Reminder> reminders) {
            super(context, 0, reminders);
        }
        @Override
        public View getView(int order, View convertView, ViewGroup parent) {
            Reminder reminder = getItem(order);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_format, parent, false);
            }
            TextView mName = (TextView) convertView.findViewById(R.id.name);
            TextView mTime = (TextView) convertView.findViewById(R.id.time);
            TextView mDuration = (TextView) convertView.findViewById(R.id.duration);
            mName.setText(reminder.name);
            mTime.setText(""+ reminder.time);
            mDuration.setText(reminder.duration);

            Button mDeleteButton = (Button)convertView.findViewById(R.id.delete_button);
            mDeleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    final int position = mListView.getPositionForView((View) v.getParent());
                    Reminder rem = array.remove(position);
                    array = sortArrayByTime(array, 0);
                    updateListView();
                    notifyDataSetChanged();
                    deleteData(rem);
                    updateAlarms();
                }
            });
            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        prefs = getSharedPreferences(PREFS_NAME, 0);

        mButton = (Button)findViewById(R.id.input_button);
        array = new ArrayList<Reminder>();
        mListView = (ListView) findViewById(R.id.list_view);

        am = (AlarmManager) ReminderListActivity.this.getSystemService(ReminderListActivity.this.ALARM_SERVICE);
        final ReminderAdapter adapter = new ReminderAdapter(this, array);
        mListView.setAdapter(adapter);
        Map<String, ?> reminderMap = prefs.getAll();
        for (Map.Entry<String, ?> entry : reminderMap.entrySet())
        {
            String parsable = (String)entry.getValue();
            String time = parsable.substring(0, parsable.indexOf(';'));
            String dur = parsable.substring(parsable.indexOf(';')+1, parsable.indexOf('+'));
            String description = parsable.substring(parsable.indexOf('+')+1);
            Reminder temprem = new Reminder(entry.getKey(), time, dur, description);
            array.add(temprem);
        }
        array = sortArrayByTime(array, 0);
        updateListView();
        updateAlarms();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReminderListActivity.this);
                builder.setTitle("Add Reminder");

                Context context = ReminderListActivity.this;
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText nameBox = new EditText(context);
                nameBox.setHint("Activity Name");
                nameBox.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                layout.addView(nameBox);

                final TimePicker timeBox = new TimePicker(context);
                layout.addView(timeBox);

                final EditText durationBox = new EditText(context);
                durationBox.setHint("Duration");
                durationBox.setInputType(InputType.TYPE_CLASS_NUMBER);
                layout.addView(durationBox);
                final TextView minutes = new TextView(context);
                minutes.setText("minutes");

                final EditText descriptionBox = new EditText(context);
                descriptionBox.setHint("Description");
                descriptionBox.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                layout.addView(descriptionBox);

                builder.setView(layout);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        name_add = nameBox.getText().toString();
                        int time_h = timeBox.getHour();
                        int time_m = timeBox.getMinute();
                        String hour_string = "";
                        String minute_string = "";
                        hour_string = "" + time_h;
                        minute_string = time_m + " AM";
                        if(time_h > 12){
                            hour_string = (time_h-12) + "";
                            minute_string = time_m + " PM";
                        }
                        if(time_m < 10){
                            minute_string = "0" + minute_string;
                        }
                        time_add = hour_string + ":" + minute_string;
                        duration_add = durationBox.getText().toString();
                        description_add = descriptionBox.getText().toString();
                        Reminder newReminder = new Reminder(name_add, time_add, duration_add, description_add);

                        array.add(newReminder);
                        array = sortArrayByTime(array, 0);
                        //adapter.notifyDataSetChanged();
                        updateListView();
                        updateAlarms();
                        saveData(newReminder);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }
        });
    }
    private void updateAlarms(){
        //delete prior alarms*/
        for(int i = 0; i< intentArray.size(); i++){
            am.cancel(intentArray.get(i));
        }
        intentArray.clear();
        //repopulate alarms

        for(int i = 0; i < array.size(); i++)
        {
            String tempname = array.get(i).getName();
            int temphour = array.get(i).getHour();
            int tempmin = array.get(i).getMinute();
            //Toast.makeText(ReminderListActivity.this, "" + temphour, Toast.LENGTH_SHORT).show();
            String tempdur = array.get(i).getDuration();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, temphour);
            calendar.set(Calendar.MINUTE, tempmin);
            calendar.set(Calendar.SECOND, 0);
            long start = calendar.getTimeInMillis();
            if (calendar.before(Calendar.getInstance())) {
                start += AlarmManager.INTERVAL_DAY;
            }
            Intent intent1 = new Intent(ReminderListActivity.this, AlarmReceiver.class);
            intent1.putExtra("name", tempname);
            intent1.putExtra("duration", tempdur);
            intent1.putExtra("id", i);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(ReminderListActivity.this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            intentArray.add(pendingIntent);
            //AlarmManager am = (AlarmManager) ReminderListActivity.this.getSystemService(ReminderListActivity.this.ALARM_SERVICE);
            //am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            am.setRepeating(AlarmManager.RTC_WAKEUP, start, AlarmManager.INTERVAL_DAY, pendingIntent);
        }

    }

    private void saveData(Reminder rem){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(rem.getName(), rem.getTime()+";"+rem.getDuration()+"+"+rem.getDescription());
        editor.commit();
    }

    private void deleteData(Reminder rem){
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(rem.getName());
        editor.commit();
    }
    private ArrayList<Reminder> sortArrayByTime(ArrayList<Reminder> arr, int n){
        if(n>=arr.size()){
            return arr;
        }
        int min = (arr.get(n).getHour() * 60) + arr.get(n).getMinute();
        int minIndex = n;
        for(int i=n; i < arr.size(); i++){
            int time = (arr.get(i).getHour() * 60) + arr.get(i).getMinute();
            if(time<min){
                minIndex = i;
            }
        }
        arr = switchRems(arr, n, minIndex);
        return sortArrayByTime(arr, n+1);
    }
    private ArrayList<Reminder> switchRems(ArrayList<Reminder> arr, int a, int b){
        Reminder temp = arr.get(a);
        arr.set(a, arr.get(b));
        arr.set(b, temp);
        return arr;
    }
    private void updateListView(){
        final ReminderAdapter adapter = new ReminderAdapter(this, array);
        mListView.setAdapter(adapter);
    }
}

