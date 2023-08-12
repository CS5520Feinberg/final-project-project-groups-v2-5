package edu.northeastern.rhythmlounge.LoginRegisterAndAccount;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.rhythmlounge.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterEmail, editTextRegisterPassword, editTextRegisterUsername;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextRegisterUsername = findViewById(R.id.editTextRegisterUsername);
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonGoToLogin = findViewById(R.id.buttonGoToLogin);

        buttonGoToLogin.setOnClickListener(v -> {
            // Start the Login Activity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        });

        buttonRegister.setOnClickListener(v -> {
            String username = editTextRegisterUsername.getText().toString().trim();
            String email = editTextRegisterEmail.getText().toString().trim();
            String password = editTextRegisterPassword.getText().toString().trim();

            // check if the username, email and password fields are empty
            if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
                Toast.makeText(RegisterActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            // create a new user object to store in the Firestore
                            Map<String, Object> userObj = new HashMap<>();
                            userObj.put("username", username);
                            userObj.put("username_lowercase", username.toLowerCase());
                            userObj.put("email", email);

                            // creating following and followers arrays
                            List<String> followers = new ArrayList<>();
                            List<String> following = new ArrayList<>();
                            userObj.put("followers", followers);
                            userObj.put("following", following);

                            if (user != null) {
                                db.collection("users")
                                        .document(user.getUid())
                                        .set(userObj)
                                        .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error saving user data.",
                                                Toast.LENGTH_SHORT).show());
                            }

                            Toast.makeText(RegisterActivity.this, "Registration successful.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
