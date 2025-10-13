package com.example.barberuapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class SubmitReviewTask extends AsyncTask<Void, Void, Boolean> {

    public interface ReviewSubmitListener {
        void onReviewSubmitted(boolean success, float stars, String reviewContent);
    }

    private final Context context;
    private final float stars;
    private final String reviewContent;
    private final ReviewSubmitListener listener;

    public SubmitReviewTask(Context context, float stars, String reviewContent, ReviewSubmitListener listener) {
        this.context = context;
        this.stars = stars;
        this.reviewContent = reviewContent;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // Simulate submission without actual network call
        return true; // always "success"
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Review submission failed.", Toast.LENGTH_LONG).show();
        }

        if (listener != null) {
            listener.onReviewSubmitted(success, stars, reviewContent);
        }
    }
}
