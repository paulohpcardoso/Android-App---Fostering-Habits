package com.example.sophi.fosteringhabits;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{
    private EditText nameEdit;
    private EditText emailEdit;
    private EditText passwordEdit;
    private EditText repaetpasswordEdit;

    private Button signupButton;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;

    public void login(View view) {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(this);
        nameEdit = (EditText)findViewById(R.id.name);
        emailEdit = (EditText)findViewById(R.id.email);
        passwordEdit = (EditText)findViewById(R.id.password);
        repaetpasswordEdit = (EditText)findViewById(R.id.repeatpassword);
        signupButton = (Button) findViewById(R.id.signup);

        signupButton.setOnClickListener(this);
    }


    private void registerUser(){
        final String name = nameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String repeatpassword = repaetpasswordEdit.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            //email is empty
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(email)){
            //email is empty
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password)){
            //password is empty
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(repeatpassword)){
            //password is empty
            Toast.makeText(this, "Please enter password again", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!repeatpassword.equals(password)){
            //password not matching
            Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show();
            return;
        }

        //if validation are ok
        //we will first show a progressbar
        progressDialog.setMessage("Registering User...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            //user is successfully registered and logged in
                            //we will start the profile activity here
                            Toast.makeText(MainActivity.this, "Registered Successfully.", Toast.LENGTH_SHORT).show();
                            String userid = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = mDatabase.child(userid);
                            current_user_db.child("name").setValue(name);

                            progressDialog.dismiss();

                            Intent intent = new Intent(MainActivity.this, HabitsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        }else{
                            Toast.makeText(MainActivity.this, "Can not register,please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onClick(View v) {
        if(v == signupButton){
            registerUser();
        }

    }
}
