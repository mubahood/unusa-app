package app.unusa.app.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import app.components.R;
import app.unusa.app.LoginActivity;
import app.unusa.app.model.UnusaTransacton;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;
import app.unusa.app.utils.Tools;
import com.orm.SugarContext;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

import static com.orm.util.ReflectionUtil.getDomainClasses;

public class DashboardMain extends AppCompatActivity {
    private static final String TAG = "DashboardMain";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String usersDb = "users";
    UnusaUser loggedInUser = new UnusaUser();
    MyFunctions functions;
    String transactionDb = "transactions";

    boolean isAdmin = false;
    boolean isReady = false;

    void getData() {
        isAdmin = false;
        loggedInUser = new UnusaUser();
        SugarContext.init(getApplicationContext());
        try {
            List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
            if (users.isEmpty()) {
                Toast.makeText(context, "Not Logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            for (UnusaUser u : users) {
                loggedInUser = u;
                isReady = true;
                break;
            }
            if (loggedInUser.firstName.length() < 1) {
                Toast.makeText(context, "Login before you proceed", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (!loggedInUser.userType.equals("admin")) {
                isAdmin = true;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        db.collection(transactionDb)
                .whereEqualTo("personResponsibleId", loggedInUser.userId)
                .whereEqualTo("isIncome", true)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        transactons = (ArrayList<UnusaTransacton>) queryDocumentSnapshots.toObjects(UnusaTransacton.class);
                        feeData();
                    }
                });

    }


    boolean paidThisMonth = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void feeData() {
        paidThisMonth = false;
        for (UnusaTransacton t : transactons) {
            my_balance_val += t.transactionAmount;
            if (t.dueMonth == (functions.thisMonth+1) &&
                    t.dueYear == functions.thisYear
            ) {
                paidThisMonth = true;
            }
        }

        if (paidThisMonth) {
            paid_this_month.setText("PAID THIS MONTH");
            paid_this_month_container.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green_700));
        } else {
            paid_this_month.setText("NOT PAID THIS MONTH");
            paid_this_month_container.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.red_700));
        }
        my_balance.setText("R. " + functions.toMoneyFormat(my_balance_val));
    }

    ArrayList<UnusaTransacton> transactons = new ArrayList<>();
    int my_balance_val = 0;

    DashboardMain context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_grid_fab);
        context = DashboardMain.this;

        getData();
        SugarContext.init(getApplicationContext());
        initToolbar();
    }

    TextView my_balance;
    TextView et_search;
    TextView paid_this_month;
    ImageButton search_button;
    RelativeLayout paid_this_month_container;

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
        functions = new MyFunctions(DashboardMain.this);
        my_balance = findViewById(R.id.my_balance);
        paid_this_month = findViewById(R.id.paid_this_month);
        search_button = findViewById(R.id.search_button);
        et_search = findViewById(R.id.et_search);
        paid_this_month_container = findViewById(R.id.paid_this_month_container);

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_member();
            }
        });


    }

    String search_text = "";

    private void search_member() {
        //et_search
        search_text = "";
        search_text = et_search.getText().toString();
        if (search_text.length() < 2) {
            Toast.makeText(context, "Search text too short.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(DashboardMain.this, MembersUnasaActivity.class);
        intent.putExtra("reg_num", search_text);
        DashboardMain.this.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    String chosenId = "";

    public void startRegistering(View view) {
        if (true)
            return;
        if (isUploading) {
            Toast.makeText(context, "already uploading.... ", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show();
        isUploading = true;

    }


    String not_submited = "Not Submitted";
    int myCounter = 0;
    Boolean isUploading = false;


    private void reg_member(String first_name, String last_name, String reg_number) {
        /*uploadImage();
        if (true)
            return;*/
        if (true)
            return;
        final UnusaUser newMember = new UnusaUser();
        newMember.firstName = functions.stringCapitalize(first_name);
        newMember.lastName = functions.stringCapitalize(last_name);
        newMember.address = functions.stringCapitalize(not_submited);
        newMember.company = functions.stringCapitalize(not_submited);
        newMember.phoneNumber = reg_number;
        newMember.nationality = functions.stringCapitalize(not_submited);
        newMember.email = not_submited;
        newMember.userId = db.collection(usersDb).document().getId();
        newMember.profilePhoto = "";
        newMember.reg_num = reg_number;
        newMember.company_position = "Member";
        newMember.gender = not_submited;
        newMember.regDate = functions.getTimeStamp();
        newMember.lastSeen = functions.getTimeStamp();
        newMember.username = newMember.phoneNumber;
        newMember.password = "2020";
        newMember.userType = "regular";

        db.collection(usersDb).document(newMember.userId).set(newMember).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                myCounter++;
                Log.d(TAG, "\n " + myCounter + ". ----> BUSINESS: -- SUCESSS ---- REG NO: " + newMember.reg_num + " Name: " + newMember.firstName + " " + newMember.lastName);
                return;
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "\n " + myCounter + ". ----> BUSINESS:  FAILED: " + newMember.reg_num + " Name: " + newMember.firstName + " " + newMember.lastName + " --->> " + e.getMessage());
            }
        });

        if (true)
            return;

        db.collection(usersDb).whereEqualTo("reg_num", newMember.reg_num).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "\n " + myCounter + ". ----> BUSINESS: already exists: REG NO: " + newMember.reg_num + " Name: " + newMember.firstName + " " + newMember.lastName);
                            return;
                        } else {


                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "\n " + myCounter + ". ----> BUSINESS:  Failed: " + newMember.reg_num + " Name: " + newMember.firstName + " " + newMember.lastName + " --->> " + e.getMessage());
            }
        });


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        //Toast.makeText(this, "Hi There", Toast.LENGTH_SHORT).show();
        this.finish();
        System.exit(0);
        //super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            chosenId = (String) item.getTitle().toString();

            switch (chosenId) {
                case "About Company":
                    Intent intent = new Intent(DashboardMain.this, AboutCompany.class);
                    DashboardMain.this.startActivity(intent);
                    break;
                case "About App":
                    Intent i = new Intent(DashboardMain.this, AboutApp.class);
                    DashboardMain.this.startActivity(i);
                    break;
                case "Logout":
                    loggedInUser.delete(loggedInUser);

                    List<Class> users = getDomainClasses(DashboardMain.this);
                    for (Class user : users) {
                        SugarRecord.deleteAll(user);
                    }

                    Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show();
                    Intent x = new Intent(DashboardMain.this, LoginActivity.class);
                    DashboardMain.this.startActivity(x);
                    break;
            }


            //Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    public void openStatistics(View view) {
        Intent intent = new Intent(DashboardMain.this, StatisticsActivity.class);
        DashboardMain.this.startActivity(intent);
    }

    public void openTransactions(View view) {
        Intent intent = new Intent(DashboardMain.this, UnusaTransactionsActivity.class);
        DashboardMain.this.startActivity(intent);
    }

    public void openAnnouncements(View view) {
        Intent intent = new Intent(DashboardMain.this, AnnouncementsUnasaActivity.class);
        DashboardMain.this.startActivity(intent);
    }

    public void openMembers(View view) {
        Intent intent = new Intent(DashboardMain.this, MembersUnasaActivity.class);
        DashboardMain.this.startActivity(intent);
    }

    public void openProfile(View view) {
        if (!isReady) {
            Toast.makeText(context, "Please Wait", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(DashboardMain.this, ProfileUnasaActivity.class);
        intent.putExtra("userId", loggedInUser.userId);
        DashboardMain.this.startActivity(intent);
    }

    public void openMeetings(View view) {
        Intent intent = new Intent(DashboardMain.this, EventesUnasaActivity.class);
        DashboardMain.this.startActivity(intent);
    }


}
