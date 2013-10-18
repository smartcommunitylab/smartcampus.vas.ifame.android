package eu.trentorise.smartcampus.ifame.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.ac.embedded.EmbeddedSCAccessProvider;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.ifame.R;
import eu.trentorise.smartcampus.ifame.adapter.PiattoKcalListAdapter;
import eu.trentorise.smartcampus.ifame.model.MenuDelGiorno;
import eu.trentorise.smartcampus.ifame.model.MenuDelMese;
import eu.trentorise.smartcampus.ifame.model.MenuDellaSettimana;
import eu.trentorise.smartcampus.ifame.model.Piatto;
import eu.trentorise.smartcampus.ifame.utils.ConnectionUtils;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class MenuDelMeseActivity extends SherlockFragmentActivity {
	/** Logging tag */
	private static final String TAG = "Menu_mese";

	private Spinner weekSpinner;
	private ListView listacibi_view;
	ProgressDialog progressDialog;
	String selectedDish;
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

						// String end_day = arr[0];

						// List<Piatto> p = new ArrayList<Piatto>();
						// ArrayAdapter<Piatto> adpter = new
						// PiattoKcalListAdapter(
						// MenuDelMeseActivity.this, p);

						setPiattiList(Integer.parseInt(start_day));
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}

				});

		// String title = getString(R.string.iDeciso_title_activity);

		if (ConnectionUtils.isConnectedToInternet(this)) {
			new MenuDelMeseConnector(MenuDelMeseActivity.this).execute();
		} else {
			ConnectionUtils.showToastNotConnectedToInternet(this);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);

	}

	/*
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
		// public String authToken = "aee58a92-d42d-42e8-b55e-12e4289586fc";

		private static final String CLIENT_ID = "9c7ccf0a-0937-4cc8-ae51-30d6646a4445";
		private static final String CLIENT_SECRET = "f6078203-1690-4a12-bf05-0aa1d1428875";
		private String token;

		public MenuDelMeseConnector(Context applicationContext) {
			context = applicationContext;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = ProgressDialog
					.show(context, "iFame", "Loading...");

			/*
			 * get the token
			 */
			SCAccessProvider accessProvider = new EmbeddedSCAccessProvider();
			try {
				token = accessProvider.readToken(MenuDelMeseActivity.this,
						CLIENT_ID, CLIENT_SECRET);

			} catch (AACException e) {
				Log.e(TAG, "Failed to get token: " + e.getMessage());
			}
		}

		@Override
		protected MenuDelMese doInBackground(Void... params) {
			mProtocolCarrier = new ProtocolCarrier(context, appToken);
			MessageRequest request = new MessageRequest(
					"http://smartcampuswebifame.app.smartcampuslab.it",
					"getmenudelmese");
			request.setMethod(Method.GET);
			MessageResponse response;
			try {
				response = mProtocolCarrier
						.invokeSync(request, appToken, token);

				if (response.getHttpStatus() == 200) {
					String body = response.getBody();
					MenuDelMese mdm = Utils.convertJSONToObject(body,
							MenuDelMese.class);
					return mdm;
				}
			} catch (ConnectionException e) {
				e.printStackTrace();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(MenuDelMese mdm) {
			super.onPostExecute(mdm);

			if (mdm == null) {
				ConnectionUtils
						.showToastErrorConnectingToWebService(MenuDelMeseActivity.this);
				finish();
			} else {

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
						MenuDelMeseActivity.this,
						android.R.layout.simple_spinner_dropdown_item,
						spinner_date_list);

				String[] months = {
						getString(R.string.iDeciso_monthly_menu_january),
						getString(R.string.iDeciso_monthly_menu_february),
						getString(R.string.iDeciso_monthly_menu_march),
						getString(R.string.iDeciso_monthly_menu_april),
						getString(R.string.iDeciso_monthly_menu_may),
						getString(R.string.iDeciso_monthly_menu_june),
						getString(R.string.iDeciso_monthly_menu_july),
						getString(R.string.iDeciso_monthly_menu_august),
						getString(R.string.iDeciso_monthly_menu_september),
						getString(R.string.iDeciso_monthly_menu_october),
						getString(R.string.iDeciso_monthly_menu_november),
						getString(R.string.iDeciso_monthly_menu_december) };

				setTitle(getString(R.string.iDeciso_monthly_menu_of) + " "
						+ months[c.get(Calendar.MONTH)]);

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
					spinner_adapter
							.add(getString(R.string.iDeciso_monthly_menu_from)
									+ " "
									+ start_day_string
									+ " "
									+ getString(R.string.iDeciso_monthly_menu_to)
									+ " " + end_day_string);

					// se il giorno corrente è tra il giorno iniziale e quelo
					// finale
					// della settimana sono nella settimana che mi interessa
					if (currentDay >= start_day && currentDay <= end_day) {
						position = iter;
					}
					iter++;
				}
				weekSpinner.setAdapter(spinner_adapter);
				weekSpinner.setSelection(position);
				weekSpinner.setVisibility(View.VISIBLE);
				// chiudo il loading...
				progressDialog.dismiss();
			}
		}
	}

	private void setPiattiList(int start_day) {
		// prendo la lista di menu della settimana
		List<MenuDellaSettimana> mds = menuDelMese.getMenuDellaSettimana();
		// creo la lista di piatti da mostrare
		ArrayList<Piatto> currentWeek = new ArrayList<Piatto>();
		// creo l'adapter per la lista di piatti

		ArrayAdapter<Piatto> adapter = new PiattoKcalListAdapter(
				MenuDelMeseActivity.this, currentWeek);

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
		listacibi_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View arg1,
					int position, long arg3) {
				selectedDish = ((Piatto) parent.getItemAtPosition(position))
						.getPiatto_nome();

				if (!selectedDish.matches("[0-9]+")) { // assure that the date
														// number values are not
														// included in the
														// search
					StartWebSearchAlertDialog dialog = new StartWebSearchAlertDialog();
					dialog.show(getSupportFragmentManager(), null);
				}
			}
		});
	}

	private class StartWebSearchAlertDialog extends SherlockDialogFragment {

		public Dialog onCreateDialog(Bundle savedInstanceState) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setMessage(
					getString(R.string.iDeciso_GoogleSearchAlertText) + " "
							+ selectedDish + "?")
					.setPositiveButton(
							getString(R.string.iDeciso_GoogleSearchAlertAccept),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									Intent intent = new Intent(
											Intent.ACTION_WEB_SEARCH);
									intent.putExtra(SearchManager.QUERY,
											selectedDish); // query contains
									// search string
									startActivity(intent);

								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();

		}

	}
}
