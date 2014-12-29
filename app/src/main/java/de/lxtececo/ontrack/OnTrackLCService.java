package de.lxtececo.ontrack;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.util.Random;

/**
 * A {@link Service} that publishes a {@link LiveCard} in the timeline.
 */
public class OnTrackLCService extends Service {

    private static final String LIVE_CARD_TAG = "OnTrackLCService";

    private LiveCard mLiveCard;
    private RemoteViews mLiveCardView;

    private int trainID, seatID, scheduledTime;
    private Random mIntGenerator;


    private final Handler mHandler = new Handler();
    private final UpdateLiveCardRunnable mUpdateLiveCardRunnable = new UpdateLiveCardRunnable();
    private static final long DELAY_MILLIS = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        mIntGenerator = new Random();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            // Get  an instance of a live card
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            // Inflate a layout into a remote view
            mLiveCardView= new RemoteViews(getPackageName(), R.layout.on_track_lc);

            //Setup initial values
            trainID=0;
            seatID=0;
            scheduledTime=0;
            mLiveCardView.setTextViewText(R.id.train_id_value, String.valueOf(trainID));
            mLiveCardView.setTextViewText(R.id.scheduled_time_value, String.valueOf(scheduledTime));

            //Set up live card's action with a pending intent to show a menu when tapped
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            //Publish the live card
            mLiveCard.publish(PublishMode.REVEAL);

            //Queue the update text runnable
            mHandler.post(mUpdateLiveCardRunnable);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            //Stop the handler from queuing more runnable jobs
            mUpdateLiveCardRunnable.setStop(true);
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    /**
     * Runnable that updates live card contents
     */
    private class UpdateLiveCardRunnable implements Runnable{

        private boolean mIsStopped = false;

        /*
         * Updates the card with a fake score every 30 seconds as a demonstration.
         * You also probably want to display something useful in your live card.
         *
         * If you are executing a long running task to get data to update a
         * live card(e.g, making a web call), do this in another thread or
         * AsyncTask.
         */
        public void run(){
            if(!isStopped()){
                // Get train id.
                trainID = 611;
                seatID = 29;
                scheduledTime += mIntGenerator.nextInt(5);

                // Update the remote view with the train connection.
                mLiveCardView.setTextViewText(R.id.train_id_value,
                        String.valueOf(trainID));
                mLiveCardView.setTextViewText(R.id.scheduled_time_value,
                        String.valueOf(scheduledTime));

                // Always call setViews() to update the live card's RemoteViews.
                mLiveCard.setViews(mLiveCardView);

                // Queue another score update in 30 seconds.
                mHandler.postDelayed(mUpdateLiveCardRunnable, DELAY_MILLIS);
            }
        }

        public boolean isStopped() {
            return mIsStopped;
        }

        public void setStop(boolean isStopped) {
            this.mIsStopped = isStopped;
        }
    }

    /**
     * Runnable for web calls
    private class OnTrackServiceRunnable implements Runnable {}
     */
}
