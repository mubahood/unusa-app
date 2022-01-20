package app.unusa.app.model;

import com.orm.dsl.Unique;

import java.util.ArrayList;

public class Post {
    @Unique
    public String post_id = "";

    public String post_address = "";
    public double post_longitude = 0.0;
    public double post_latitude = 0.0;

    public ArrayList<String> liked_by = new ArrayList<>();
    public ArrayList<String> downloaded_by = new ArrayList<>();
    public ArrayList<String> shared_by = new ArrayList<>();
    public String
            post_title = "",
            post_photo = "",
            post_description = "",
            post_category = "",
            image_name = "",
            post_time = "";
    public boolean is_cleared = false;
    public int post_likes_num = 0, post_shares_num = 0;

    public UnusaUser posted_by = new UnusaUser();

    public Post() {

    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getPost_title() {
        return post_title;
    }

    public void setPost_title(String post_title) {
        this.post_title = post_title;
    }

    public String getPost_photo() {
        return post_photo;
    }

    public void setPost_photo(String post_photo) {
        this.post_photo = post_photo;
    }

    public String getPost_description() {
        return post_description;
    }

    public void setPost_description(String post_description) {
        this.post_description = post_description;
    }

    public String getPost_category() {
        return post_category;
    }

    public void setPost_category(String post_category) {
        this.post_category = post_category;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }

    public int getPost_likes_num() {
        return post_likes_num;
    }

    public void setPost_likes_num(int post_likes_num) {
        this.post_likes_num = post_likes_num;
    }

    public int getPost_shares_num() {
        return post_shares_num;
    }

    public void setPost_shares_num(int post_shares_num) {
        this.post_shares_num = post_shares_num;
    }

    public UnusaUser getPosted_by() {
        return posted_by;
    }

    public void setPosted_by(UnusaUser posted_by) {
        this.posted_by = posted_by;
    }

    public Post(String post_id, String post_title, String post_photo, String post_description, String post_category, String post_time, int post_likes_num, int post_shares_num, UnusaUser posted_by) {
        this.post_id = post_id;
        this.post_title = post_title;
        this.post_photo = post_photo;
        this.post_description = post_description;
        this.post_category = post_category;
        this.post_time = post_time;
        this.post_likes_num = post_likes_num;
        this.post_shares_num = post_shares_num;
        this.posted_by = posted_by;
    }
}
