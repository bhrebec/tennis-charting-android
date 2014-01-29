package com.inklily.tennischarting;

import com.example.tennischarting.R;
import com.inklily.tennischarting.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MatchChartActivity extends Activity {
	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
	
	private enum Handedness {
		LEFT,
		RIGHT
	}

	private enum Court {
		NEAR_COURT,
		FAR_COURT
	}
	
	private PointF mGestureStart = new PointF();
	private PointF mGestureEnd = new PointF();
	private boolean mActiveGesture = false;


	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private GestureOverlay mGestureOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

		setContentView(R.layout.activity_match_chart);

		final View contentView = findViewById(R.id.fullscreen_content);
		final RelativeLayout shotGuide = (RelativeLayout) findViewById(R.id.shot_guide);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mGestureOverlay = new GestureOverlay(this);
		shotGuide.addView(mGestureOverlay);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.hide();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mGestureEnd.x = mGestureStart.x = event.getX();
				mGestureEnd.y = mGestureStart.y = event.getY();
				mActiveGesture = true;
				break;
			case MotionEvent.ACTION_MOVE:
				mGestureEnd.x = event.getX();
				mGestureEnd.y = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				mGestureEnd.x = event.getX();
				mGestureEnd.y = event.getY();
				recordPoint();
				mActiveGesture = false;
				break;
		}
		mGestureOverlay.invalidate();
		return super.onTouchEvent(event);
	}
	
	public void recordPoint() {
	}

	public String detectPoint() {
		return "";
	}
	
	public class GestureOverlay extends View {
		Paint mPaint = new Paint();

		public GestureOverlay(Context context) {
			super(context);
			mPaint.setColor(Color.RED);
			mPaint.setStrokeWidth(10.0f);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (mActiveGesture) {
				canvas.drawLine(mGestureStart.x, mGestureStart.y, mGestureEnd.x, mGestureEnd.y, mPaint);
			}
		}
	}
}
