package com.example.integrated;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.example.integrated.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProgressAdapter extends RecyclerView.Adapter<ProgressAdapter.ViewHolder> {

    private List<ProgressModel> progressList;

    public ProgressAdapter(List<ProgressModel> progressList) {
        this.progressList = progressList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress_graph, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProgressModel progressModel = progressList.get(position);
        holder.setGraphData(progressModel);
        holder.tvActivityName.setText(progressModel.getActivityName()); // Set activity name
    }

    @Override
    public int getItemCount() {
        return progressList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LineChart lineChart;
        TextView tvActivityName; // TextView for displaying the activity name

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lineChart = itemView.findViewById(R.id.lineChart);
            tvActivityName = itemView.findViewById(R.id.tvActivityName); // Initialize TextView
        }

        void setGraphData(ProgressModel progressModel) {
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            int colorIndex = 0;
            int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE}; // Example colors

            for (Map.Entry<Integer, ArrayList<Entry>> levelEntry : progressModel.getLevelEntriesMap().entrySet()) {
                LineDataSet dataSet = new LineDataSet(levelEntry.getValue(), "Level " + levelEntry.getKey());
                dataSet.setColor(colors[colorIndex % colors.length]);
                dataSet.setValueTextColor(colors[colorIndex % colors.length]);
                dataSets.add(dataSet);
                colorIndex++;
            }

            if (!dataSets.isEmpty()) {
                LineData lineData = new LineData(dataSets);
                lineChart.setData(lineData);
                lineChart.invalidate(); // Refresh the chart
            }
        }
    }
}

