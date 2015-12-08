package uk.czcz.longrunningprocess;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    public static class PleaseDontRotateDuringThisOperationTask extends AsyncTask<Void, Integer, Void>
    {
        public interface ProgressListener
        {
            void progressUpdated(int progress);
        }

        private final WeakReference<ProgressListener> listener;

        public PleaseDontRotateDuringThisOperationTask(ProgressListener listener) {
            this.listener = new WeakReference<ProgressListener>(listener);
        }


        @Override
        protected Void doInBackground(Void... params) {
            for(int i = 0; i < 100; i++)
            {
                try {
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(i);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            ProgressListener listenerStrongReference = listener.get();
            if (listenerStrongReference != null) // We still have a reference to our listener
            {
                listenerStrongReference.progressUpdated(values[0]);
            }
        }
    }

    public static class CustomDialogFragment extends DialogFragment implements PleaseDontRotateDuringThisOperationTask.ProgressListener
    {
        private ProgressBar progressBar;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.mega_dialog, null);

            AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("Woop woop magoop").setView(dialogView).create();
            progressBar = (ProgressBar) dialogView.findViewById(R.id.progress);
            progressBar.setMax(100);
            return dialog;
        }

        @Override
        public void progressUpdated(int progress) {
            if (progressBar != null)
            {
                progressBar.setProgress(progress);
            }
        }
    }

    public static final String PROGRESS_DIALOG_TAG = "progressDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomDialogFragment dialogFragment = (CustomDialogFragment) getSupportFragmentManager().findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (dialogFragment == null)
        {
            dialogFragment = new CustomDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), PROGRESS_DIALOG_TAG);
        }
        new PleaseDontRotateDuringThisOperationTask(dialogFragment).execute();
    }
}
