package com.example.sophi.fosteringhabits;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HabitsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseCurentUser;
    private DatabaseReference mDatabaseUsers;
    private Query mQueryCurrentUser;

    private HabitAdapter habitAdapter;
    private ArrayList<Habit> habit;
    private ArrayList<String> habitId;
    private ListView listView;

    private FirebaseAuth.AuthStateListener mAuthListerner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);

        mAuth = FirebaseAuth.getInstance();
        mAuthListerner = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() ==  null){
                    Intent loginItent = new Intent(HabitsActivity.this,LoginActivity.class);
                    loginItent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginItent);
                }
            }
        };

        mCurrentUser = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Habits");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        String currentUserId = mAuth.getCurrentUser().getUid();
        mDatabaseCurentUser = FirebaseDatabase.getInstance().getReference().child("Habits");
        mQueryCurrentUser = mDatabaseCurentUser.orderByChild("uid").equalTo(currentUserId);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        listView = (ListView)findViewById(R.id.listView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListerner);

        //adding listener to root reference - when data in database changes, the data in views will change accordingly
        //data is accessible through dataSnapshot

        mQueryCurrentUser.addValueEventListener(new ValueEventListener() {
                final String userid = mCurrentUser.getUid();
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    habit = new ArrayList<Habit>();
                    habitId = new ArrayList<String>();

                    //iterate the children of the root node
                    for (DataSnapshot hab : dataSnapshot.getChildren()) {
                        //the condition is to eliminate exceptions when data changed in another activity
                        if (hab.child("title").getValue() != null && hab.child("goal").getValue() != null) {
                            //get the values from database and set them to arraylist
                                habit.add(new Habit(hab.child("title").getValue().toString(), hab.child("goal").getValue().toString()));
                                habitId.add(hab.getKey());

                        }

                    }

                    habitAdapter = new HabitAdapter(HabitsActivity.this, R.layout.list_item, habit);

                    listView.setAdapter(habitAdapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }


    //struct for habits
    public class Habit {
        private String habitTitle;
        private String habitGoal;

        public Habit(String habitTitle, String habitGoal) {
            this.habitTitle = habitTitle;
            this.habitGoal = habitGoal;
        }

        public String getHabitTitle() {
            return habitTitle;
        }

        public String getHabitGoal() {
            return habitGoal;
        }

    }

    private class HabitAdapter extends ArrayAdapter<Habit> {

        private ArrayList<Habit> items;

        public HabitAdapter(Context context, int textViewResourceId, ArrayList<Habit> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        //This method is called once for every item in the ArrayList as the list is loaded.
        //It returns a View -- a list item in the ListView -- for each item in the ArrayList
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }
            Habit o = items.get(position);

            if (o != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null) {
                    tt.setText(o.getHabitTitle());
                }
                if (bt != null) {
                    bt.setText(o.getHabitGoal());
                }
            }
            return v;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tool_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                AlertDialog.Builder builder = new AlertDialog.Builder(HabitsActivity.this);

                LayoutInflater inflater = LayoutInflater.from(this);
                final View inf = inflater.inflate(R.layout.custom_alert,null);

                final EditText titleEdit = (EditText) inf.findViewById(R.id.title);
                final EditText goalEdit = (EditText) inf.findViewById(R.id.goal);
                final Button saveButton = (Button) inf.findViewById(R.id.save);

                builder.setView(inf);
                final AlertDialog alertDialog = builder.create();

                saveButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        final String title = titleEdit.getText().toString().trim();
                        final String goal = goalEdit.getText().toString().trim();


                        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(goal)){

                            final DatabaseReference newHabit = mDatabase.push();

                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    newHabit.child("title").setValue(title);
                                    newHabit.child("goal").setValue(goal);
                                    newHabit.child("uid").setValue(mCurrentUser.getUid());
                                    newHabit.child("username").setValue(dataSnapshot.child("name").getValue());

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            alertDialog.dismiss();
                        }
                    }
                });

                alertDialog.show();
                break;

            case R.id.settings:
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_logout:
                logout();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void logout() {
        mAuth.signOut();
    }

}

