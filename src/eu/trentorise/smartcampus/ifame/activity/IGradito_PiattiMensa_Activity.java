package eu.trentorise.smartcampus.ifame.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.ifame.R;
import eu.trentorise.smartcampus.ifame.R.layout;
import eu.trentorise.smartcampus.ifame.model.Mensa;
import eu.trentorise.smartcampus.ifame.model.Piatto;
import eu.trentorise.smartcampus.ifame.model.Piatto_Mensa;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class IGradito_PiattiMensa_Activity extends Activity {

	ProgressDialog pd;
	private Spinner portataSpinner;
	private Mensa mensa; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_igradito_piattimensa);

		// Aggiungo lo spinner
		portataSpinner = (Spinner) findViewById(R.id.spinner_portata);
		portataSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> adapter,
							View view, int position, long id) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}

				});
		
		Bundle extras = getIntent().getExtras();

		// if there are no available intents return
		if (extras == null) {
			return;
		}

		// Get mensa intent from activity:ifretta
		mensa = (Mensa) extras.get("mensa");
		setTitle(mensa.getMensa_nome());

		pd = new ProgressDialog(IGradito_PiattiMensa_Activity.this).show(
				IGradito_PiattiMensa_Activity.this, "IGradito", "Loading...");

		new IGraditoConnector(IGradito_PiattiMensa_Activity.this).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.igradito, menu);

		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu
				.findItem(R.id.igradito_search).getActionView();
		// Assumes current activity is the searchable activity
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false); // Do not iconify the widget;
													// expand it by default

		return true;

	}

	/*
	 * 
	 * 
	 * 
	 * ADAPTER PER IGRADITO
	 */

	private class IGraditoAdapter extends ArrayAdapter<Piatto_Mensa> {

		public IGraditoAdapter(Context context, int textViewResourceId,
				List<Piatto_Mensa> list) {
			super(IGradito_PiattiMensa_Activity.this, textViewResourceId, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			Piatto_Mensa p = getItem(position);

			convertView = inflater.inflate(layout.igradito_layout_row_adapter,
					null);

			TextView piatto_name = (TextView) convertView
					.findViewById(R.id.piatto_name_adapter);
			TextView piatto_avg = (TextView) convertView
					.findViewById(R.id.piatto_avgvote_adapter);

			piatto_name.setText(p.getPiatto().getPiatto_nome());
			piatto_avg.setText(p.getVoto_medio().toString());

			return convertView;
		}
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * CONNECTOR TO GETALLPIATTI WEB SERVICE
	 */

	private class IGraditoConnector extends AsyncTask<Void, Void, List<Piatto_Mensa>> {

		private ProtocolCarrier mProtocolCarrier;
		public Context context;
		public String appToken = "test smartcampus";
		public String authToken = "aee58a92-d42d-42e8-b55e-12e4289586fc";

		public IGraditoConnector(Context applicationContext) {
			context = applicationContext;
		}

		private List<Piatto_Mensa> getPiattiList() {

			mProtocolCarrier = new ProtocolCarrier(context, appToken);

			MessageRequest request = new MessageRequest(
					"http://smartcampuswebifame.app.smartcampuslab.it",
					"iGradito/" + mensa.getMensa_id());
			request.setMethod(Method.GET);

			MessageResponse response;
			try {
				response = mProtocolCarrier.invokeSync(request, appToken,
						authToken);

				if (response.getHttpStatus() == 200) {

					String body = response.getBody();

					List<Piatto_Mensa> lista_piatti = Utils.convertJSONToObjects(
							body, Piatto_Mensa.class);

					return lista_piatti;

				} else {

				}
			} catch (ConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;

		}

		@Override
		protected List<Piatto_Mensa> doInBackground(Void... params) {
			return getPiattiList();
		}

		@Override
		protected void onPostExecute(List<Piatto_Mensa> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			System.out.println("Sono nel onpostexecute");
			createPiatti(result);

			pd.dismiss();
		}

	}

	private void createPiatti(List<Piatto_Mensa> lista_piatti) {

		ListView list_view = (ListView) findViewById(R.id.list_view_igradito);


		if (lista_piatti.size() == 0) {
			System.out.println("è null");
		}

		IGraditoAdapter adapter = new IGraditoAdapter(
				IGradito_PiattiMensa_Activity.this,
				android.R.layout.simple_list_item_1, lista_piatti);
		
		list_view.setAdapter(adapter);

		list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long id) {

				Piatto piatto = (Piatto) adapter.getItemAtPosition(position);
				Intent i = new Intent(IGradito_PiattiMensa_Activity.this,
						Recensioni_Activity.class);
				i.putExtra("nome_piatto", piatto.getPiatto_nome());
				startActivity(i);
			}
		});
	}

}
