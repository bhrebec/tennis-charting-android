package com.inklily.tennischarting;

import com.inklily.tennischarting.MatchStorage.MatchStorageNotAvailableException;
import com.inklily.tennischarting.Point.Direction;
import com.inklily.tennischarting.Point.ServeDirection;
import com.inklily.tennischarting.Point.Stroke;
import com.inklily.tennischarting.PointEndDialog.OnPointEndListener;
import com.inklily.tennischarting.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.view.ViewGroup;
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
	private static final float TAP_MAX_DIST = 15.0f;
	private static final double SWINGING_VOLLEY_MAX = Math.toRadians(60.0);
	private static final double SWINGING_VOLLEY_MIN = Math.toRadians(30.0);

	private static final double HALF_VOLLEY_MAX = Math.toRadians(-30.0);
	private static final double HALF_VOLLEY_MIN = Math.toRadians(-60.0);

	private static final double LOB_MAX = Math.toRadians(135.0);
	private static final double DROP_MIN = Math.toRadians(-135.0);

	private static final double TRICK_ANGLE = Math.toRadians(135.0);

    private static final double NET_APPROACH_MIN = Math.toRadians(45.0);
    private static final double NET_APPROACH_MAX = Math.toRadians(135.0);

	private enum State {
		SERVE,
		LOCATION,
		STROKE,
	}
	
	private State current_state = State.SERVE;

	private PointF mGestureStart = new PointF();
	private PointF mGestureEnd = new PointF();
	private boolean mActiveGesture = false;
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

		final ViewGroup contentView = (ViewGroup) findViewById(R.id.fullscreen_content);
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
		contentView.addView(mGestureOverlay, params);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider.hide();

		newPoint();
		
		updateUI();
	}
	
	private void newPoint() {
		currentPoint = new Point(match.server());
	}

	private void savePoint() {
        try {
            match.addPoint(currentPoint, matchStorage);
        } catch (MatchStorageNotAvailableException e) {
            // TODO: notify the user
            e.printStackTrace();
        }

        newPoint();

        if (match.isComplete()) {
            endMatch();
        }
	}

    private void endMatch () {
        Intent intent = new Intent(this, MatchDetailActivity.class);
        intent.putExtra("match_id", match.id);
        intent.putExtra("review", false);
        startActivity(intent);
    }

	
	private void updateUI() {
		serveGuide.setVisibility(View.INVISIBLE);
		locationGuide.setVisibility(View.INVISIBLE);
		shotGuide.setVisibility(View.INVISIBLE);

		switch (current_state) {
		case SERVE:
			serveGuide.setVisibility(View.VISIBLE);
			serveGuide.setPoint(match, currentPoint);
			break;
		case STROKE:
			shotGuide.setVisibility(View.VISIBLE);
			shotGuide.setPoint(match, currentPoint);
			break;
		case LOCATION:
			locationGuide.setVisibility(View.VISIBLE);
			locationGuide.setPoint(match, currentPoint);
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
            mActiveGesture = true;
            mGestureEnd.x = mGestureStart.x = event.getX();
            mGestureEnd.y = mGestureStart.y = event.getY();
            handler.postDelayed(mLongPressRunnable, 1000);
		} else if (action == MotionEvent.ACTION_UP) {
			handler.removeCallbacks(mLongPressRunnable);
            mGestureEnd.x = event.getX();
            mGestureEnd.y = event.getY();
			switch (current_state) {
			case LOCATION:
				recordStroke(locationGuide.getDirection(event.getX()));
				break;
			case STROKE:
				currentStroke = detectStroke();
				break;
			case SERVE:
                recordServe(serveGuide.getServeDirection(event.getX()));
				break;
			default:
				break;
			}
            mActiveGesture = false;
			nextState();
		} else if (action == MotionEvent.ACTION_MOVE) {
            mGestureEnd.x = event.getX();
            mGestureEnd.y = event.getY();
            mGestureOverlay.invalidate();
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
        float dX = mGestureStart.x  - mGestureEnd.x;
        float dY = mGestureStart.y  - mGestureEnd.y;

        if (distance(dX, dY) > TAP_MAX_DIST) {
            double angle = angle(dX, dY);
            if (angle > NET_APPROACH_MIN && angle < NET_APPROACH_MAX) {
                currentPoint.addStroke(currentStroke, direction, null, Point.Approach.NET_APPROACH);
            } else {
                currentPoint.addStroke(currentStroke, Direction.UNKNOWN);
            }
        } else {
            currentPoint.addStroke(currentStroke, direction);
        }

		Log.i("Shot", currentPoint.toString());
		
		return true;
	}

	private Point.Stroke detectCenterStroke(double distance, double angle) {

		if (distance < TAP_MAX_DIST * getResources().getDisplayMetrics().density)
			return Point.Stroke.UNKNOWN;

		if (angle > TRICK_ANGLE || angle < -TRICK_ANGLE)
			return Point.Stroke.TRICK;
		else if (angle > 0)
			return Point.Stroke.FOREHAND_OVERHEAD;
		else
			return Point.Stroke.BACKHAND_OVERHEAD;
	}

    private static double distance(float dX, float dY) {
        return Math.sqrt(dX*dX + dY*dY);
    }

    private static double angle(float dX, float dY) {
        return Math.atan2(dY, dX);
    }

	private Point.Stroke detectStroke(Point.StrokeIndex index, double distance, double angle) {
		if (distance < TAP_MAX_DIST * getResources().getDisplayMetrics().density)
			return index.GROUNDSTROKE;
		
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
		boolean rightForehand = shotGuide.isRightHanded();
		
		if (!right)
			dX = -dX;

        // Find the stroke type
		if ((right && rightForehand) || (left && !rightForehand)) {
			return detectStroke(Point.FOREHAND_STROKES, distance(dX, dY), angle(dX, dY));
		} else if (left || right) {
			return detectStroke(Point.BACKHAND_STROKES, distance(dX, dY), angle(dX, dY));
		} else {
			return detectCenterStroke(dX, dY);
		}
	}

    private void recordServe(Point.ServeDirection dir) {
        float dX = mGestureStart.x  - mGestureEnd.x;
        float dY = mGestureStart.y  - mGestureEnd.y;

        if (distance(dX, dY) > TAP_MAX_DIST) {
            double angle = angle(dX, dY);
            if (angle > NET_APPROACH_MIN && angle < NET_APPROACH_MAX) {
                currentPoint.serve(dir, Point.Approach.SERVE_AND_VOLLEY);
            } else {
                currentPoint.serve(ServeDirection.UNKNOWN);
            }
        } else {
            currentPoint.serve(dir);
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
            // Fix the UI in case the point has changed
            if (currentPoint.shotCount() > 0)
                setState(State.STROKE);
            else if (currentPoint.shotCount() == 0)
                setState(State.SERVE);
        }
    }

    @Override
    public void onMatchOver() {
        savePoint();
        endMatch();
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
		private boolean nearCourt = true;
		private boolean deuceCourt = true;
        protected boolean strokeRightHanded;
        protected boolean locRightHanded;
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

        protected boolean near() {
            return nearCourt;
        }

        protected boolean deuce() {
            return deuceCourt;
        }

        public void setPoint(Match match, Point point) {
			nearCourt = match.nextNearFor(point);
			deuceCourt = match.deuceCourt();
			strokeRightHanded = match.nextStrokeRighthandedFor(point);
            locRightHanded = match.nextStrokeRighthandedFor(point);

            invalidate();
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

        public boolean isRightHanded() {
            return strokeRightHanded == nearCourt;
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

        @Override
        protected boolean near() {
            return !super.near();
        }
		
		public ServeDirection getServeDirection(float x) {
			float w = (float)this.getWidth();
			float g1x = w * 0.3f;
			float g2x = w * 0.7f;
			if (x >= g1x && x <= g2x) {
				return Point.ServeDirection.BODY;
			}

			if (near() == deuce()) {
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
        public void setPoint(Match match, Point point) {
			super.setPoint(match, point);

            ((TextView) this.findViewById(R.id.serve_server)).setText(match.playerName(match.server()));
            ((TextView) this.findViewById(R.id.serve_pts_score)).setText(
                    String.format("%s %s %s",
                            match.score().isFirstServe() ?
                                    getResources().getString(R.string.first) :
                                    getResources().getString(R.string.second),
                            getResources().getString(R.string.serving_at),
                            match.score().ptsScore())
            );
            ((TextView) this.findViewById(R.id.serve_score)).setText(match.score().gameScore());

			if (serve_l == null)
				serve_l = ((TextView) this.findViewById(R.id.serve_left_loc));
			if (serve_r == null)
				serve_r = ((TextView) this.findViewById(R.id.serve_right_loc));
			
			if (near() == deuce()) {
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
			if (deuce() && near()) {
				canvas.drawLine(w - LINE_WIDTH / 2, 0.0f, w - LINE_WIDTH / 2, h, mLinePaint); // sideline
				canvas.drawLine(0.0f, h - SERVICE_LINE_INSET, w, h - SERVICE_LINE_INSET, mLinePaint); // service line
				canvas.drawLine(T_LINE_INSET, 0.0f, T_LINE_INSET, h - SERVICE_LINE_INSET, mLinePaint); // T line
			} else if (deuce() && !near()) {
				canvas.drawLine(LINE_WIDTH / 2, 0.0f, LINE_WIDTH / 2, h, mLinePaint); // sideline
				canvas.drawLine(0.0f, SERVICE_LINE_INSET, w, SERVICE_LINE_INSET, mLinePaint); // service line
				canvas.drawLine(w - T_LINE_INSET, SERVICE_LINE_INSET, w - T_LINE_INSET, h, mLinePaint); // T line
			} else if (!deuce() && near()) {
				canvas.drawLine(LINE_WIDTH / 2, 0.0f, LINE_WIDTH / 2, h, mLinePaint); // sideline
				canvas.drawLine(0.0f, h - SERVICE_LINE_INSET, w, h - SERVICE_LINE_INSET, mLinePaint); // service line
				canvas.drawLine(w - T_LINE_INSET, 0, w - T_LINE_INSET, h - SERVICE_LINE_INSET, mLinePaint); // T line
			} else if (!deuce() && !near()) {
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
        protected boolean near() {
            return !super.near();
        }

        @Override
        public void setPoint(Match match, Point point) {
            super.setPoint(match, point);

			if (shot_l == null)
				shot_l = ((TextView) this.findViewById(R.id.shot_left_loc));
			if (shot_r == null)
				shot_r = ((TextView) this.findViewById(R.id.shot_right_loc));

			if (near() == locRightHanded) {
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

			if (near()) {
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

        public boolean isRightHanded() {
            return locRightHanded == near();
        }

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			float h = (float)this.getHeight();
			float w = (float)this.getWidth();

			// Draw court lines
			float T_y1 = 0.0f;
			float T_y2 = 0.0f;
			if (near()) {
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
			if (near())
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
        public void setPoint(Match match, Point point) {
            super.setPoint(match, point);
            ((TextView) this.findViewById(R.id.stoke_player)).setText(match.playerName(point.nextPlayer()));

			if (right_hand == null)
				right_hand = ((TextView) this.findViewById(R.id.right_hand));
			if (left_hand == null)
				left_hand = ((TextView) this.findViewById(R.id.left_hand));
			
			if (near() == strokeRightHanded) {
				left_hand.setText(R.string.backhand);
				right_hand.setText(R.string.forehand);
			} else {
				left_hand.setText(R.string.forehand);
				right_hand.setText(R.string.backhand);
			}
		}
	}

}
