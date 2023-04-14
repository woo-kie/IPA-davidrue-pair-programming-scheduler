package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.meetings;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.RecyclerViewInterface;
import com.google.api.services.calendar.model.TimePeriod;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MeetingsRecyclerViewAdapter extends RecyclerView.Adapter<MeetingsRecyclerViewAdapter.MyViewHolder> {

  private final RecyclerViewInterface recyclerViewInterface;

  Context context;
  List<TimePeriod> meetings;

  SimpleDateFormat hoursFormat = new SimpleDateFormat("HH:mm");
  SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy");


  public MeetingsRecyclerViewAdapter(Context context, List<TimePeriod> meetings, RecyclerViewInterface recyclerViewInterface) {
    this.context = context;
    this.meetings = meetings;
    this.recyclerViewInterface = recyclerViewInterface;
  }

  @NonNull
  @Override
  public MeetingsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.meeting_row, parent, false);

    return new MeetingsRecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
  }

  @Override
  public void onBindViewHolder(@NonNull MeetingsRecyclerViewAdapter.MyViewHolder holder,
      int position) {

    String start = hoursFormat.format(new Date(meetings.get(position).getStart().getValue()));
    String end = hoursFormat.format(new Date(meetings.get(position).getEnd().getValue()));

    holder.time.setText(start + " - " + end);
    holder.date.setText(dateFormat.format(new Date(meetings.get(position).getEnd().getValue())));

  }



  @Override
  public int getItemCount() {
    return meetings.size();
  }

  public static class MyViewHolder extends RecyclerView.ViewHolder{

    ImageView meetingIcon;
    TextView time, date;


    public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
      super(itemView);

      meetingIcon = itemView.findViewById(R.id.meetingIcon);
      time = itemView.findViewById(R.id.time);
      date = itemView.findViewById(R.id.date);

      itemView.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v) {
          if (recyclerViewInterface != null){
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION){
              recyclerViewInterface.onItemClick(position);
            }
          }
        }
      });
    }
  }
}
