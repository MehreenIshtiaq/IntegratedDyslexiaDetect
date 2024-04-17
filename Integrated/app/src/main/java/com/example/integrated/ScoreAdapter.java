package com.example.integrated;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ActivityViewHolder> {

    private List<ActivityModel> activities;

    public ScoreAdapter(List<ActivityModel> activities) {
        this.activities = activities;
    }

    @Override
    public ActivityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ActivityViewHolder holder, int position) {
        ActivityModel activity = activities.get(position);
        Log.d("ScoreAdapter", "Binding activity: " + activity.getActivityName()); // Log activity being bound
        holder.activityNameTextView.setText(activity.getActivityName());
        LevelAdapter levelAdapter = new LevelAdapter(activity.getLevels());
        holder.levelsRecyclerView.setLayoutManager(new LinearLayoutManager(holder.levelsRecyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.levelsRecyclerView.setAdapter(levelAdapter);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        public TextView activityNameTextView;
        public RecyclerView levelsRecyclerView;

        public ActivityViewHolder(View itemView) {
            super(itemView);
            activityNameTextView = itemView.findViewById(R.id.activityNameTextView);
            levelsRecyclerView = itemView.findViewById(R.id.levelsRecyclerView); // Assuming your item_activity.xml has a RecyclerView with this ID
        }
    }

    private static class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

        private List<LevelModel> levels;

        LevelAdapter(List<LevelModel> levels) {
            this.levels = levels;
        }

        @Override
        public LevelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level, parent, false);
            return new LevelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LevelViewHolder holder, int position) {
            LevelModel level = levels.get(position);
            holder.levelTextView.setText(String.format(Locale.getDefault(), "Level %d", level.getLevel()));
            holder.maxScoreTextView.setText(String.format(Locale.getDefault(), "Max Score: %d", level.getMaxScore()));
        }

        @Override
        public int getItemCount() {
            return levels.size();
        }

        public class LevelViewHolder extends RecyclerView.ViewHolder {
            public TextView levelTextView;
            public TextView maxScoreTextView;

            public LevelViewHolder(View itemView) {
                super(itemView);
                levelTextView = itemView.findViewById(R.id.levelNumberTextView);
                maxScoreTextView = itemView.findViewById(R.id.maxScoreTextView);
            }
        }

    }
}

