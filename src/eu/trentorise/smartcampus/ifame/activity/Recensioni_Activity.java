package eu.trentorise.smartcampus.ifame.activity;

import java.text.SimpleDateFormat;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.ifame.R;
import eu.trentorise.smartcampus.ifame.model.GiudizioDataToPost;
import eu.trentorise.smartcampus.ifame.model.GiudizioNew;
import eu.trentorise.smartcampus.ifame.model.Likes;
import eu.trentorise.smartcampus.ifame.model.Mensa;
import eu.trentorise.smartcampus.ifame.model.Piatto;
import eu.trentorise.smartcampus.ifame.utils.ConnectionUtils;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

public class Recensioni_Activity extends SherlockActivity {

	List<Mensa> listaMense = null;
	MenuItem menuItem = null;
	ReviewAdapter adapter = null;
	Piatto piatto;
	String user_id;
	Mensa mensa;
	TextView giudizio_espresso_da;
	TextView giudizio_medio_txt;
	TextView no_data_to_display;
	// PostLikeConnector postLike;
	GiudizioDataToPost giudizioDataToPost = null;

	ListView giudiziListview;

	// String add = "http://192.168.33.106:8080/web-ifame";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_igradito_pagina_recensioni);
		Bundle extras = getIntent().getExtras();
		giudiziListview = (ListView) findViewById(R.id.recensioni_list);
		giudizio_espresso_da = (TextView) findViewById(R.id.espresso_da);
		giudizio_medio_txt = (TextView) findViewById(R.id.giudizio);
		no_data_to_display = (TextView) findViewById(R.id.giudizio_no_data_to_display);

		// whattttt????
		if (extras == null) {
			return;
		}
		// Get intents from the igradito activity
		piatto = (Piatto) extras.get("nome_piatto");
		setTitle(piatto.getPiatto_nome());

		user_id = (String) extras.get("user_id");
		mensa = (Mensa) extras.get("igradito_spinner_mense");

		new ProgressDialog(Recensioni_Activity.this);
		// get list of comments
		if (ConnectionUtils.isOnline(this)) {
			new GetGiudizioConnector(Recensioni_Activity.this).execute(
					mensa.getMensa_id(), piatto.getPiatto_id());
		} else {
			Toast.makeText(this, "Controlla la tua connessione ad internet!",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * DISPLAY THE CUSTOMIZED DIALOG
	 * 
	 */
	private void showCustomizedDialog() {
		// hide the title bar
		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		FragmentManager fm = getFragmentManager();
		CustomDialog cd = new CustomDialog();
		cd.show(fm, "fragment");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.igradito_recensioni_menu_item, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			menuItem = item;
			menuItem.setActionView(R.layout.actionbar_progressbar_circle);
			menuItem.expandActionView();
			if (adapter != null) {
				adapter.clear();
				adapter.notifyDataSetChanged();
			}
			if (ConnectionUtils.isOnline(this)) {
				new GetGiudizioConnector(Recensioni_Activity.this).execute(
						mensa.getMensa_id(), piatto.getPiatto_id());
			} else {
				Toast.makeText(this,
						"Controlla la tua connessione ad internet!",
						Toast.LENGTH_LONG).show();
				finish();
			}
			break;
		case android.R.id.home:
			onBackPressed();
			break;
		case R.id.action_add_comments:

			showCustomizedDialog();

			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * CLASS TO CREATE A CUSTOM DIALOG BOX FOR THE REVIEWS
	 * 
	 */

	public class CustomDialog extends DialogFragment {

		EditText cd_editText;
		String commento;
		SeekBar cd_seekbar;
		int progressChanged = 0;

		public CustomDialog() {
			// NO ARG CONSTRUCTOR---REQUIRED
		}

		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.igradito_custom_dialogbox,
					container);

			// set the title of the dialog box
			getDialog().setTitle("La tua recensione... ");

			cd_editText = (EditText) view
					.findViewById(R.id.custom_dialog_etext);

			// Get the seekbar asscociated with this view
			cd_seekbar = (SeekBar) view.findViewById(R.id.recensioni_seekbar);
			progressChanged = cd_seekbar.getProgress();

			// ADD LISTENER TO THE SEEKBAR
			cd_seekbar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							Toast.makeText(getApplicationContext(),
									progressChanged + "", Toast.LENGTH_LONG)
									.show();
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							progressChanged = progress;
							// seekBar.incrementProgressBy(1);

						}
					});

			// ADD LISTENER TO THE ANNULA BUTTON
			view.findViewById(R.id.annulla_button).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							dismiss();
						}
					});

			// ADD LISTENER TO THE OK BUTTON
			view.findViewById(R.id.OK_button).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							giudizioDataToPost = new GiudizioDataToPost();
							commento = cd_editText.getText().toString();
							giudizioDataToPost.commento = commento;
							giudizioDataToPost.userId = Long.parseLong(user_id);
							giudizioDataToPost.voto = (float) progressChanged;

							if (adapter != null) {
								adapter.clear();
								adapter.notifyDataSetChanged();
							}

							new PostGiudizioConnector(getActivity(),
									giudizioDataToPost).execute(
									mensa.getMensa_id(), piatto.getPiatto_id());
							dismiss();
						}
					});

			return view;
		}
	}

	/**
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
	 * ADAPTER FOR DISPLAYING REVIEWS PROBLEM : LIKE IS NOT UPDATED
	 * INSTANTANEOUSLY
	 * 
	 */
	public class ReviewAdapter extends ArrayAdapter<GiudizioNew> {

		SimpleDateFormat dateformat = null;

		public ReviewAdapter(Activity activity, int layout_id,
				List<GiudizioNew> reviews) {
			super(activity, layout_id, reviews);
			dateformat = new SimpleDateFormat("HH:mm  dd/MM/yy");
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;

			final DataHandler handler;
			final GiudizioNew giudizio = getItem(position);
			if (v == null) {

				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.list_igradito_recensioni, null);

				handler = new DataHandler();

				handler.username = (TextView) v
						.findViewById(R.id.iGradito_recensioni_nome_utente);
				handler.review_date = (TextView) v
						.findViewById(R.id.iGradito_recensioni_data_inserimento);
				handler.review_content = (TextView) v
						.findViewById(R.id.iGradito_recensioni_review_content);
				handler.like_count_view = (TextView) v
						.findViewById(R.id.like_count);
				handler.dislike_count_view = (TextView) v
						.findViewById(R.id.dislike_count);
				handler.like_button = (ImageButton) v
						.findViewById(R.id.like_button);
				handler.dislike_button = (ImageButton) v
						.findViewById(R.id.dislike_button);

				handler.voto = (TextView) v
						.findViewById(R.id.iGradito_recensioni_voto);

				List<Likes> list_likes = giudizio.getLikes();
				Integer likes_count = 0;
				Integer dislikes_count = 0;

				for (Likes l : list_likes) {
					if (l.getIs_like()) {
						likes_count++;

						if (l.getUser_id() == Long.parseLong(user_id)) {
							handler.non_ha_ancora_fatto_like_o_dislike = false;
							handler.is_like = true;
							handler.like_button
									.setImageResource(R.drawable.icon_like_up);
						}

					} else {
						dislikes_count++;

						if (l.getUser_id() == Long.parseLong(user_id)) {
							handler.non_ha_ancora_fatto_like_o_dislike = false;
							handler.is_like = false;
							handler.dislike_button
									.setImageResource(R.drawable.icon_like_down);
						}
					}
				}

				handler.like_count = likes_count;
				handler.dislike_count = dislikes_count;

				v.setTag(handler);
			} else {
				handler = (DataHandler) v.getTag();
			}

			handler.username.setText(giudizio.getUser_id().toString());
			handler.review_date.setText(dateformat.format(
					giudizio.getUltimo_aggiornamento()).toString());
			handler.review_content.setText(giudizio.getCommento());
			handler.like_count_view.setText(handler.like_count + "");
			handler.dislike_count_view.setText(handler.dislike_count + "");
			handler.voto.setText(giudizio.getVoto() + "");

			// ADD LISTENER TO THE LIKE BUTTON
			handler.like_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Likes likes = new Likes();

					likes.setGiudizio_id(giudizio.getGiudizio_id());
					likes.setUser_id(Long.parseLong(user_id));

					if (handler.non_ha_ancora_fatto_like_o_dislike) {
						// caso base faccio like
						handler.non_ha_ancora_fatto_like_o_dislike = false;
						handler.is_like = true;

						likes.setIs_like(true);
						handler.like_count++;
						handler.like_count_view
								.setText(handler.like_count + "");

						handler.like_button
								.setImageResource(R.drawable.icon_like_up);

						new PostLikeConnector(Recensioni_Activity.this)
								.execute(likes);
					} else {
						// ho gia messo un like o dislike
						if (!handler.is_like) {
							// sto cambiando idea da dislike a like
							handler.is_like = true;

							likes.setIs_like(true);

							// aumento i dislike
							handler.dislike_count--;
							handler.dislike_count_view
									.setText(handler.dislike_count + "");

							// riduco i like
							handler.like_count++;
							handler.like_count_view.setText(handler.like_count
									+ "");

							handler.like_button
									.setImageResource(R.drawable.icon_like_up);

							handler.dislike_button
									.setImageResource(R.drawable.icon_like_down_outline);

							new PostLikeConnector(Recensioni_Activity.this)
									.execute(likes);

						} else {
							// ho fatto like ancora tolgo il like
							handler.non_ha_ancora_fatto_like_o_dislike = true;
							// lo metto null ma dovrebbe fottere cosa sia
							handler.is_like = null;

							likes.setIs_like(true);

							handler.like_count--;
							handler.like_count_view.setText(handler.like_count
									+ "");

							handler.like_button
									.setImageResource(R.drawable.icon_like_up_outline);

							new DeleteLikeConnector(Recensioni_Activity.this)
									.execute(likes);
						}
					}
				}
			});

			// ADD LISTENER TO THE LIKE BUTTON
			handler.dislike_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Likes likes = new Likes();

					likes.setGiudizio_id(giudizio.getGiudizio_id());
					likes.setUser_id(Long.parseLong(user_id));

					if (handler.non_ha_ancora_fatto_like_o_dislike) {
						// non ho ancora fatto like o dislike e clicco su
						// dislike
						handler.non_ha_ancora_fatto_like_o_dislike = false;
						handler.is_like = false;

						likes.setIs_like(false);

						handler.dislike_count++;
						handler.dislike_count_view
								.setText(handler.dislike_count + "");

						handler.dislike_button
								.setImageResource(R.drawable.icon_like_down);

						new PostLikeConnector(Recensioni_Activity.this)
								.execute(likes);

					} else {
						// ho gia messo un like o dislike
						if (handler.is_like) {
							// sto cambiando idea da like a dislike
							handler.is_like = false;

							likes.setIs_like(false);

							// aumento i dislike
							handler.dislike_count++;
							handler.dislike_count_view
									.setText(handler.dislike_count + "");

							// riduco i like
							handler.like_count--;
							handler.like_count_view.setText(handler.like_count
									+ "");

							handler.dislike_button
									.setImageResource(R.drawable.icon_like_down);

							handler.like_button
									.setImageResource(R.drawable.icon_like_up_outline);

							new PostLikeConnector(Recensioni_Activity.this)
									.execute(likes);

						} else {
							// ho fatto dislike ancora tolgo il dislike
							handler.non_ha_ancora_fatto_like_o_dislike = true;
							// lo metto null ma dovrebbe fottere cosa sia
							handler.is_like = null;

							likes.setIs_like(false);

							handler.dislike_count--;
							handler.dislike_count_view
									.setText(handler.dislike_count + "");

							handler.dislike_button
									.setImageResource(R.drawable.icon_like_down_outline);

							new DeleteLikeConnector(Recensioni_Activity.this)
									.execute(likes);
						}
					}
				}
			});
			return v;
		}
	}

	private static class DataHandler {

		TextView voto;
		TextView username;
		TextView review_date;
		TextView review_content;
		TextView like_count_view;
		TextView dislike_count_view;
		ImageButton like_button;
		ImageButton dislike_button;
		Integer like_count;
		Integer dislike_count;
		Boolean non_ha_ancora_fatto_like_o_dislike;
		Boolean is_like;

		public DataHandler() {
			Integer like_count = 0;
			Integer dislike_count = 0;
			non_ha_ancora_fatto_like_o_dislike = true;
		}
	}

	/**
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
	 * CONNECTOR TO GET THE LIST OF COMMENTS
	 * 
	 */

	private class GetGiudizioConnector extends
			AsyncTask<Long, Void, List<GiudizioNew>> {
		private ProtocolCarrier mProtocolCarrier;
		private ProgressDialog progressDialog;
		private Context context;
		private String appToken = "test smartcampus";
		private String authToken = "aee58a92-d42d-42e8-b55e-12e4289586fc";

		public GetGiudizioConnector(Context applicationContext) {
			context = applicationContext;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = ProgressDialog.show(context, "iGradito",
					"Loading...");
		}

		@Override
		protected List<GiudizioNew> doInBackground(Long... params) {

			mProtocolCarrier = new ProtocolCarrier(context, appToken);

			MessageRequest request = new MessageRequest(
					"http://smartcampuswebifame.app.smartcampuslab.it",
					"mensa/" + params[0] + "/piatto/" + params[1] + "/giudizio");

			request.setMethod(Method.GET);

			MessageResponse response;
			try {
				response = mProtocolCarrier.invokeSync(request, appToken,
						authToken);

				if (response.getHttpStatus() == 200) {
					String body = response.getBody();
					List<GiudizioNew> list = Utils.convertJSONToObjects(body,
							GiudizioNew.class);
					return list;
				} else {
					return null;
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
		protected void onPostExecute(List<GiudizioNew> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == null) {
				progressDialog.dismiss();
				Toast.makeText(context, "Ooooops! Qualcosa è andato storto!",
						Toast.LENGTH_LONG).show();
			} else {
				createGiudiziList(result);
				progressDialog.dismiss();
			}
		}
	}

	/**
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
	 * CONNECTOR TO POST COMMENTS
	 * 
	 */

	private class PostGiudizioConnector extends
			AsyncTask<Long, Void, List<GiudizioNew>> {
		private ProtocolCarrier mProtocolCarrier;
		private Context context;
		private String appToken = "test smartcampus";
		private String authToken = "aee58a92-d42d-42e8-b55e-12e4289586fc";

		private ProgressDialog progressDialog;
		private GiudizioDataToPost data = null;

		public PostGiudizioConnector(Context applicationContext,
				GiudizioDataToPost data) {
			context = applicationContext;
			this.data = data;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = ProgressDialog.show(context, "iGradito",
					"Loading...");
		}

		@Override
		protected List<GiudizioNew> doInBackground(Long... params) {
			// TODO Auto-generated method stu

			mProtocolCarrier = new ProtocolCarrier(context, appToken);

			MessageRequest request = new MessageRequest(
					"http://smartcampuswebifame.app.smartcampuslab.it",
					"mensa/" + params[0] + "/piatto/" + params[1]
							+ "/giudizio/add");

			// MessageRequest request = new MessageRequest(add, "mensa/"
			// + params[0] + "/piatto/" + params[1] + "/giudizio/add");

			request.setMethod(Method.POST);

			request.setBody(Utils.convertToJSON(data));

			MessageResponse response;
			try {
				response = mProtocolCarrier.invokeSync(request, appToken,
						authToken);

				if (response.getHttpStatus() == 200) {
					String body = response.getBody();
					List<GiudizioNew> list = Utils.convertJSONToObjects(body,
							GiudizioNew.class);
					return list;
				} else {
					return null;
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
		protected void onPostExecute(List<GiudizioNew> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result == null) {
				Toast.makeText(context, "Ooooops! Qualcosa è andato storto!",
						Toast.LENGTH_LONG).show();
			} else {
				createGiudiziList(result);
				progressDialog.dismiss();
				Toast.makeText(context, "Recensione aggiunta correttamente",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/*
	 * 
	 * METHOD CALLED AFTER POST OR GET GIUDIZI FROM THE EWB
	 */
	private void createGiudiziList(List<GiudizioNew> reviews) {

		int review_size = reviews.size();
		float avg = 0;
		if (review_size > 0) {
			for (GiudizioNew g : reviews) {
				// calcolo la media
				avg += g.getVoto();
			}
			avg = avg / (float) review_size;

			if (adapter == null) {
				adapter = new ReviewAdapter(Recensioni_Activity.this,
						android.R.layout.simple_list_item_1, reviews);
				giudiziListview.setAdapter(adapter);
			} else {
				adapter.clear();
				adapter.addAll(reviews);
			}

			giudizio_espresso_da.setText(review_size + " utent"
					+ (review_size == 1 ? "e" : "i"));
			giudizio_medio_txt.setText(avg + "");
			adapter.notifyDataSetChanged();
			giudiziListview.setVisibility(View.VISIBLE);
			no_data_to_display.setVisibility(View.GONE);
		} else {

			giudiziListview.setVisibility(View.GONE);
			no_data_to_display.setVisibility(View.VISIBLE);
		}
		// PER IL CARICAMENTO NELL ACTION BAR
		if (menuItem != null) {
			menuItem.collapseActionView();
			menuItem.setActionView(null);
		}
	}

	/**
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
	 * LIKE CONNECTORS
	 * 
	 */

	private class PostLikeConnector extends AsyncTask<Likes, Void, Boolean> {

		private ProtocolCarrier mProtocolCarrier;
		private Context context;
		private String appToken = "test smartcampus";
		private String authToken = "aee58a92-d42d-42e8-b55e-12e4289586fc";

		public PostLikeConnector(Context applicationContext) {
			context = applicationContext;
		}

		@Override
		protected Boolean doInBackground(Likes... like) {

			mProtocolCarrier = new ProtocolCarrier(context, appToken);

			MessageRequest request = new MessageRequest(
					"http://smartcampuswebifame.app.smartcampuslab.it",
					"giudizio/" + like[0].getGiudizio_id() + "/like");

			request.setMethod(Method.POST);

			request.setBody(Utils.convertToJSON(like[0]));

			MessageResponse response;
			try {
				response = mProtocolCarrier.invokeSync(request, appToken,
						authToken);

				if (response.getHttpStatus() == 200) {
					return true;
				} else {
					return false;
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
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result) {
			} else {
				Toast.makeText(context, "Oooops! Not Liked", Toast.LENGTH_SHORT)
						.show();
			}

		}
	}

	private class DeleteLikeConnector extends AsyncTask<Likes, Void, Boolean> {

		private ProtocolCarrier mProtocolCarrier;
		private Context context;
		private String appToken = "test smartcampus";
		private String authToken = "aee58a92-d42d-42e8-b55e-12e4289586fc";

		public DeleteLikeConnector(Context applicationContext) {
			context = applicationContext;
		}

		@Override
		protected Boolean doInBackground(Likes... like) {

			mProtocolCarrier = new ProtocolCarrier(context, appToken);
			// giudizio/43/user/67/like/delete
			MessageRequest request = new MessageRequest(
					"http://smartcampuswebifame.app.smartcampuslab.it",
					"giudizio/" + like[0].getGiudizio_id() + "/like/delete");

			request.setMethod(Method.POST);

			request.setBody(Utils.convertToJSON(like[0]));

			MessageResponse response;
			try {
				response = mProtocolCarrier.invokeSync(request, appToken,
						authToken);

				if (response.getHttpStatus() == 200) {
					return true;
				} else {
					return false;
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
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result) {
			} else {
				Toast.makeText(context, "Oooops! Like not deleted",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}