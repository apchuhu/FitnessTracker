package com.example.fitnesstracker;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    //creation of the Firebase object
    private FirebaseAuth mAuth;

    private ProgressBar progressBar;
    TextView textViewRegister;
    TextView forgotPasswordLink;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Creation of the Firebase instance
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.button_login);
        progressBar = findViewById(R.id.progressBar);
        textViewRegister = findViewById(R.id.registerNow);
        forgotPasswordLink = findViewById(R.id.forgotPassword);


        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
          @Override
          public  void onClick(View view) {
              progressBar.setVisibility(View.VISIBLE);
              String email, password;
              email = String.valueOf(editTextEmail.getText());
              password = String.valueOf(editTextPassword.getText());

              if (TextUtils.isEmpty(email)) {
                  Toast.makeText(Login.this, "Enter a Email", Toast.LENGTH_SHORT).show();
                  return;
              }

              if (TextUtils.isEmpty(password)) {
                  Toast.makeText(Login.this, "Enter a Password", Toast.LENGTH_SHORT).show();
                  return;
              }

              mAuth.signInWithEmailAndPassword(email, password)
                      .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                          @Override
                          public void onComplete(@NonNull Task<AuthResult> task) {
                              progressBar.setVisibility(View.GONE);
                              if (task.isSuccessful()) {
                                  //On successful login this code will make the app transfer to the "MainActivity" Screen and end this login screen task.
                                  Toast.makeText(getApplicationContext(), "Login Successful.", Toast.LENGTH_SHORT).show();
                                  Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                  startActivity(intent);
                                  finish();
                                  // Sign in success, update UI with the signed-in user's information
//                                  Log.d(TAG, "signInWithEmail:success");
//                                  FirebaseUser user = mAuth.getCurrentUser();
//                                  updateUI(user);
                              } else {
                                  // If sign in fails, display a message to the user.
//                                  Log.w(TAG, "signInWithEmail:failure", task.getException());
                                  Toast.makeText(Login.this, "Authentication failed.",
                                          Toast.LENGTH_SHORT).show();
//                                  updateUI(null);
                              }
                          }
                      });

            }
        });
        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset  Password");
                passwordResetDialog.setMessage("Enter Your Email For Reset Link");
                passwordResetDialog.setView(resetMail);
                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Login.this, "Reset Link Sent To Your Email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error! Reset Link Not Send " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close the dialog
                    }
                });
                passwordResetDialog.create().show();
            }
        });
    }
}