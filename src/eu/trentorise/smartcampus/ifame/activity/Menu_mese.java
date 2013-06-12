package eu.trentorise.smartcampus.ifame.activity;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.ifame.R;
import eu.trentorise.smartcampus.ifame.R.layout;
import eu.trentorise.smartcampus.ifame.model.MenuDelGiorno;
import eu.trentorise.smartcampus.ifame.model.MenuDelMese;
import eu.trentorise.smartcampus.ifame.model.MenuDellaSettimana;
import eu.trentorise.smartcampus.ifame.model.Piatto;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class Menu_mese extends Activity {

	private Spinner weekSpinner;
	private ListView listacibi_view;
	ProgressDialog pd;
	private MenuDelMese menuDelMese;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_menu_mese);

		listacibi_view = (ListView) findViewById(R.id.menu_of_the_day);

		// Aggiungo lo spinner
		weekSpinner = (Spinner) findViewById(R.id.spinner_settimana);
		weekSpinner
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> adapter,
							View view, int position, long id) {
						String string_date = (String) adapter
								.getItemAtPosition(position);

						String[] numbers = string_date.split("\\s");
						String[] arr = numbers[1].split("/", 2);
						String start_day = arr[0];

						arr = numbers[3].split("/", 2);
						String end_day = arr[0];

						List<Piatto> p = new ArrayList<Piatto>();
						ArrayAdapter<Piatto> adpter = new ListHeaderAdapter(
								Menu_mese.this, p);

						setPiattiList(Integer.parseInt(start_day));
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}

				});

		pd = new ProgressDialog(Menu_mese.this).show(Menu_mese.this, "iDeciso",
				"Loading...");

		// new MenuDellaSettimanaConnector(Menu_mese.this).execute();
		new MenuDelMeseConnector(Menu_mese.this).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_mese, menu);
		return true;
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * CONNECTOR MENU DEL MESE
	 */
	public class MenuDelMeseConnector extends
			AsyncTask<Void, Void, MenuDelMese> {

		private ProtocolCarrier mProtocolCarrier;
		public Context context;
		public String appToken = "test smartcampus";
		public String authToken = "aee58a92-d42d-42e8-b55e-12e4289586fc";

		public MenuDelMeseConnector(Context applicationContext) {
			context = applicationContext;
		}

		private MenuDelMese getMenuDelMese() {
			mProtocolCarrier = new ProtocolCarrier(context, appToken);
			MessageRequest request = new MessageRequest(
					"http://smartcampuswebifame.app.smartcampuslab.it",
					"getmenudelmese");
			request.setMethod(Method.GET);
			MessageResponse response;
			try {
				response = mProtocolCarrier.invokeSync(request, appToken,
						authToken);
				if (response.getHttpStatus() == 200) {
					String body = response.getBody();
					MenuDelMese mdm = Utils.convertJSONToObject(body,
							MenuDelMese.class);
					return mdm;
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
		protected MenuDelMese doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return getMenuDelMese();
		}

		@Override
		protected void onPostExecute(MenuDelMese mdm) {
			// TODO Auto-generated method stub
			super.onPostExecute(mdm);
			// setto il menu del mese ricevuto come variabile di classe
			menuDelMese = mdm;
			// cerco la settimana corrente e la mostro
			Calendar c = Calendar.getInstance();
			int currentDay = c.get(Calendar.DAY_OF_MONTH);

			// prendo la lista di menu della settimana
			ArrayList<MenuDellaSettimana> mds = (ArrayList<MenuDellaSettimana>) mdm
					.getMenuDellaSettimana();

			// creo la lista per lo spinner
			ArrayList<String> spinner_date_list = new ArrayList<String>();
			ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(
					Menu_mese.this,
					android.R.layout.simple_spinner_dropdown_item,
					spinner_date_list);

			String[] months = { "January", "February", "March", "April", "May",
					"June", "July", "August", "September", "October",
					"November", "December" };
			setTitle("Menu of " + months[c.get(Calendar.MONTH)]);

			// ciclo sulle settimane e prendo tutti i piatti della settimana
			// corrente
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

			int iter = 0;
			int position = 0;
			for (MenuDellaSettimana m : mds) {
				int start_day = m.getStart_day();
				int end_day = m.getEnd_day();
				c.set(Calendar.DATE, start_day);
				String start_day_string = dateFormat.format(c.getTime());
				c.set(Calendar.DATE, end_day);
				String end_day_string = dateFormat.format(c.getTime());
				// setto l'item dello spinner
				spinner_adapter.add("Da " + start_day_string + " a "
						+ end_day_string);

				// se il giorno corrente è tra il giorno iniziale e quelo finale
				// della settimana sono nella settimana che mi interessa
				if (currentDay >= start_day && currentDay <= end_day) {
					position = iter;
				}
				iter++;
			}
			weekSpinner.setAdapter(spinner_adapter);
			weekSpinner.setSelection(position);

			// chiudo il loading...
			pd.dismiss();
		}
	}

	private void setPiattiList(int start_day) {
		// prendo la lista di menu della settimana
		List<MenuDellaSettimana> mds = menuDelMese.getMenuDellaSettimana();
		// creo la lista di piatti da mostrare
		ArrayList<Piatto> currentWeek = new ArrayList<Piatto>();
		// creo l'adapter per la lista di piatti

		ArrayAdapter<Piatto> adapter = new ListHeaderAdapter(
				Menu_mese.this, currentWeek);

		for (MenuDellaSettimana m : mds) {
			if (start_day == m.getStart_day()) {
				// sono nella settimana interessata ciclo sui menu del
				// giorno
				ArrayList<MenuDelGiorno> mdglist = (ArrayList<MenuDelGiorno>) m
						.getMenuDelGiorno();
				for (MenuDelGiorno mdg : mdglist) {
					// ATTENZIONE AL MAGHEGGIO
					Piatto piattoSentinella = new Piatto();
					// setto come nome del piatto il numero del giorno
					piattoSentinella.setPiatto_nome(mdg.getDay() + "");
					// del menu del giorno che sto iterando perche mi serve
					// come sentinella nell'adapter
					adapter.add(piattoSentinella);
					// aggiungo tutti gli altri piatti
					adapter.addAll(mdg.getPiattiDelGiorno());
				}
			}
		}

		listacibi_view.setAdapter(adapter);
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * LISTADAPTER FOR THE LIST OF PIATTOKCAL
	 */
	private class ListHeaderAdapter extends ArrayAdapter<Piatto> {

		public ListHeaderAdapter(Context context, List<Piatto> list) {
			super(Menu_mese.this, android.R.layout.simple_list_item_1, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			Piatto p = getItem(position);

			if (p.getPiatto_nome().matches("[0-9]+")) {
				// ho un piatto sentinella setto il testo come data
				convertView = inflater.inflate(
						layout.layout_row_header_menu_adapter, null);

				TextView dayHeader = (TextView) convertView
						.findViewById(R.id.menu_day_header_adapter);
				TextView kcalHeader = (TextView) convertView
						.findViewById(R.id.menu__kcal_header_adapter);

				int day = Integer.parseInt(p.getPiatto_nome());

				Calendar c = Calendar.getInstance();
				c.set(Calendar.DATE, day);
				SimpleDateFormat s = new SimpleDateFormat("EEEEE dd MMMM yyyy");
				String date_formatted = s.format(c.getTime());

				dayHeader.setText(date_formatted);
				// dayHeader.setTextSize((float) 18);
				// name.setBackgroundColor(Color.RED);
				dayHeader.setTextColor(Color.WHITE);
				// perche nel layout c'è che esce 'kcal'
				kcalHeader.setTextColor(Color.WHITE);
				// kcalHeader.setText("KCal");

			} else {
				// ho un piatto vero setto i campi coi valori corrispondenti
				convertView = inflater.inflate(layout.layout_row_menu_adapter,
						null);

				TextView name = (TextView) convertView
						.findViewById(R.id.menu_name_adapter);
				TextView kcal = (TextView) convertView
						.findViewById(R.id.menu_kcal_adapter);

				name.setText(p.getPiatto_nome());
				kcal.setText(p.getPiatto_kcal());
			}
			return convertView;
		}
	}

}
