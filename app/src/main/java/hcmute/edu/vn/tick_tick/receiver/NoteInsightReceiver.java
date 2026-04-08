package hcmute.edu.vn.tick_tick.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import hcmute.edu.vn.tick_tick.util.AINoteHelper;

/**
 * Lightweight receiver to process note content via AI and broadcast the generated summary/tags.
 * Trigger with action {@link #ACTION_AI_NOTE_INSIGHT} and extras {@link #EXTRA_NOTES}.
 */
public class NoteInsightReceiver extends BroadcastReceiver {

    public static final String ACTION_AI_NOTE_INSIGHT = "hcmute.edu.vn.tick_tick.ACTION_AI_NOTE_INSIGHT";
    public static final String ACTION_AI_NOTE_RESULT = "hcmute.edu.vn.tick_tick.ACTION_AI_NOTE_RESULT";
    public static final String EXTRA_NOTES = "notes";
    public static final String EXTRA_SUMMARY = "ai_summary";
    public static final String EXTRA_TAGS = "ai_tags";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION_AI_NOTE_INSIGHT.equals(intent.getAction())) {
            return;
        }

        String notes = intent.getStringExtra(EXTRA_NOTES);
        if (TextUtils.isEmpty(notes)) {
            Log.d("NoteInsightReceiver", "No notes provided; skipping AI insight");
            return;
        }

        AINoteHelper.Suggestion suggestion = AINoteHelper.suggest(notes);

        Intent result = new Intent(ACTION_AI_NOTE_RESULT);
        result.putExtra(EXTRA_SUMMARY, suggestion.summary);
        result.putExtra(EXTRA_TAGS, suggestion.tags);
        context.sendBroadcast(result);
    }
}
