package app.unusa.app.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import app.components.R;
import app.unusa.app.adapter.UnusaAdapterContributionTypes;
import app.unusa.app.model.UnusaContributionType;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.Tools;
import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.List;

public class EventesUnasaActivity extends AppCompatActivity {

    private View parent_view;
    private RecyclerView recyclerView;
    private UnusaAdapterContributionTypes mAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unasa_events);
        get_user_data();
        getExtras();
        bindViews();
        initToolbar();
        fetchData();
    }

    void getExtras() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("userId")) {
                this.userId = getIntent().getStringExtra("userId");
            }
            if (extras.containsKey("task")) {
                this.task = getIntent().getStringExtra("task");
                if (task.equals("addOther")) {
                    isAddOtherTask = true;
                }
            }
        }
    }

    RelativeLayout events_progress_bar;

    private void bindViews() {
        events_progress_bar = findViewById(R.id.events_progress_bar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        events_progress_bar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    String contributionDb = "contributionTypes";
    ArrayList<UnusaContributionType> contributionTypes = new ArrayList<>();

    private void fetchData() {
        db.collection(contributionDb).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(EventesUnasaActivity.this, "No Data to display", Toast.LENGTH_SHORT).show();
                        }
                        contributionTypes = (ArrayList<UnusaContributionType>) queryDocumentSnapshots.toObjects(UnusaContributionType.class);
                        initComponent();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EventesUnasaActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean isAdmin = false;

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Contributions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    String userId = "";
    boolean isAddOtherTask = false;
    UnusaUser loggedInUser;

    private void get_user_data() {
        loggedInUser = new UnusaUser();
        SugarContext.init(getApplicationContext());
        try {

            List<UnusaUser> users = UnusaUser.listAll(UnusaUser.class);
            if (users.isEmpty()) {
                return;
            }
            isAdmin = false;
            for (UnusaUser u : users) {
                loggedInUser = u;
                if (u.userType.equals("admin")) {
                    isAdmin = true;
                }
                break;
            }

            if (loggedInUser.firstName.length() < 1) {
                Toast.makeText(EventesUnasaActivity.this, "Login before you proceed", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }


            return;
        } catch (Exception e) {
            Toast.makeText(this, "Something Went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

    }


    private void initComponent() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        events_progress_bar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);


        //set data and list adapter
        mAdapter = new UnusaAdapterContributionTypes(EventesUnasaActivity.this, contributionTypes);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new UnusaAdapterContributionTypes.OnItemClickListener() {
            @Override
            public void onItemClick(View view, UnusaContributionType obj, int position) {
                if (isAddOtherTask) {
                    Intent intent = new Intent(EventesUnasaActivity.this, AddOtherContributionUnasaActivity.class);
                    intent.putExtra("contributionId", obj.contributionId);
                    intent.putExtra("userId", userId);
                    EventesUnasaActivity.this.startActivity(intent);
                    finish();
                    return;
                } else {
                    Intent intent = new Intent(EventesUnasaActivity.this, UnusaSingleContributionActivity.class);
                    intent.putExtra("contributionId", obj.contributionId);
                    EventesUnasaActivity.this.startActivity(intent);
                    finish();
                    return;
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            if (item.getTitle().toString().equals("Add")) {
                if (!isAdmin) {
                    Toast.makeText(this, "Unauthorized access.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(EventesUnasaActivity.this, AddContributionTypeUnasaActivity.class);
                    EventesUnasaActivity.this.startActivity(i);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
