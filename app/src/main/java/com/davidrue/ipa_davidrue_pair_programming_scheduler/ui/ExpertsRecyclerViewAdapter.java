package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.Expert;
import com.google.android.material.chip.Chip;
import java.util.List;

public class ExpertsRecyclerViewAdapter extends RecyclerView.Adapter<ExpertsRecyclerViewAdapter.MyViewHolder> {
  Context context;
  List<Expert> experts;

  public ExpertsRecyclerViewAdapter(Context context, List<Expert> experts) {
    this.context = context;
    this.experts = experts;
  }

  @NonNull
  @Override
  public ExpertsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.expert_row, parent, false);

    return new ExpertsRecyclerViewAdapter.MyViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ExpertsRecyclerViewAdapter.MyViewHolder holder,
      int position) {

    System.out.println("DAVID IS THE BEST!!!!!");
    holder.name.setText(experts.get(position).getName());
    holder.email.setText(experts.get(position).getEmail());
    holder.lead.setVisibility(experts.get(position).isLead() ? View.VISIBLE : View.INVISIBLE);
  }

  @Override
  public int getItemCount() {
    return experts.size();
  }

  public static class MyViewHolder extends RecyclerView.ViewHolder{

    ImageView personIcon;
    TextView name, email;
    Chip lead;


    public MyViewHolder(@NonNull View itemView) {
      super(itemView);

      personIcon = itemView.findViewById(R.id.personIcon);
      name = itemView.findViewById(R.id.name);
      email = itemView.findViewById(R.id.email);
      lead = itemView.findViewById(R.id.lead);
    }
  }
}
