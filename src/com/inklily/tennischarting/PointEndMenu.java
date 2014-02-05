package com.inklily.tennischarting;
import java.util.HashMap;
import java.util.Map;

import com.example.tennischarting.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;


public class PointEndMenu extends RelativeLayout {
	private RadioGroup serveEndGroup;
	private RadioGroup pointEndGroup;
	private RadioGroup errorGroup;
	private Button addNote;
	private Button continuePoint;
	private Button nextPoint;
	private Button retire;
	private Button editPoint;
	private EditText pointEditor;
	private RadioButton footFault;
	
	private static Map<Integer, Point.PointOutcome> pointOutcomeMap = new HashMap<Integer, Point.PointOutcome>();
	{
		pointOutcomeMap.put(R.id.point_ace, Point.PointOutcome.ACE);
		pointOutcomeMap.put(R.id.point_winner, Point.PointOutcome.WINNER);
		pointOutcomeMap.put(R.id.point_error_deep, Point.PointOutcome.DEEP);
		pointOutcomeMap.put(R.id.point_error_net, Point.PointOutcome.NET);
		pointOutcomeMap.put(R.id.point_error_shank, Point.PointOutcome.SHANK);
		pointOutcomeMap.put(R.id.point_error_unknown, Point.PointOutcome.UNKNOWN);
		pointOutcomeMap.put(R.id.point_service_winner, Point.PointOutcome.UNRETURNABLE);
		pointOutcomeMap.put(R.id.point_error_wide, Point.PointOutcome.WIDE);
		pointOutcomeMap.put(R.id.point_error_wide, Point.PointOutcome.WIDE_DEEP);
		pointOutcomeMap.put(R.id.point_error_foot_fault, Point.PointOutcome.FOOT_FAULT);
	}

	public PointEndMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		setVisibility(View.GONE);
	}
	
	public void hide() {
		setVisibility(View.GONE);
	}
	
	public void setup() {
		serveEndGroup = (RadioGroup) findViewById(R.id.serve_end_group);
		serveEndGroup.setOnCheckedChangeListener(new OnCheckedChangeListener () {
			@Override
			public void onCheckedChanged(RadioGroup group, int id) {
				if (id == R.id.point_fault) {
					errorGroup.setVisibility(View.VISIBLE);
				} else {
					errorGroup.setVisibility(View.GONE);
				}
				
			}
			
		});
		
		pointEndGroup = (RadioGroup) findViewById(R.id.point_end_group);
		pointEndGroup.setOnCheckedChangeListener(new OnCheckedChangeListener () {
			@Override
			public void onCheckedChanged(RadioGroup group, int id) {
				if (id == R.id.point_unforced_error || id == R.id.point_forced_error) {
					errorGroup.setVisibility(View.VISIBLE);
				} else {
					errorGroup.setVisibility(View.GONE);
				}
				
			}
		});
		
		errorGroup = (RadioGroup) findViewById(R.id.point_error_group);
		addNote = (Button) findViewById(R.id.point_add_note);
		continuePoint = (Button) findViewById(R.id.point_continue_point);
		nextPoint = (Button) findViewById(R.id.point_next_point);
		retire = (Button) findViewById(R.id.point_retire);
		editPoint = (Button) findViewById(R.id.point_edit_point);
		pointEditor = (EditText) findViewById(R.id.point_edit_box);
		footFault = (RadioButton) findViewById(R.id.point_error_foot_fault);
	}
	
	public void show(boolean serveMode) {
		if (serveEndGroup == null)
			setup();
		
		setVisibility(View.VISIBLE);
		errorGroup.setVisibility(View.GONE);
		if (serveMode) {
			serveEndGroup.setVisibility(View.VISIBLE);
			pointEndGroup.setVisibility(View.GONE);
			footFault.setVisibility(View.VISIBLE);
			serveEndGroup.clearCheck();
		} else {
			serveEndGroup.setVisibility(View.GONE);
			pointEndGroup.setVisibility(View.VISIBLE);
			footFault.setVisibility(View.INVISIBLE);
			pointEndGroup.clearCheck();
		}
	}
}
