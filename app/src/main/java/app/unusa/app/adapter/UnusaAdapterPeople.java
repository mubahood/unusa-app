package app.unusa.app.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import app.unusa.app.model.UnusaUser;

import java.util.ArrayList;
import java.util.List;

import app.components.R;

public class UnusaAdapterPeople extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<UnusaUser> items = new ArrayList<>();
    private Context ctx;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public UnusaAdapterPeople(Context context, List<UnusaUser> items) {
        this.items = items;
        ctx = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView image;
        public TextView name;
        public TextView position_view;
        public TextView number_view;
        public TextView reg_num;
        public LinearLayout lyt_parent;
        public LinearLayout main_container_bg;

        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            main_container_bg = (LinearLayout) v.findViewById(R.id.main_container_bg);
            position_view = v.findViewById(R.id.position_view);
            name = (TextView) v.findViewById(R.id.name);
            number_view = (TextView) v.findViewById(R.id.number_view);
            reg_num = (TextView) v.findViewById(R.id.reg_num);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unusa_people, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder view = (ViewHolder) holder;
            final UnusaUser o = items.get(position);

            if (o.verified != null) {
                if (o.verified.equals("0")) {
                    view.position_view.setText("Not Verified");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        TypedArray a = ctx.obtainStyledAttributes(new int[]{android.R.attr.activatedBackgroundIndicator});
                        int resource = a.getResourceId(0, 0);
                        //first 0 is the index in the array, second is the   default value
                        a.recycle();

                        view.main_container_bg.setBackground(ctx.getResources().getDrawable(R.color.colorPrimary));
                    }
                } else {
                    view.position_view.setText("Verified");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        TypedArray a = ctx.obtainStyledAttributes(new int[]{android.R.attr.activatedBackgroundIndicator});
                        int resource = a.getResourceId(0, 0);
                        //first 0 is the index in the array, second is the   default value
                        a.recycle();

                        view.main_container_bg.setBackground(ctx.getResources().getDrawable(R.color.green_A700));
                    }
                }
            } else {
                view.position_view.setText("Not Verified");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    TypedArray a = ctx.obtainStyledAttributes(new int[]{android.R.attr.activatedBackgroundIndicator});
                    int resource = a.getResourceId(0, 0);
                    //first 0 is the index in the array, second is the   default value
                    a.recycle();

                    view.main_container_bg.setBackground(ctx.getResources().getDrawable(R.color.colorPrimary));
                }

            }


            view.name.setText(o.firstName + " " + o.lastName);
            try {
                view.reg_num.setText("REG No: " + o.reg_num + "");
            } catch (Exception e) {
                view.reg_num.setText("No REG NUM");
            }
            try {
                view.number_view.setText("" + o.phoneNumber + "");
            } catch (Exception e) {
                view.reg_num.setText("No phone number");
            }
            try {

            } catch (Exception e) {
                view.reg_num.setText("N/A");
            }
            //Tools.displayImageRound(ctx, view.image, o.image);
            //Tools.displayImageRound(ctx, view.image, o.image);
            if (o.profilePhoto != null) {
                if (o.profilePhoto.length() > 5) {
                    Glide.with(ctx)
                            .load(o.profilePhoto)
                            .placeholder(R.drawable.user)
                            .centerCrop()
                            .into(view.image);
                } else {
                    Glide.with(ctx)
                            .load(R.drawable.user)
                            .placeholder(R.drawable.user)
                            .centerCrop()
                            .into(view.image);
                }
            } else {
                Glide.with(ctx)
                        .load(R.drawable.user)
                        .placeholder(R.drawable.user)
                        .centerCrop()
                        .into(view.image);
            }

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(view, o, position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public UnusaUser getItem(int position) {
        return items.get(position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, UnusaUser obj, int pos);
    }

    public UnusaAdapterPeople() {
    }
}