package eu.trentorise.smartcampus.ifame.tabs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import eu.trentorise.smartcampus.ifame.R;
import eu.trentorise.smartcampus.ifame.activity.Fai_il_tuo_menu;

public class TipologiaInteroFragment extends SherlockFragment {

	ViewGroup theContainer;
	boolean isC1Avail;
	boolean isC2Avail;
	boolean isDessertAvail;
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		theContainer = container;
		return inflater.inflate(R.layout.layout_tipologia_intero_fr, container, false);
	}

	
	
	@Override
	public void onResume() {
		
		Intent i = getSherlockActivity().getIntent();
		boolean isCalled = i.getBooleanExtra(Fai_il_tuo_menu.HAS_CALLED_TIPOLOGIE, false);
		String selected_menu = i.getStringExtra(Fai_il_tuo_menu.SELECTED_MENU);
		Toast.makeText(getSherlockActivity().getApplicationContext(), "Menu : "+selected_menu + ", isCalled = "+isCalled, Toast.LENGTH_LONG).show();
		

		
		
		TextView bigLabel = (TextView)theContainer.findViewById(R.id.tipologia_intero_biglabel);
		bigLabel.setText("- "+getString(R.string.iDeciso_compose_menu_checkbox_first)+", "+getString(R.string.iDeciso_compose_menu_checkbox_second));
		bigLabel.setTypeface(null, Typeface.BOLD);
		
		TextView contorni = (TextView)theContainer.findViewById(R.id.tipologia_intero_2contorni);
		contorni.setText("+ "+getString(R.string.iDeciso_2contorni));
		
		TextView dessert = (TextView)theContainer.findViewById(R.id.tipologia_intero_dessert);
		dessert.setText("+ "+getString(R.string.iDeciso_compose_menu_checkbox_dessert));

		
		TextView pane = (TextView)theContainer.findViewById(R.id.tipologia_intero_pane);
		pane.setText("+ "+getString(R.string.iDeciso_pane));
		
		if (isCalled /*&&  selected_menu.equals("Intero")*/ ){
			isC1Avail = i.getBooleanExtra(Fai_il_tuo_menu.CONTORNO_1_AVAILABLE, false);
			isC2Avail = i.getBooleanExtra(Fai_il_tuo_menu.CONTORNO_2_AVAILABLE, false);
			isDessertAvail = i.getBooleanExtra(Fai_il_tuo_menu.DESSERT_AVAILABLE, false);
		
			if (isC1Avail || isC2Avail){
				contorni.setTextColor(Color.parseColor("#08D126"));
			}
			if (isDessertAvail){
				dessert.setTextColor(Color.parseColor("#08D126"));
			}
			
			pane.setTextColor(Color.parseColor("#08D126"));
		}
		
		super.onResume();
		
		
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

}