package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.meetings;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.MeetingSlotFinder;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.MeetingSlotsController;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.BaseActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.RecyclerViewInterface;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ToolBarHelper;
import com.google.api.services.calendar.model.TimePeriod;

/**
 * Activity for displaying and interacting with the available meeting slots for a selected expert.
 */
public class MeetingSlotsActivity extends BaseActivity implements RecyclerViewInterface {

    private final MeetingSlotsController meetingSlotsController = new MeetingSlotsController();

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState if the activity is being re-initialized after previously being shut down, this Bundle
     *                           contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_slots);
        meetingSlotsController.initialize(this);
        ToolBarHelper.setUpToolbar(this, "Available Meeting Slots", true, true);
    }

    /**
     * Called when a meeting slot is clicked in the RecyclerView.
     *
     * @param position the position of the clicked meeting slot in the list
     */
    @Override
    public void onItemClick(int position) {
        TimePeriod time = MeetingSlotFinder.getPosition(position);
        meetingSlotsController.onClick(time);
    }
}