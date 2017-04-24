package com.example.sophi.fosteringhabits;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;

public class DetailsActivity extends AppCompatActivity {
    private static final String TAG = "DetailsActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference habitIdRef;
    private DatabaseReference checkedhabits;

    private CheckBox chHabit;
    private CalendarPickerView calendar;
    private TextView count;
    private TextView goal;

    private  ArrayList<String> dateStringList;
    private ArrayList<Date> dateList;
    private SharedPreferences preferences;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Log.d(TAG, "onCreate");

        //get the data (extras) in the intent that was used
        //to start this activity
        Intent intent = getIntent();
        String habitTitle = intent.getStringExtra("habitTitle");
        String habitId = intent.getStringExtra("habitId");
        String habitGoal = intent.getStringExtra("habitGoal");


        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        // Current User in database
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        // User' all habits
        mDatabase = mDatabaseUsers.child("Habits");

        habitIdRef = mDatabase.child(habitId);
        checkedhabits = habitIdRef.child("CheckedDates");

        //Use toolbar to instead action bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //toolbar title is habit title
        getSupportActionBar().setTitle(habitTitle);

        goal = (TextView) findViewById(R.id.goal);
        goal.setText("Goal: finish " + habitGoal +" times" );
        count = (TextView) findViewById(R.id.count);
        chHabit = (CheckBox)findViewById(R.id.chHabit);

        //apply shared preference to checkbox
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        if(preferences.contains("checked") && preferences.getBoolean("checked",false) == true) {
            chHabit.setChecked(true);
        }else {
            chHabit.setChecked(false);
        }
        chHabit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(chHabit.isChecked()) {
                    //once user click checked, store current date info
                    final DatabaseReference checkeddates = checkedhabits.push();
                    habitIdRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                            dateFormatter.setLenient(false);
                            Date today = new Date();
                            String date = dateFormatter.format(today);
                            checkeddates.child("date").setValue(date);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    editor.putBoolean("checked", true);
                    editor.apply();
                }else{
                    editor.putBoolean("checked", false);
                    editor.apply();
                }
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        dateStringList = new ArrayList<String>();
        dateList = new ArrayList<Date>();

        checkedhabits.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot hab : dataSnapshot.getChildren()) {
                    //the condition is to eliminate exceptions when data changed in another activity
                    if (hab.child("date").getValue() != null) {
                        //get the values from database and set them to arraylist
                        dateStringList.add(hab.child("date").getValue().toString());

                        //count completed times
                        int counts = dateStringList.size();
                        String countsString = Integer.toString(counts);
                        count.setText("Actual: finished " + countsString + " times");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd");

                        // convert string array list to date array list
                        for (String dateString : dateStringList) {
                            try {
                                dateList.add(simpleDateFormat.parse(dateString));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        //create min date
                        Date today = new Date();
                        //create max date
                        Calendar lastMonth = Calendar.getInstance();
                        lastMonth.add(Calendar.MONTH, -1);

                        Calendar nextDay = Calendar.getInstance();
                        nextDay.add(Calendar.DATE, 1);

                        //create calendar picker view by using times square
                        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);

                        calendar.init(lastMonth.getTime(),nextDay.getTime())
                                .inMode(RANGE)
                                .withHighlightedDates(dateList);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd");

        for (String dateString : dateStringList) {
            try {
                dateList.add(simpleDateFormat.parse(dateString));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //create min date
        Date today = new Date();
        //create max date
        Calendar lastMonth = Calendar.getInstance();
        lastMonth.add(Calendar.MONTH, -1);

        Calendar nextDay = Calendar.getInstance();
        nextDay.add(Calendar.DATE, 1);

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);

        calendar.init(lastMonth.getTime(),nextDay.getTime())
                .inMode(RANGE)
                .withHighlightedDates(dateList);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                //build alert dialog
                AlertDialog.Builder builder1 = new AlertDialog.Builder(DetailsActivity.this);
                builder1.setMessage("Are you sure you want to delete this entry?");
                builder1.setCancelable(true);
                // delete entry once user choose Yes
                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                habitIdRef.removeValue();
                                Toast.makeText(DetailsActivity.this, "Deleted Successfully.",Toast.LENGTH_LONG).show();
                                dialog.cancel();
                            }
                        });
               //cancel dialog once user choose No
                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                //create and show alert dialog
                AlertDialog alert11 = builder1.create();
                alert11.show();

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


}
