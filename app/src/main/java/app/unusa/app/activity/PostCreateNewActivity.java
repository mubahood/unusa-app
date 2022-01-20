package app.unusa.app.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.orm.SugarContext;
import app.unusa.app.model.Post;
import app.unusa.app.model.UnusaUser;
import app.unusa.app.utils.MyFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import app.components.R;

public class PostCreateNewActivity extends AppCompatActivity {

    MyFunctions functions;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        // Obtain the FirebaseAnalytics instance.
        setContentView(R.layout.activity_post_create_new);
        functions = new MyFunctions(getBaseContext());
        SugarContext.init(context);
        try {
            ArrayList<UnusaUser> users = new ArrayList<>();
            users = (ArrayList<UnusaUser>) UnusaUser.listAll(UnusaUser.class);
            if (!users.isEmpty()) {
                logged_in_user = users.get(0);
            } else {
                Toast.makeText(context, "Login before you proceed.", Toast.LENGTH_SHORT).show();
                onBackPressed();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(context, "Login before you proceed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }
        bind_views();
    }


    Boolean location_access_granted = false;
    ImageView imgView_1;
    Post new_post = new Post();
    ProgressDialog progressDialog;
    UnusaUser logged_in_user = new UnusaUser();

    FirebaseStorage storage;
    AppCompatEditText location_title_view;

    void bind_views() {
        imgView_1 = findViewById(R.id.imgView_1);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        spn_property_type = findViewById(R.id.spn_property_type);
        location_title_view = findViewById(R.id.location_title_view);

        progressDialog = new ProgressDialog(PostCreateNewActivity.this);
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);

        imgView_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        findViewById(R.id.upload_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_post.post_title = ((AppCompatEditText) (findViewById(R.id.post_title_view))).getText() + "";
                if (new_post.post_title.length() < 3) {
                    Toast.makeText(PostCreateNewActivity.this, "Title too short.", Toast.LENGTH_LONG).show();
                    return;
                }

                new_post.post_description = ((AppCompatEditText) (findViewById(R.id.post_description_view))).getText() + "";
                if (new_post.post_description.length() < 3) {
                    Toast.makeText(PostCreateNewActivity.this, "Description too short.", Toast.LENGTH_LONG).show();
                    return;
                }

                new_post.post_category = ((Button) (findViewById(R.id.spn_property_type))).getText() + "";
                if (new_post.post_category.length() < 3 || (new_post.post_category.equals("Select Post Category"))) {
                    Toast.makeText(PostCreateNewActivity.this, "Category too short.", Toast.LENGTH_LONG).show();
                    return;
                }

                new_post.post_address = "";
                new_post.posted_by = logged_in_user;
                new_post.post_longitude = 0.0;
                new_post.post_latitude = 0.0;

                uploadImage();


            }
        });

    }

    StorageReference storageReference;
    StorageReference ref;
    private static final String TAG = "PostCreateNewActivity";

    private void uploadImage() {
        new_post.image_name = "images/" + UUID.randomUUID().toString();
        ref = storageReference.child(new_post.image_name);
        progressDialog.show();

        if (filePath != null) {

            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            new_post.post_photo = uri.toString() + "";
                            upload_post_to_db();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostCreateNewActivity.this, "Failed. " + e.getMessage(), Toast.LENGTH_LONG).show();
                            progressDialog.cancel();

                            return;
                        }
                    });
                    return;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostCreateNewActivity.this, "Failed. " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressDialog.cancel();
                    return;
                }
            });


        } else {
            new_post.post_photo = "";
            upload_post_to_db();
            return;
        }
    }


    String posts_db = "posts";
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    private void upload_post_to_db() {
        new_post.post_likes_num = 0;
        new_post.post_shares_num = 0;
        new_post.post_time = functions.getTimeStamp();


        new_post.post_id = db.collection(posts_db).document().getId();
        db.collection(posts_db).document(new_post.post_id).set(new_post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PostCreateNewActivity.this, "Report uploaded successfully.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostCreateNewActivity.this, "Report uploaded successfully.", Toast.LENGTH_LONG).show();
                progressDialog.hide();
                return;
            }
        });
    }

    private int PICK_IMAGE_REQUEST = 71;

    private Uri filePath;
    private Button spn_property_type;

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                PICK_IMAGE_REQUEST = 71;
                imgView_1.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
