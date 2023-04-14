package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.experts;


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
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.RecyclerViewInterface;
import com.google.android.material.chip.Chip;
import java.util.List;

/**
 * An adapter for the RecyclerView displaying the list of experts with the required skills.
 */
public class ExpertsRecyclerViewAdapter extends RecyclerView.Adapter<ExpertsRecyclerViewAdapter.MyViewHolder> {

  // This ensures that we handle onClick() correctly across the board
  private final RecyclerViewInterface recyclerViewInterface;

  Context context;
  List<Expert> experts;

  /**
   * Constructor for ExpertsRecyclerViewAdapter.
   *
   * @param context               the context in which the adapter operates
   * @param experts               the list of experts to display in the RecyclerView
   * @param recyclerViewInterface an interface for handling item click events
   */
  public ExpertsRecyclerViewAdapter(Context context, List<Expert> experts, RecyclerViewInterface recyclerViewInterface) {
    this.context = context;
    this.experts = experts;
    this.recyclerViewInterface = recyclerViewInterface;
  }

  @NonNull
  @Override
  public ExpertsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.expert_row, parent, false);

    return new ExpertsRecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
  }

  /**
   * Binds the data to the ViewHolder for the expert item at the given position.
   *
   * @param holder   the ViewHolder to bind the data to
   * @param position the position of the expert in the list
   */
  @Override
  public void onBindViewHolder(@NonNull ExpertsRecyclerViewAdapter.MyViewHolder holder,
      int position) {
    holder.name.setText(experts.get(position).getName());
    holder.email.setText(experts.get(position).getEmail());
    holder.lead.setVisibility(experts.get(position).isLead() ? View.VISIBLE : View.INVISIBLE);
  }

  @Override
  public int getItemCount() {
    return experts.size();
  }

  /**
   * A ViewHolder for the expert items in the RecyclerView.
   */
  public static class MyViewHolder extends RecyclerView.ViewHolder{

    ImageView personIcon;
    TextView name, email;
    Chip lead;

    /**
     * Constructor for MyViewHolder.
     *
     * @param itemView the expert item view
     * @param recyclerViewInterface an interface for handling item click events
     */
    public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
      super(itemView);

      // Could be replaced with actual account icon in the future.
      personIcon = itemView.findViewById(R.id.personIcon);
      name = itemView.findViewById(R.id.name);
      email = itemView.findViewById(R.id.email);
      lead = itemView.findViewById(R.id.lead);

      itemView.setOnClickListener(v -> {
        if (recyclerViewInterface != null){
          int position = getAdapterPosition();
          // Validate that item has a valid position
          if (position != RecyclerView.NO_POSITION){
            recyclerViewInterface.onItemClick(position);
          }
        }
      });
    }
  }
}
