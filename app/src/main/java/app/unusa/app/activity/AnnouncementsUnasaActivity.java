package app.unusa.app.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.orm.SugarContext;
import app.unusa.app.activity.article.ArticleSimple;
import app.unusa.app.adapter.AdapterPost;
import app.unusa.app.model.Post;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.Tools;

import java.util.ArrayList;

import app.components.R;

public class AnnouncementsUnasaActivity extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unasa_announcements);
        context = this;
        //Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
        //finish();

    }


    @Override
    protected void onStart() {
        super.onStart();
        bind_views();
        initToolbar();
        initComponent();
        login_in_process();
    }

    ArrayList<UnusaUser> temp_users = new ArrayList<>();
    LinearLayout feed_main_loader, feed_main_container;
    RecyclerView feed_recyclerview;
    boolean isLogged_in = false;
    ArrayList<Post> feeds = new ArrayList<>();
    public UnusaUser logged_in_user = new UnusaUser();

    void bind_views() {
        feed_main_loader = findViewById(R.id.feed_main_loader);
        feed_recyclerview = findViewById(R.id.feed_recyclerview);
        feed_main_container = findViewById(R.id.feed_main_container);
    }

    void login_in_process() {
        SugarContext.init(context);
        try {
            temp_users = (ArrayList<UnusaUser>) UnusaUser.listAll(UnusaUser.class);
        } catch (Exception e) {
            Toast.makeText(this, "Failed. " + e.getMessage(), Toast.LENGTH_LONG).show();
            fetch_data();
            return;
        }

        if (temp_users.isEmpty()) {
            isLogged_in = false;
            fetch_data();
            return;
        }

        logged_in_user = temp_users.get(0);
        fetch_data();
        isLogged_in = true;
    }

    String posts_db = "posts", user_id = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private void fetch_data() {
        show_loader();
        Query q = db.collection(posts_db).orderBy("post_time", Query.Direction.DESCENDING);

        q.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(context, "There is no any announcement..", Toast.LENGTH_LONG).show();
                            hide_loader();
                            return;
                        }
                        feeds = (ArrayList<Post>) queryDocumentSnapshots.toObjects(Post.class);
                        feed_data();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                hide_loader();
            }
        });
    }

    private RecyclerView recyclerView;
    private AdapterPost mAdapter;

    private void feed_data() {
        hide_loader();
        recyclerView = findViewById(R.id.feed_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterPost(this, feeds, 0, logged_in_user.userId);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnLikeClickListener(new AdapterPost.OnItemClickListener() {
            @Override
            public int onItemClick(View view, Post obj, int position) {
                if (!isLogged_in) {
                    Toast.makeText(context, "Please first login.", Toast.LENGTH_SHORT).show();

                    return position;
                }
                if (obj.liked_by == null) {
                    obj.liked_by = new ArrayList<String>();
                }
                if (obj.liked_by.contains(logged_in_user.userId)) {
                    //Toast.makeText(context, "Already Loved", Toast.LENGTH_SHORT).show();
                } else {
                    obj.liked_by.add(logged_in_user.userId);
                    //Toast.makeText(context, "Loved", Toast.LENGTH_SHORT).show();
                    db.collection(posts_db).document(obj.post_id).set(obj);
                }

                return position;
            }
        });

        mAdapter.setOnItemClickListener(new AdapterPost.OnItemClickListener() {
            @Override
            public int onItemClick(View view, Post obj, int position) {
                Intent i = new Intent(getApplicationContext(), PostActivity.class);
                i.putExtra("post_id", obj.post_id);
                context.startActivity(i);
                return position;
            }
        });
        mAdapter.setOnDownloadClickListener(new AdapterPost.OnItemClickListener() {
            @Override
            public int onItemClick(View view, Post obj, int position) {
                if (!isLogged_in) {
                    Toast.makeText(context, "Please first login.", Toast.LENGTH_SHORT).show();

                    return position;
                }
                if (obj.downloaded_by == null) {
                    obj.downloaded_by = new ArrayList<String>();
                }

                if (obj.downloaded_by.contains(logged_in_user.userId)) {


                } else {
                    obj.downloaded_by.add(logged_in_user.userId);
                    db.collection(posts_db).document(obj.post_id).set(obj);
                }


               /* if (is_permitted()) {
                    do_download(obj, false);
                } else {
                    Toast.makeText(TimeLineActivity.this, "Allow this App to download files.", Toast.LENGTH_SHORT).show();
                }*/
                return 0;
            }
        });

    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Announcements");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.colorPrimary);
    }

    void show_loader() {
        feed_main_loader.setVisibility(View.VISIBLE);
        feed_main_container.setVisibility(View.GONE);
    }

    void hide_loader() {
        feed_main_loader.setVisibility(View.GONE);
        feed_main_container.setVisibility(View.VISIBLE);
    }

    private void initComponent() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            openCreateArticle();
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void openArticle(View view) {
        Intent i = new Intent(AnnouncementsUnasaActivity.this, ArticleSimple.class);
        AnnouncementsUnasaActivity.this.startActivity(i);
    }

    public void openCreateArticle() {
        Intent i = new Intent(AnnouncementsUnasaActivity.this, PostCreateNewActivity.class);
        AnnouncementsUnasaActivity.this.startActivity(i);
    }
}
