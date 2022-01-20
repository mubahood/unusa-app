package app.unusa.app.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;
import app.unusa.app.model.Post;
import app.unusa.app.utils.MyFunctions;

import java.util.ArrayList;

import app.components.R;

public class AdapterPost extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String user_id = "";
    private ArrayList<Post> items = new ArrayList<>();

    private Context ctx;
    int style = 0;
    public OnItemClickListener likeClickListener;
    public OnItemClickListener shareClickListener;
    public OnItemClickListener item_post_download;
    public OnItemClickListener item_post;
    MyFunctions functions;

    public interface OnItemClickListener {
        int onItemClick(View view, Post obj, int position);
    }


    public void setOnShareClickListener(final OnItemClickListener mItemClickListener) {
        this.shareClickListener = mItemClickListener;
    }

    public void setOnLikeClickListener(final OnItemClickListener mItemClickListener) {
        this.likeClickListener = mItemClickListener;
    }

    public void setOnDownloadClickListener(final OnItemClickListener onItemClickListener) {
        this.item_post_download = onItemClickListener;
    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        this.item_post = onItemClickListener;
    }


    public AdapterPost(Context context, ArrayList<Post> items, int style, String user_id) {
        this.items = items;
        this.ctx = context;
        this.user_id = user_id;
        this.style = style;
        functions = new MyFunctions(ctx);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView item_post_image;
        public CircularImageView posted_by_image;
        MaterialRippleLayout parent_layout;
        public TextView clear_view, item_post_title;
        public TextView posted_by, location_view, time_ago_view;

        public OriginalViewHolder(View v) {
            super(v);
            item_post_image = (ImageView) v.findViewById(R.id.item_post_image);
            item_post_title = v.findViewById(R.id.item_post_title);
            posted_by_image = v.findViewById(R.id.posted_by_image);
            clear_view = v.findViewById(R.id.clear_view);
            posted_by = v.findViewById(R.id.posted_by);
            location_view = v.findViewById(R.id.location_view);
            parent_layout = v.findViewById(R.id.parent_layout);
            time_ago_view = v.findViewById(R.id.time_ago_view);
            /* title = (TextView) v.findViewById(R.id.news_post_title);
            product_price = (TextView) v.findViewById(R.id.product_price);
            lyt_parent = v.findViewById(R.id.news_post_parent_view);*/
        }
    }

    int count = 0;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v;
        if (style == 0) {
            if (count == 0) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_4, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_4, parent, false);
            }
        } else if (style == 1) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_4, parent, false);


        } else if (style == 2) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_4, parent, false);


        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_4, parent, false);
        }


        count++;
        vh = new OriginalViewHolder(v);


        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;

            final Post p = items.get(position);
            view.item_post_title.setText(functions.stringCapitalize(p.post_title));
            view.posted_by.setText(functions.stringCapitalize(p.posted_by.firstName + " " + p.posted_by.lastName));
            view.time_ago_view.setText(functions.timeAgo(p.post_time));
            view.location_view.setText(" " + p.posted_by.company_position);
            if (p.liked_by == null) {
                view.clear_view.setText("0");
            } else {
                view.clear_view.setText("Seen by " + p.liked_by.size() + "");
            }

            if (p.post_photo != null) {
                if (p.post_photo.length() > 1) {
                    try {
                        Glide.with(ctx)
                                .load(p.post_photo)
                                .centerCrop()
                                .into(view.item_post_image);
                    } catch (Exception e) {
                        view.item_post_image.setVisibility(View.GONE);
                    }
                } else {
                    view.item_post_image.setVisibility(View.GONE);
                }
            } else {
                view.item_post_image.setVisibility(View.GONE);
            }

            if (p.posted_by.profilePhoto != null) {
                if (p.posted_by.profilePhoto.length() > 1) {
                    try {
                        Glide.with(ctx)
                                .load(p.posted_by.profilePhoto)
                                .centerCrop()
                                .into(view.posted_by_image);
                    } catch (Exception e) {

                    }
                } else {
                    //view.item_post_image.setVisibility(View.GONE);
                }
            } else {
                //view.item_post_image.setVisibility(View.GONE);
            }


            //Tools.displayImageRound(ctx, view.image, p.image);


            view.parent_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item_post != null) {
                        item_post.onItemClick(view, items.get(position), position);
                    }
                }
            });


        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}