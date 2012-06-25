package org.test.download;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DownloadActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	Context c = this;
	ProgressBar pBar;
	LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mReceiver;
    LinearLayout ll;
    Button btnStart,btnPause;
    DownloadService ds;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ll = (LinearLayout) findViewById(R.id.container);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnStart.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				ds = new DownloadService("http://mediastudio.ir/dummy", c, pBar);
				btnPause.setEnabled(true);
			}
		});
        btnPause.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				ds.cancel();
				btnPause.setEnabled(false);
				btnStart.setEnabled(true);
			}
		});
        
        /*******************************************/
        /*				Broadcast Receiver		   */
        /*******************************************/
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("org.test.download.DOWNLOAD_STARTED");
        filter.addAction("org.test.download.DOWNLOAD_FINISHED");
        filter.addAction("org.test.download.DOWNLOAD_CANCELLED");
        pBar = new ProgressBar(c,null,android.R.attr.progressBarStyleHorizontal);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            	if(intent.getAction().equals("org.test.download.DOWNLOAD_STARTED")){
            		Toast.makeText(c, "Download started!", Toast.LENGTH_SHORT).show();
            		if(!pBar.isShown()){
            			ll.addView(pBar);
            		}
            		btnStart.setEnabled(false);
            	}
            	if(intent.getAction().equals("org.test.download.DOWNLOAD_FINISHED")){
            		Toast.makeText(c, "Download finished!", Toast.LENGTH_SHORT).show();
            		Log.d("status","Download finished");
            		ll.removeView(pBar);
            	}
            	if(intent.getAction().equals("org.test.download.DOWNLOAD_CANCELLED")){
            		Toast.makeText(c, "Download cancelled!", Toast.LENGTH_SHORT).show();
            		Log.d("status","Download cancelled");
            		ll.removeView(pBar);
            	}
            }
        };
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}