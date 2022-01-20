package app.unusa.app.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import app.unusa.app.model.UnusaTransacton;
import app.unusa.app.utils.MyFunctions;

import java.util.ArrayList;

import app.components.R;

public class UnusaTransactionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<UnusaTransacton> items = new ArrayList<>();
    private Context ctx;
    private MyFunctions functions;

    private OnItemClickListener onItemClickListener;
    private OnMoreButtonClickListener onMoreButtonClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnMoreButtonClickListener(final OnMoreButtonClickListener onMoreButtonClickListener) {
        this.onMoreButtonClickListener = onMoreButtonClickListener;
    }


    public UnusaTransactionsAdapter(Context context, ArrayList<UnusaTransacton> items) {
        this.items = items;
        ctx = context;
        functions = new MyFunctions(ctx);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView image;
        public TextView
                name,
                transaction_status,
                transaction_time_ago,
                transaction_amount;
        public LinearLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            transaction_time_ago = (TextView) v.findViewById(R.id.transaction_time_ago);
            transaction_amount = (TextView) v.findViewById(R.id.transaction_amount);
            transaction_status = (TextView) v.findViewById(R.id.transaction_status);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.unasa_item_transaction, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder view = (ViewHolder) holder;
            final UnusaTransacton o = items.get(position);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener == null) return;
                    onItemClickListener.onItemClick(view, o, position);
                }
            });

            view.name.setText(o.personResponsible.firstName + " " + o.personResponsible.lastName);
            view.transaction_time_ago.setText(functions.timeAgo(o.dateRecorded));
            view.transaction_amount.setText("RAND. " + functions.toMoneyFormat(o.transactionAmount));
            if (o.isIncome) {
                view.transaction_status.setText("Income");
                view.transaction_status.setTextColor(Color.parseColor("#00C853"));
            } else {
                view.transaction_status.setText("Expense");
                view.transaction_status.setTextColor(Color.parseColor("#ff0000"));
                ;
            }
            if (o.personResponsible.profilePhoto != null) {
                if (o.personResponsible.profilePhoto.length() > 5) {
                    Glide.with(ctx)
                            .load(o.personResponsible.profilePhoto)
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

            //Tools.displayImageRound(ctx, view.image, o.image);


        }
    }

    private void onMoreButtonClick(final View view, final UnusaTransacton people) {
        PopupMenu popupMenu = new PopupMenu(ctx, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onMoreButtonClickListener.onItemClick(view, people, item);
                return true;
            }
        });
        popupMenu.inflate(R.menu.menu_people_more);
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public UnusaTransacton getItem(int position) {
        return items.get(position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, UnusaTransacton obj, int pos);
    }

    public interface OnMoreButtonClickListener {
        void onItemClick(View view, UnusaTransacton obj, MenuItem item);
    }
}