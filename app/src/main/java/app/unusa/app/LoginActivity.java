package app.unusa.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.List;

import app.components.R;
import app.unusa.app.activity.DashboardMain;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;

public class LoginActivity extends AppCompatActivity {

    private View parent_view;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usersDb = "users";
    String username_val, password_val;
    MyFunctions myFunctions;

    void get_all_user() {
        db.collection(usersDb).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    people = (ArrayList<UnusaUser>) queryDocumentSnapshots.toObjects(UnusaUser.class);
                    for (UnusaUser u : people) {
                       /* db.collection(usersDb).document(u.userId)
                                .update("regDate", "1577840461000");*/

                        Log.d(TAG, "on__success: ===> " + u.userId + " " + u.regDate);

                    }
                    Toast.makeText(LoginActivity.this, "Done", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void create_user() {
        db.collection(usersDb).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG, "onSuccess: =======> response");

                for (UnusaUser u : queryDocumentSnapshots.toObjects(UnusaUser.class)) {
                       /* db.collection(usersDb).document(u.userId)
                                .update("regDate", "1577840461000");*/
                    Log.d(TAG, "on__success: ===> " + u.userId + " " + u.firstName + " " + u.lastName + " ==> " + u.username + " ==> " + u.password);

                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "FAILED: =======> response");
                    }
                });

        if (1 == 1) {
            return;
        }

        Toast.makeText(this, "Creating new User!", Toast.LENGTH_SHORT).show();
        UnusaUser unusaUser = new UnusaUser();

        unusaUser.address = "Cape Town";
        unusaUser.firstName = "Hamad";
        unusaUser.lastName = "Baluku";
        unusaUser.email = "1234";
        unusaUser.userId = "1234";
        unusaUser.username = "1234";
        unusaUser.reg_num = "1234";
        unusaUser.company = "No Campany";
        unusaUser.password = "1245";
        unusaUser.gender = "Male";
        unusaUser.userType = "admin";
        unusaUser.verified = "0";
        unusaUser.regDate = myFunctions.getTimeStamp();
        unusaUser.lastSeen = myFunctions.getTimeStamp();
        db.collection(usersDb).document(unusaUser.userId).set(unusaUser).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(LoginActivity.this, "sUCCESS", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_card_overlap);
        parent_view = findViewById(android.R.id.content);
        myFunctions = new MyFunctions(this);
        SugarContext.init(LoginActivity.this);
        bindViews();
        //create_user();
        //get_all_user();
        try {

            List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
            if (users.isEmpty()) {
                return;
            }
            for (UnusaUser u : users) {
                loggedInUser = u;
                startMainDashboard();
                break;
            }

            UnusaUser user = UnusaUser.findById(UnusaUser.class, 1);


            if (user != null) {
                if (user.username != null) {
                    if (user.username.length() > 2) {
                        startMainDashboard();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
        }

        ((View) findViewById(R.id.sign_up)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(parent_view, "Sign Up", Snackbar.LENGTH_SHORT).show();
            }
        });

        Tools.setSystemBarColor(this);

    }

    Button login_button;
    TextInputEditText username, password;

    void bindViews() {
        login_button = findViewById(R.id.login_button);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create_user();
                collect_form_data();
                //create_user();

            }
        });
    }

    private void collect_form_data() {
        username_val = username.getText().toString();
        password_val = password.getText().toString();

        if (username_val.length() < 2) {
            Toast.makeText(this, "Enter correct username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password_val.length() < 2) {
            Toast.makeText(this, "Enter correct password", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Please Wait...", Toast.LENGTH_SHORT).show();
        Query query; //db.collection(usersDb).whereEqualTo("state", "CA");
        query = db.collection(usersDb).whereEqualTo("reg_num", username_val);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    people = (ArrayList<UnusaUser>) queryDocumentSnapshots.toObjects(UnusaUser.class);
                    for (UnusaUser u : people) {
                        loggedInUser = u;
                        break;
                    }

                    if (!loggedInUser.password.equals(password_val)) {
                        Toast.makeText(LoginActivity.this, "Wrong password.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        loggedInUser.save();
                        Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        startMainDashboard();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "FAILED: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onSuccess: MUBAHOOD FAILED " + e.getMessage());
                        Log.d(TAG, "onSuccess: MUBAHOOD FAILED " + e.getCause());
                    }


                } else {
                    Toast.makeText(LoginActivity.this, "Wrong username or password", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static final String TAG = "LoginActivity";

    public void startMainDashboard() {
        Intent i = new Intent(LoginActivity.this, DashboardMain.class);
        LoginActivity.this.startActivity(i);
        finish();
        return;
    }

    UnusaUser loggedInUser = new UnusaUser();
    ArrayList<UnusaUser> people = new ArrayList<>();

}
