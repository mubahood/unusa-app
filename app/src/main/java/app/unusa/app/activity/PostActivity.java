package app.unusa.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.orm.SugarContext;
import app.unusa.app.model.Post;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import app.components.R;

public class PostActivity extends AppCompatActivity {
    String posts_db = "posts", user_id = "";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Post post = new Post();
    LinearLayout post_main_loader;
    MyFunctions functions;
    private UnusaUser logged_in_user = new UnusaUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        functions = new MyFunctions(this);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                post.post_id = "";
            } else {
                post.post_id = extras.getString("post_id");
            }
        } else {
            post.post_id = (String) savedInstanceState.getSerializable("post_id");
        }
        if (post.post_id.length() < 2) {
            Toast.makeText(this, "Article not selected.", Toast.LENGTH_SHORT).show();
            finish();
        }


        login_in_process();
        bind_views();

        fetch_data();


    }

    String title = "";


    Post my_post = new Post();

    private void fetch_data() {
        db.collection(posts_db).document(post.post_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(PostActivity.this, "Post not found. ", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                post = documentSnapshot.toObject(Post.class);
                my_post = documentSnapshot.toObject(Post.class);
                like_now();
                feed_data();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, "Poor network. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void feed_data() {
        if (isLogged_in) {
            if (logged_in_user.userType.equals("admin")) {
                admin_action.setVisibility(View.VISIBLE);
            } else {
                admin_action.setVisibility(View.GONE);
            }
        }

        Glide.with(getApplicationContext())
                .load(post.post_photo)
                .centerCrop()
                .placeholder(R.drawable.loading_spinner)
                .into(post_image);
        post_title.setText(post.post_title);
        post_category.setText(post.post_category);
        post_description.setText(post.post_description);
        post_time.setText(functions.timeAgo(post.post_time));
        title = post.posted_by.firstName;
        hide_loader();
    }

    LinearLayout post_main_container;

    TextView post_title, post_category, post_time, post_description;
    ImageView post_image;
    ImageButton button_share, button_download, button_like;

    boolean isLogged_in = false;
    ArrayList<UnusaUser> temp_users = new ArrayList<>();

    void login_in_process() {
        SugarContext.init(PostActivity.this);
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
        if (!logged_in_user.userType.equals("admin")) {
            Toast.makeText(this, "Only admin can create announcement", Toast.LENGTH_LONG).show();
            finish();
        }
        isLogged_in = true;
    }

    TextView post_see_more, admin_delete_post, admin_edit_post;
    LinearLayout admin_action;


    private void bind_views() {
        button_share = findViewById(R.id.button_share);
        admin_edit_post = findViewById(R.id.admin_edit_post);
        admin_action = findViewById(R.id.admin_action);
        post_see_more = findViewById(R.id.post_see_more);
        button_download = findViewById(R.id.button_download);
        admin_delete_post = findViewById(R.id.admin_delete_post);
        post_main_loader = findViewById(R.id.post_main_loader);
        post_image = findViewById(R.id.post_image);
        post_title = findViewById(R.id.post_title);
        post_description = findViewById(R.id.post_description);
        button_like = findViewById(R.id.button_like);
        post_time = findViewById(R.id.post_time);
        post_category = findViewById(R.id.post_category);
        post_main_container = findViewById(R.id.post_main_container);
        show_loader();
        admin_action.setVisibility(View.GONE);

        admin_edit_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setTitle("Confirm!");
                builder.setMessage("Are you sure you want to clear this post");
                builder.setPositiveButton("CLEAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(PostActivity.this, "Clearing...", Toast.LENGTH_SHORT).show();
                        db.collection(posts_db).document(post.post_id).update("is_cleared", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                /*storageReference.child(post.image_name).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(PostActivity.this, "File deleted!", Toast.LENGTH_SHORT).show();
                                    }
                                });*/
                                finish();
                                return;
                            }
                        });
                    }
                });
                builder.setNegativeButton("CANCEL", null);
                builder.show();

                return;
            }
        });

        admin_delete_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
                builder.setTitle("Warning!");
                builder.setMessage("Are you sure you want to delete this post");
                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(PostActivity.this, "Deleting...", Toast.LENGTH_SHORT).show();
                        db.collection(posts_db).document(post.post_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                /*storageReference.child(post.image_name).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(PostActivity.this, "File deleted!", Toast.LENGTH_SHORT).show();
                                    }
                                });*/

                                Toast.makeText(PostActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        });
                    }
                });
                builder.setNegativeButton("CANCEL", null);
                builder.show();

                return;
            }
        });

        post_see_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
        button_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_now(post, true);
            }
        });
        button_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_now(post, false);
            }
        });

/*        button_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                like_now();
            }
        });*/
    }

    private void like_now() {
        if (!isLogged_in) {
            Toast.makeText(PostActivity.this, "Please first login.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (my_post.liked_by == null) {
            my_post.liked_by = new ArrayList<String>();
        }
        if (my_post.liked_by.contains(logged_in_user.userId)) {

        } else {
            my_post.liked_by.add(logged_in_user.userId);
            db.collection(posts_db).document(my_post.post_id).set(my_post);
        }
    }


    File folder;
    String FolderName = "";

    private void share_now(final Post p, final boolean is_sharable) {
        FolderName = "Falaah Alert App";
        folder = new File(Environment.getExternalStorageDirectory() + File.separator + FolderName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        File file = new File(folder.getPath() + "/" + p.post_id + ".jpg");
        if (file.exists() && is_sharable) {

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (!p.shared_by.contains(logged_in_user.userId)) {
                p.shared_by.add(logged_in_user.userId);
                db.collection(posts_db).document(p.post_id).set(p);
            }

            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(folder.getPath() + "/" + p.post_id + ".jpg"));
            startActivity(Intent.createChooser(share, "Share Image"));
            return;
        } else {
        }

        if (file.exists() && !is_sharable) {
            Toast.makeText(this, "Already downloaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!success) {
            Toast.makeText(this, "Failed to create folder.", Toast.LENGTH_SHORT).show();
            return;
        }


    }


    void show_loader() {
        post_main_loader.setVisibility(View.VISIBLE);
        post_main_container.setVisibility(View.GONE);
    }

    void hide_loader() {
        post_main_loader.setVisibility(View.GONE);
        post_main_container.setVisibility(View.VISIBLE);
    }
}
