package com.davidrue.ipa_davidrue_pair_programming_scheduler.ui.meetings;

import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.R;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.MeetingSlotFinder;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.data.MeetingSlotFinder.Duration;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.BaseActivity;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.RecyclerViewInterface;
import com.davidrue.ipa_davidrue_pair_programming_scheduler.domain.helpers.ToolBarHelper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.TimePeriod;
import java.util.Date;

public class MeetingSlotsActivity extends AppCompatActivity implements RecyclerViewInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_slots);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        String expert = getIntent().getStringExtra("EXPERT");
        Date now = new Date();
        Date nextWeek = new Date(now.getTime() + 7 * (1000 * 60 * 60 * 24));
        RecyclerView recyclerView = findViewById(R.id.meetingsRecyclerView);
        TimePeriod window = new TimePeriod().setStart(new DateTime(now)).setEnd(new DateTime(nextWeek));
        MeetingSlotFinder.findAvailableMeetingSlots(this, expert, window, Duration.FIFTEEN, recyclerView, progressBar);
        ToolBarHelper.setUpToolbar(this, "Available Meeting Slots", true, true);
    }

    @Override
    public void onItemClick(int position) {

    }
}