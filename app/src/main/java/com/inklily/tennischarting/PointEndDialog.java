package com.inklily.tennischarting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;


/**
 * In-match dialog for various functions.
 *
 * TODO: rename.
 */
public class PointEndDialog extends DialogFragment {
	private RadioGroup mServeEndGroup;
	private RadioGroup mPointEndGroup;
	private RadioGroup mErrorGroupA;
	private RadioGroup mErrorGroupB;
	private Button mAddNote;
	private Button mContinuePoint;
	private Button mNextPoint;
	private Button mRetire;
	private Button mEditPoint;
	private EditText mPointEditor;
	private RadioButton mFootFault;
	private ViewGroup mRootView;
	private Point mPoint;
	private boolean mServe;
	private String[] mPlayers = new String[2];
    private Match mMatch;
    private TextView mScore;
    private boolean editingPoint;


    View.OnClickListener mUnknownListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getPlayerDialog("Who won the point?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if ((which + 1) == mMatch.server()) {
                        mPoint.givePoint(Point.PointGiven.POINT_SERVER);
                    } else {
                        mPoint.givePoint(Point.PointGiven.POINT_RETURNER);
                    }
                    finishPoint();
                }
            });
        }
    };

    public interface OnPointEndListener {
		public void onPointComplete(Point p);
		public void onPointContinue(Point p);
        public void onMatchOver();
        public void onReloadMatch();
    }
	
	// Kludge workaround for android RadioButton bug #4785
	private boolean mClearingErrors = false; 
	private class ErrorCheckListener implements OnCheckedChangeListener {
		private RadioGroup mClear;
		public ErrorCheckListener(RadioGroup clear) {
			mClear = clear;
		}

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (!mClearingErrors && checkedId != -1) {
				mClearingErrors = true;
				mClear.clearCheck();
				onOutcomeChecked(checkedId);
			}
			mClearingErrors = false;
		}
	}
	
	
	private ViewGroup mErrors;
    private boolean mReloadMatch;
	OnPointEndListener pointEndListener;

	private OnClickListener mAddNoteListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			(new DialogFragment() {
				@Override
			    public Dialog onCreateDialog(Bundle savedInstanceState) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Edit Note");
					return builder.create();
				}
			}).show(getActivity().getSupportFragmentManager(), "player_dialog");
		}
	};


    // "More... " dialog actions
    private DialogInterface.OnClickListener mMoreDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == 0) { // Point Penalty
                getPlayerDialog(R.string.penalty_prompt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ((which + 1) == mMatch.server())
                            mPoint.givePoint(Point.PointGiven.POINT_SERVER_PENALTY);
                        else
                            mPoint.givePoint(Point.PointGiven.POINT_RETURNER_PENALTY);
                        finishPoint();
                    }
                });
            } else if (which == 1) { // Retirement
                getPlayerDialog(R.string.retired_prompt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPoint.comments += mPlayers[which] + " retired";
                        finishPoint();
                        pointEndListener.onMatchOver();
                    }
                });
            } else if (which == 2) { // Flip near court
                mMatch.nearServerFirst = !mMatch.nearServerFirst;
            } else if (which == 3) { // Clear Point
                mPoint.setPoint("");
                continuePoint();
            } else if (which == 4) { // Edit match info
                Intent it = new Intent(PointEndDialog.this.getActivity(), MatchInfoActivity.class);
                it.putExtra("match_id", mMatch.id);
                startActivity(it);
                mReloadMatch = true;
            }
        }
    };

    private OnClickListener mMoreListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            (new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("More Options...");
                    final String[] items = { "Point Penalty", "Retirement", "Flip Court Vertically", "Clear Current Point",
                    "Edit Match Info"};
                    builder.setItems(items, mMoreDialogListener);
                    return builder.create();
                }
            }).show(getActivity().getSupportFragmentManager(), "more_dialog");
        }
    };

	private static SparseArray<Point.PointOutcome> pointOutcomeMap = new SparseArray<Point.PointOutcome>();

	{
		pointOutcomeMap.put(R.id.point_ace, Point.PointOutcome.ACE);
		pointOutcomeMap.put(R.id.point_winner, Point.PointOutcome.WINNER);
		pointOutcomeMap.put(R.id.point_error_deep, Point.PointOutcome.DEEP);
		pointOutcomeMap.put(R.id.point_error_net, Point.PointOutcome.NET);
		pointOutcomeMap.put(R.id.point_error_shank, Point.PointOutcome.SHANK);
		pointOutcomeMap.put(R.id.point_error_unknown, Point.PointOutcome.UNKNOWN);
		pointOutcomeMap.put(R.id.point_service_winner, Point.PointOutcome.UNRETURNABLE);
		pointOutcomeMap.put(R.id.point_error_wide, Point.PointOutcome.WIDE);
		pointOutcomeMap.put(R.id.point_error_wide_deep, Point.PointOutcome.WIDE_DEEP);
		pointOutcomeMap.put(R.id.point_error_foot_fault, Point.PointOutcome.FOOT_FAULT);
	}

    private void getPlayerDialog(int prompt, final DialogInterface.OnClickListener listener) {
        getPlayerDialog(getResources().getString(prompt), listener);
    }

	private void getPlayerDialog(final String prompt,
                                 final DialogInterface.OnClickListener listener) {
		(new DialogFragment() {
			@Override
		    public Dialog onCreateDialog(Bundle savedInstanceState) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(prompt);
				builder.setItems(mPlayers, listener);
				return builder.create();
			}
		}).show(getActivity().getSupportFragmentManager(), "player_dialog");
	}
	
	private void onOutcomeChecked(int checkedId) {
		Point.PointOutcome o = pointOutcomeMap.get(checkedId);
		if (o == null)
			return;

		if (mServe) {
			mPoint.endPoint(o);
		} else {
			Point.ErrorType et;
			if (mPointEndGroup.getCheckedRadioButtonId() == R.id.point_forced_error)
				et = Point.ErrorType.FORCED;
			else
				et = Point.ErrorType.UNFORCED;
			mPoint.endPoint(o, et);
		}

        Score score = mMatch.specScore(mPoint);
		mNextPoint.setVisibility(View.VISIBLE);
        if (mPoint.isFault() && mMatch.score().isFirstServe()) {
            mNextPoint.setText(getResources().getString(R.string.second_serve) + " "
                    + mMatch.playerLastname(mMatch.server()));
        } else {
            int winner = 0;
            if (mPoint.winner() == Point.PointWinner.SERVER)
                winner = mMatch.server();
            else if (mPoint.winner() == Point.PointWinner.RETURNER)
                winner = mMatch.returner();

            if (winner != 0) {
                String name = mMatch.playerLastname(winner);
                String msg;
                if (score.isComplete()) {
                    msg = getResources().getString(R.string.match) + " " + name;
                } else if (score.isNewSet()) {
                    msg = getResources().getString(R.string.set) + " " + name;
                } else if (score.isNewGame()) {
                    msg = getResources().getString(R.string.game) + " " + name;
                } else  {
                    msg = getResources().getString(R.string.point) + " " + name;
                }

                mNextPoint.setText(msg);
            } else {
                mNextPoint.setText(R.string.next_point);
            }
        }

		mPointEditor.setText(mPoint.toString());
        mScore.setText(score.toString());
        mScore.setTextColor(Color.GREEN);
	}

	private void onErrorGroup() {
		int errorId = mErrorGroupA.getCheckedRadioButtonId();
		if (errorId == -1)
			errorId = mErrorGroupB.getCheckedRadioButtonId();
		onOutcomeChecked(errorId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.point_end_dialog, container, false);
		mServeEndGroup = (RadioGroup) mRootView.findViewById(R.id.serve_end_group);
		mServeEndGroup.setOnCheckedChangeListener(new OnCheckedChangeListener () {
			@Override
			public void onCheckedChanged(RadioGroup group, int id) {
				if (!mServe)
					return;

				if (id == -1) {
					mPoint.reopenPoint(); // Needed to work around a bug in earlier android RadioButton
				} else if (id == R.id.point_fault) {
					onErrorGroup();
					mErrors.setVisibility(View.VISIBLE);
				} else if (id == R.id.point_service_winner || id == R.id.point_ace) {
					onOutcomeChecked(id);
					mErrors.setVisibility(View.INVISIBLE);
				}
				mRootView.invalidate();
			}
		});
		
		mPointEndGroup = (RadioGroup) mRootView.findViewById(R.id.point_end_group);
		mPointEndGroup.setOnCheckedChangeListener(new OnCheckedChangeListener () {
			@Override
			public void onCheckedChanged(RadioGroup group, int id) {
				if (mServe)
					return;

				if (id == -1) {
					mPoint.reopenPoint(); // Needed to work around a bug in earlier android RadioButton
				} else if (id == R.id.point_unforced_error || id == R.id.point_forced_error) {
					onErrorGroup();
					mErrors.setVisibility(View.VISIBLE);
				} else if (id == R.id.point_winner) {
					onOutcomeChecked(id);
					mErrors.setVisibility(View.INVISIBLE);
				}
				mRootView.invalidate();
			}
		});
		
		mErrors = (ViewGroup) mRootView.findViewById(R.id.point_errors);
		mErrorGroupA = (RadioGroup) mRootView.findViewById(R.id.point_error_group_A);
		mErrorGroupB = (RadioGroup) mRootView.findViewById(R.id.point_error_group_B);

		mErrorGroupA.setOnCheckedChangeListener(new ErrorCheckListener(mErrorGroupB));
		mErrorGroupB.setOnCheckedChangeListener(new ErrorCheckListener(mErrorGroupA));

		mAddNote = (Button) mRootView.findViewById(R.id.point_add_note);
		mAddNote.setOnClickListener(mAddNoteListener);

		mContinuePoint = (Button) mRootView.findViewById(R.id.point_continue_point);
		mContinuePoint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				continuePoint();
			}
		});
        mRootView.findViewById(R.id.point_unknown_point).setOnClickListener(mUnknownListener);
        mRootView.findViewById(R.id.point_unknown_serve).setOnClickListener(mUnknownListener);

		mNextPoint = (Button) mRootView.findViewById(R.id.point_next_point);
		mNextPoint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finishPoint();
			}
		});

		// Letchord
		mRootView.findViewById(R.id.point_letcord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPoint.addLetchord();
                continuePoint();
            }
        });

		mRootView.findViewById(R.id.point_more).setOnClickListener(mMoreListener);
		mEditPoint = (Button) mRootView.findViewById(R.id.edit_point_btn);
		mEditPoint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPointEditor.setBackgroundColor(Color.WHITE);
				mPointEditor.setTextColor(Color.BLACK);
				mPointEditor.setEnabled(true);
				mPointEditor.requestFocus();
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mPointEditor, InputMethodManager.SHOW_IMPLICIT);
                mErrors.setVisibility(View.INVISIBLE);
                mPointEndGroup.setVisibility(View.INVISIBLE);
                mServeEndGroup.setVisibility(View.INVISIBLE);
                mNextPoint.setVisibility(View.VISIBLE);
                editingPoint = true;
			}
		});
		mPointEditor = (EditText) mRootView.findViewById(R.id.point_edit_box);
		mPointEditor.setEnabled(false);
		mPointEditor.setSingleLine();

		mFootFault = (RadioButton) mRootView.findViewById(R.id.point_error_foot_fault);
        mScore = (TextView) mRootView.findViewById(R.id.point_score);

		setup();
		
		return mRootView;
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(180, 0, 0, 0)));
		return dialog;
	}
	
	private void finishPoint() {
        if (editingPoint)
            mPoint.setPoint(mPointEditor.getText().toString());
        if (mReloadMatch)
            pointEndListener.onReloadMatch();
		pointEndListener.onPointComplete(mPoint);
		PointEndDialog.this.dismiss();
	}

	private void continuePoint() {
        if (editingPoint)
            mPoint.setPoint(mPointEditor.getText().toString());
		mPoint.reopenPoint();
        if (mReloadMatch)
            pointEndListener.onReloadMatch();
		pointEndListener.onPointContinue(mPoint);
		PointEndDialog.this.dismiss();
	}

	private void setup() {
		if (mRootView == null)
			return;

		if (mPoint.shotCount() <= 1) {
			mServe = true;
			mServeEndGroup.setVisibility(View.VISIBLE);
			mPointEndGroup.setVisibility(View.GONE);
			mFootFault.setVisibility(View.VISIBLE);
			mServeEndGroup.clearCheck();
			final int[] serveRadios = { R.id.point_fault, R.id.point_ace, R.id.point_service_winner };
			for (int id : serveRadios) {
				if (mPoint.shotCount() == 0)
					mServeEndGroup.findViewById(id).setVisibility(View.INVISIBLE);
				else
					mServeEndGroup.findViewById(id).setVisibility(View.VISIBLE);
			}
		} else {
			mServe = false;
			mServeEndGroup.setVisibility(View.GONE);
			mPointEndGroup.setVisibility(View.VISIBLE);
			mFootFault.setVisibility(View.INVISIBLE);
			mPointEndGroup.clearCheck();
		}
		mErrors.setVisibility(View.INVISIBLE);
        mNextPoint.setVisibility(View.INVISIBLE);

		mPointEditor.setText(mPoint.toString());
		mPointEditor.invalidate();
        mScore.setText(mMatch.score().toString());
        mScore.setTextColor(Color.WHITE);
        mReloadMatch = false;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			pointEndListener = (OnPointEndListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
                    + " must implement PointEndDialog.OnPointEndListener");
		}
	}
	

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		
		pointEndListener.onPointContinue(null);
	}

	public void show(Match m, Point p, FragmentManager manager, String tag) {
        show(manager, tag);
		mPoint = new Point(p);
        mMatch = m;
        if (mMatch.server() == 1) {
            mPlayers[0] = m.player1 + " " + "(server)";
            mPlayers[1] = m.player2 + " " + "(returner)";
        } else {
            mPlayers[0] = m.player1 + " " + "(returner)";
            mPlayers[1] = m.player2 + " " + "(server)";
        }
	}
	
	public Point point() {
		return mPoint;
	}
}
