package com.inklily.tennischarting;

import com.inklily.tennischarting.MatchStorage.MatchStorageNotAvailableException;
import com.inklily.tennischarting.Point.Direction;
import com.inklily.tennischarting.Point.ServeDirection;
import com.inklily.tennischarting.Point.Stroke;
import com.inklily.tennischarting.PointEndDialog.OnPointEndListener;
import com.inklily.tennischarting.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * Activity to chart matches.
 * 
 * It's a trivial state machine:
 * 
 * SERVE -> STROKE -> POSITION
 *            |          |
 *            \----<<----/
 * 
 * @author mrdog
 */
public class MatchChartActivity extends FragmentActivity implements OnPointEndListener {
	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
	private static final float TAP_MAX_DIST = 10.0f;
	private static final double SWINGING_VOLLEY_MAX = Math.toRadians(60.0);
	private static final double SWINGING_VOLLEY_MIN = Math.toRadians(30.0);

	private static final double HALF_VOLLEY_MAX = Math.toRadians(-30.0);
	private static final double HALF_VOLLEY_MIN = Math.toRadians(-60.0);

	private static final double LOB_MAX = Math.toRadians(135.0);
	private static final double DROP_MIN = Math.toRadians(-135.0);

	private static final double TRICK_ANGLE = Math.toRadians(135.0);
	
	private enum State {
		SERVE,
		LOCATION,
		STROKE,
	}
	
	private State current_state = State.SERVE;

	private PointF mGestureStart = new PointF();
	private PointF mGestureEnd = new PointF();
	private boolean mActiveGesture = false;
	private boolean nextStrokeRightHanded = true;
    private boolean nextLocRightHanded = true;
	private boolean nextStrokeNear = true;
	private boolean disableInput;
	
	private Match match;
	private Point currentPoint;

	private SystemUiHider mSystemUiHider;

	private GestureOverlay mGestureOverlay;

	private GuideView shotGuide;
	private ServeGuide serveGuide;
	private LocationGuide locationGuide;
	private View centerLegend;

    private Stroke currentStroke;
    private SQLiteMatchStorage matchStorage;
    private Handler handler;

	private Runnable mLongPressRunnable = new Runnable() {
		@Override
		public void run() {
            Point p;

            // Add a partial point if necessary
            if (current_state == State.LOCATION) {
                p = new Point(currentPoint);
                p.addStroke(currentStroke);
            } else {
                p = currentPoint;
            }
			disableInput = true;

            // TODO: DI this class for testing
            PointEndDialog pointEndDialog = new PointEndDialog();
			pointEndDialog.show(match, p, getSupportFragmentManager(), "point_end");
			mActiveGesture = false;
		}
		
	};
	
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler = new Handler(getMainLooper());
		matchStorage = SQLiteMatchStorage.getGlobalInstance(this);
		disableInput = false;
		Long m_id = null;
		if (savedInstanceState != null) {
			savedInstanceState.getLong("match_id");
		} else {
			Bundle extras = this.getIntent().getExtras();
			if (extras != null && extras.containsKey("match_id")) {
				m_id = extras.getLong("match_id");
			}
		}
		
		if (m_id != null) {
			try {
				match = matchStorage.retrieveMatch(m_id);
			} catch (MatchStorageNotAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		setContentView(R.layout.activity_match_chart);

		final View contentView = findViewById(R.id.fullscreen_content);
		shotGuide = (GuideView) findViewById(R.id.shot_guide);
		locationGuide = (LocationGuide) findViewById(R.id.location_guide);
		serveGuide = (ServeGuide) findViewById(R.id.serve_guide);

		centerLegend = findViewById(R.id.center_legend);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mGestureOverlay = new GestureOverlay(this);
		shotGuide.addView(mGestureOverlay, params);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.hide();

		match = new Match(3, true, true);
		match.player1hand = 'R';
		match.player2hand = 'R';
		newPoint(true);
		
		updateUI();
	}
	
	private void newPoint(boolean firstServe) {
		currentPoint = new Point(firstServe, match.server());
	}

	private void updateHandedness() {
		int n = currentPoint.nextPlayer();

        // Handedness of the next shot returner
		nextLocRightHanded = match.rightHanded(n);
        // Handedness of the next shot striker
		nextStrokeRightHanded = match.rightHanded(n % 2 + 1);

        // Is the next stroke near to the camera?
        nextStrokeNear = match.near() ^ (currentPoint.shotCount() % 2 == 0);
	}

	private void savePoint() {
        try {
            match.addPoint(currentPoint, matchStorage);
        } catch (MatchStorageNotAvailableException e) {
            // TODO: notify the user
            e.printStackTrace();
        }

        if (currentPoint.isFault())
			newPoint(false);
		else
			newPoint(true);
	}
	
	private void updateUI() {
        updateHandedness();

		serveGuide.setVisibility(View.INVISIBLE);
		locationGuide.setVisibility(View.INVISIBLE);
		shotGuide.setVisibility(View.INVISIBLE);

        boolean deuce = match.deuceCourt();
        Log.d("updateUI", String.format("%s %s %s",
                nextStrokeNear ? "Near" : "Far",
                nextStrokeRightHanded ? "Right" : "Left",
                deuce ? "Deuce" : "Ad"));
		switch (current_state) {
		case SERVE:
			serveGuide.setVisibility(View.VISIBLE);
			serveGuide.setCourt(nextStrokeNear, deuce, false);
			break;
		case STROKE:
			shotGuide.setVisibility(View.VISIBLE);
			shotGuide.setCourt(!nextStrokeNear, false, nextStrokeRightHanded);
			break;
		case LOCATION:
			locationGuide.setVisibility(View.VISIBLE);
			locationGuide.setCourt(nextStrokeNear, false, nextLocRightHanded);
			break;
		}
	}

    private void setState(State s) {
        current_state = s;
        updateUI();
    }
	
	private void nextState() {
		switch (current_state) {
		case SERVE:
			current_state = State.STROKE;
			break;
		case LOCATION:
			current_state = State.STROKE;
			break;
		case STROKE:
			current_state = State.LOCATION;
			break;
		}
		
		updateUI();
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (disableInput)
			return false;

		if (action == MotionEvent.ACTION_DOWN) {
			handler.postDelayed(mLongPressRunnable, 1000);
			if (current_state == State.STROKE) {
				mGestureEnd.x = mGestureStart.x = event.getX();
				mGestureEnd.y = mGestureStart.y = event.getY();
				mActiveGesture = true;
			}
		} else if (action == MotionEvent.ACTION_UP) {
			handler.removeCallbacks(mLongPressRunnable);
			switch (current_state) {
			case LOCATION:
				recordStroke(locationGuide.getDirection(event.getX()));
				break;
			case STROKE:
				mGestureEnd.x = event.getX();
				mGestureEnd.y = event.getY();
				mActiveGesture = false;
				currentStroke = detectStroke();
				break;
			case SERVE:
				currentPoint.serve(serveGuide.getServeDirection(event.getX()));
				break;
			default:
				break;
			}
			nextState();
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (current_state == State.STROKE) {
				mGestureEnd.x = event.getX();
				mGestureEnd.y = event.getY();
				mGestureOverlay.invalidate();
			}
		} else {
			return super.onTouchEvent(event);
		}
		return false;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(mLongPressRunnable);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        if (match != null && match.id != null)
            outState.putLong("match_id", match.id);
	}

	/**
	 * Records the stroke based on the current gesture.
	 * @param direction direction of the stroke
	 */
	private boolean recordStroke(Direction direction) {

		updateHandedness();
		nextStrokeNear = !nextStrokeNear;

		currentPoint.addStroke(currentStroke, direction);

		Log.i("Shot", currentPoint.toString());
		
		return true;
	}

	private Point.Stroke detectCenterStroke(float dX, float dY) {
		double distance = Math.sqrt(dX*dX + dY*dY);
			
		if (distance < TAP_MAX_DIST * getResources().getDisplayMetrics().density)
			return Point.Stroke.UNKNOWN;

		double angle = Math.atan2(dY, dX);
		if (angle > TRICK_ANGLE || angle < -TRICK_ANGLE)
			return Point.Stroke.TRICK;
		else if (angle > 0)
			return Point.Stroke.FOREHAND_OVERHEAD;
		else
			return Point.Stroke.BACKHAND_OVERHEAD;
	}

	private Point.Stroke detectStroke(Point.StrokeIndex index, float dX, float dY) {
		double distance = Math.sqrt(dX*dX + dY*dY);
			
		if (distance < TAP_MAX_DIST * getResources().getDisplayMetrics().density)
			return index.GROUNDSTROKE;
		
		double angle = Math.atan2(dY, dX);
		if (angle > LOB_MAX || angle < DROP_MIN)
			return index.SLICE;
		if (angle > SWINGING_VOLLEY_MAX)
			return index.LOB;
		if (angle > SWINGING_VOLLEY_MIN)
			return index.SWINGINGVOLLEY;
		if (angle > HALF_VOLLEY_MAX)
			return index.VOLLEY;
		if (angle > HALF_VOLLEY_MIN)
			return index.HALFVOLLEY;
		if (angle > DROP_MIN)
			return index.DROPSHOT;

		return Point.Stroke.UNKNOWN;
	}

	private Point.Stroke detectStroke() {
		float dX = mGestureStart.x  - mGestureEnd.x;
		float dY = mGestureStart.y  - mGestureEnd.y;
		boolean left = mGestureStart.x < centerLegend.getLeft();
		boolean right = mGestureStart.x > centerLegend.getRight();
		boolean rightForehand = nextStrokeRightHanded != nextStrokeNear;
		
		if (!right)
			dX = -dX;

        // Find the stroke type
		if ((right && rightForehand) || (left && !rightForehand)) {
			return detectStroke(Point.FOREHAND_STROKES, dX, dY);
		} else if (left || right) {
			return detectStroke(Point.BACKHAND_STROKES, dX, dY);
		} else {
			return detectCenterStroke(dX, dY);
		}
	}

    @Override
    public void onPointComplete(Point p) {
        disableInput = false;
        currentPoint = p;
        savePoint();
        this.current_state = State.SERVE;
        updateUI();
    }

    @Override
    public void onPointContinue(Point p) {
        disableInput = false;
        if (p != null) {
            currentPoint = p;
            // If this is not a serve, reset to stroke selection
            if (currentPoint.shotCount() > 0)
                setState(State.STROKE);
        }
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

	public static class GuideView extends RelativeLayout {
		final float LINE_WIDTH = 15.0f;
		final float GUIDE_WIDTH = 2.0f;
		final int LINE_COLOR = Color.rgb(220, 220, 220);
		final int GUIDE_COLOR = Color.rgb(220, 220, 220);
		protected boolean nearCourt = true;
		protected boolean deuceCourt = true;
		protected boolean rightHanded = true;
		protected Paint mLinePaint = new Paint();
		protected Paint mGuidePaint = new Paint();

		public GuideView(Context context, AttributeSet attrs) {
			super(context, attrs);
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			float density = metrics.density;
			mGuidePaint.setColor(GUIDE_COLOR);
			mGuidePaint.setStrokeWidth(GUIDE_WIDTH * density);
			float dash_length = GUIDE_WIDTH * density * 2;
			float[] ivals = {dash_length,dash_length };
			mGuidePaint.setPathEffect(new DashPathEffect(ivals, 0.0f));
			mLinePaint.setColor(LINE_COLOR);
			mLinePaint.setStrokeWidth(LINE_WIDTH * density);
		}

		public void setCourt(boolean near, boolean deuce, boolean righthand) {
			if (nearCourt != near || deuceCourt != deuce || righthand != rightHanded)
				this.invalidate();

			nearCourt = near;
			deuceCourt = deuce;
			rightHanded = righthand;
		}

		protected void drawGuides(Canvas canvas) {
			float h = (float)this.getHeight();
			float w = (float)this.getWidth();

			// Draw guides
			float g1x = w * 0.3f;
			float g2x = w * 0.7f;
			canvas.drawLine(g1x, 0.0f, g1x, h, mGuidePaint);
			canvas.drawLine(g2x, 0.0f, g2x, h, mGuidePaint);
		}
	}

	public static class ServeGuide extends GuideView {
		final float SERVICE_LINE_INSET = 50.0f;
		final float T_LINE_INSET = 40.0f;
		private TextView serve_l = null;
		private TextView serve_r = null;
		
		public ServeGuide(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		public ServeDirection getServeDirection(float x) {
			float w = (float)this.getWidth();
			float g1x = w * 0.3f;
			float g2x = w * 0.7f;
			if (x >= g1x && x <= g2x) {
				return Point.ServeDirection.BODY;
			}

			if ((nearCourt && deuceCourt) || (!nearCourt && !deuceCourt)) {
				if (x < g1x)
					return Point.ServeDirection.T;
				else
					return Point.ServeDirection.WIDE;
			} else {
				if (x < g1x)
					return Point.ServeDirection.WIDE;
				else
					return Point.ServeDirection.T;
			}
					
		}

		@Override
		public void setCourt(boolean near, boolean deuce, boolean righthand) {
			super.setCourt(near, deuce, righthand);
			
			if (serve_l == null)
				serve_l = ((TextView) this.findViewById(R.id.serve_left_loc));
			if (serve_r == null)
				serve_r = ((TextView) this.findViewById(R.id.serve_right_loc));
			
			if ((near && deuce) || (!near && !deuce)) {
				serve_l.setText(R.string.serve_t);
				serve_r.setText(R.string.serve_wide);
			} else {
				serve_l.setText(R.string.serve_wide);
				serve_r.setText(R.string.serve_t);
			}

		}

		@SuppressWarnings("ConstantConditions")
        @Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			float h = (float)this.getHeight();
			float w = (float)this.getWidth();

			// Draw court lines
			if (deuceCourt && nearCourt) {
				canvas.drawLine(w - LINE_WIDTH / 2, 0.0f, w - LINE_WIDTH / 2, h, mLinePaint); // sideline
				canvas.drawLine(0.0f, h - SERVICE_LINE_INSET, w, h - SERVICE_LINE_INSET, mLinePaint); // service line
				canvas.drawLine(T_LINE_INSET, 0.0f, T_LINE_INSET, h - SERVICE_LINE_INSET, mLinePaint); // T line
			} else if (deuceCourt && !nearCourt) {
				canvas.drawLine(LINE_WIDTH / 2, 0.0f, LINE_WIDTH / 2, h, mLinePaint); // sideline
				canvas.drawLine(0.0f, SERVICE_LINE_INSET, w, SERVICE_LINE_INSET, mLinePaint); // service line
				canvas.drawLine(w - T_LINE_INSET, SERVICE_LINE_INSET, w - T_LINE_INSET, h, mLinePaint); // T line
			} else if (!deuceCourt && nearCourt) {
				canvas.drawLine(LINE_WIDTH / 2, 0.0f, LINE_WIDTH / 2, h, mLinePaint); // sideline
				canvas.drawLine(0.0f, h - SERVICE_LINE_INSET, w, h - SERVICE_LINE_INSET, mLinePaint); // service line
				canvas.drawLine(w - T_LINE_INSET, 0, w - T_LINE_INSET, h - SERVICE_LINE_INSET, mLinePaint); // T line
			} else if (!deuceCourt && !nearCourt) {
				canvas.drawLine(w - LINE_WIDTH / 2, 0.0f, w - LINE_WIDTH / 2, h, mLinePaint); // sideline
				canvas.drawLine(0.0f, SERVICE_LINE_INSET, w, SERVICE_LINE_INSET, mLinePaint); // service line
				canvas.drawLine(T_LINE_INSET, SERVICE_LINE_INSET, T_LINE_INSET, h, mLinePaint); // T line
			}
			
			this.drawGuides(canvas);
		}
	}

	public static class LocationGuide extends GuideView {
		private TextView shot_l = null;
		private TextView shot_r = null;

		public LocationGuide(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setCourt(boolean near, boolean deuce, boolean righthand) {
			super.setCourt(near, deuce, righthand);
			
			if (shot_l == null)
				shot_l = ((TextView) this.findViewById(R.id.shot_left_loc));
			if (shot_r == null)
				shot_r = ((TextView) this.findViewById(R.id.shot_right_loc));
			
			if ((near && righthand) || (!near && !righthand)) {
				shot_l.setText(R.string.backhand_side);
				shot_r.setText(R.string.forehand_side);
			} else {
				shot_l.setText(R.string.forehand_side);
				shot_r.setText(R.string.backhand_side);
			}
		}

		public Direction getDirection(float x) {
			float w = (float)this.getWidth();
			float g1x = w * 0.3f;
			float g2x = w * 0.7f;
			if (x >= g1x && x <= g2x) {
				return Point.Direction.MIDDLE;
			}

			if (nearCourt) {
				if (x < g1x)
					return Point.Direction.AD;
				else
					return Point.Direction.DEUCE;
			} else {
				if (x < g1x)
					return Point.Direction.DEUCE;
				else
					return Point.Direction.AD;
			}
					
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			float h = (float)this.getHeight();
			float w = (float)this.getWidth();

			// Draw court lines
			float T_y1 = 0.0f;
			float T_y2 = 0.0f;
			if (nearCourt) {
				T_y1 = h * 0.33f;
				T_y2 = 0.0f;
			} else {
				T_y1 = h * 0.66f;
				T_y2 = h;
			}
			
			// Sidelines
			canvas.drawLine(LINE_WIDTH / 2, 0.0f, LINE_WIDTH / 2, h, mLinePaint);
			canvas.drawLine(w - LINE_WIDTH / 2, 0.0f, w - LINE_WIDTH / 2, h, mLinePaint);

			// Baseline
			if (nearCourt)
				canvas.drawLine(0.0f, h - LINE_WIDTH / 2, w, h - LINE_WIDTH / 2, mLinePaint);
			else
				canvas.drawLine(0.0f, LINE_WIDTH / 2, w, LINE_WIDTH / 2, mLinePaint);
			
			// Service line
			canvas.drawLine(0.0f, T_y1, w, T_y1, mLinePaint);

			// T line
			canvas.drawLine(w / 2, T_y1, w / 2, T_y2, mLinePaint);

			this.drawGuides(canvas);
			// TODO: Draw horizontal guides if needed
		}
	}

	public static class ShotGuide extends GuideView {
		private TextView right_hand = null;
		private TextView left_hand = null;

		public ShotGuide(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setCourt(boolean near, boolean deuce, boolean righthand) {
			super.setCourt(near, deuce, righthand);
			
			if (right_hand == null)
				right_hand = ((TextView) this.findViewById(R.id.right_hand));
			if (left_hand == null)
				left_hand = ((TextView) this.findViewById(R.id.left_hand));
			
			if ((near && rightHanded) || (!near && !righthand)) {
				left_hand.setText(R.string.backhand);
				right_hand.setText(R.string.forehand);
			} else {
				left_hand.setText(R.string.forehand);
				right_hand.setText(R.string.backhand);
			}
		}
	}

}
