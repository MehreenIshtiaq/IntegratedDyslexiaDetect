package com.example.integrated;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> implements Filterable {

    private final List<ActivityItem> activityList;
    private List<ActivityItem> activityListFull; // For filtering
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ActivityItem item);
    }

    public ActivityAdapter(List<ActivityItem> activityList, OnItemClickListener listener) {
        this.activityList = activityList;
        this.activityListFull = new ArrayList<>(activityList);
        this.listener = listener;
    }

    @Override
    public Filter getFilter() {
        return activityFilter;
    }

    private Filter activityFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ActivityItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(activityListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ActivityItem item : activityListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            activityList.clear();
            activityList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem item = activityList.get(position);
        holder.nameTextView.setText(item.getName());
        //holder.descriptionTextView.setText(item.getDescription());
        holder.imageView.setImageResource(item.getImageResourceId());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView;
        ImageView imageView;

        ActivityViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.textViewActivityName);
            //descriptionTextView = itemView.findViewById(R.id.textViewActivityDescription);
            imageView = itemView.findViewById(R.id.imageViewActivityIcon);
        }
    }
}
