package com.example.sampleapplication.UI.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.sampleapplication.Utils.PreferenceUtils;
import com.example.sampleapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    AppCompatEditText emailLogin, passwordLogin;
    AppCompatButton loginBtn;
    AppCompatTextView signuptxtview;
    private FirebaseAuth auth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        PreferenceUtils utils = new PreferenceUtils();

        if (utils.getEmail(this) != null ){
            Intent intent = new Intent(LoginActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        }else{

        }
        
        if (user != null)
        {

        }

        emailLogin = findViewById(R.id.email_login);
        passwordLogin = findViewById(R.id.password_login);
        loginBtn = findViewById(R.id.login_btn);
        signuptxtview = findViewById(R.id.signup_txtview);

        signuptxtview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(in);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = emailLogin.getText().toString();
                final String password = passwordLogin.getText().toString();

                if (TextUtils.isEmpty(emailLogin.getText().toString())){

                    emailLogin.setError("Required");
                    passwordLogin.setError(null);
                }
                else if (TextUtils.isEmpty(passwordLogin.getText().toString())){

                    emailLogin.setError(null);
                    passwordLogin.setError("Required");
                }
                else {

                    emailLogin.setError(null);
                    passwordLogin.setError(null);

                    dialog.setMessage("Signing in...");
                    dialog.show();

                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {
                                dialog.dismiss();
                                PreferenceUtils.saveEmail(email, LoginActivity.this);
                                PreferenceUtils.savePassword(password, LoginActivity.this );
                                Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
                                Intent in = new Intent(LoginActivity.this, UserActivity.class);
                                startActivity(in);
                                finish();

                            /*String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            dialog.dismiss();
                            Toast.makeText(LoginActivity.this, currentuser, Toast.LENGTH_SHORT).show();*/

                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(LoginActivity.this, "user not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                /*auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    dialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Successful", Toast.LENGTH_SHORT).show();

                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(LoginActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });*/

            }
        });
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
