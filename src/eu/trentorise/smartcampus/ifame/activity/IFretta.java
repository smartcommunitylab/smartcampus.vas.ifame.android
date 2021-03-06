package eu.trentorise.smartcampus.ifame.activity;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import it.smartcampuslab.ifame.R;
import eu.trentorise.smartcampus.ifame.adapter.MensaSpinnerAdapter;
import eu.trentorise.smartcampus.ifame.model.Mensa;
import eu.trentorise.smartcampus.ifame.model.WebcamAspectRatioImageView;
import eu.trentorise.smartcampus.ifame.utils.IFameUtils;
import eu.trentorise.smartcampus.ifame.utils.MensaUtils;

public class IFretta extends SherlockActivity implements OnNavigationListener {

	private MenuItem refreshButton;
	private MensaSpinnerAdapter adapter;
	private int currentTabSelected;

	private ProgressBar progress;

	private WebcamAspectRatioImageView webcamImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_ifretta_details);

		webcamImage = (WebcamAspectRatioImageView) findViewById(R.id.imageViewId);
		progress = (ProgressBar) findViewById(R.id.progressBar1);

		adapter = new MensaSpinnerAdapter(IFretta.this);
		for (Mensa mensa : MensaUtils.getMensaList(IFretta.this)) {
			adapter.add(mensa);
		}
		// setup actionbar (supportActionBar is initialized in super.onCreate())
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(adapter, IFretta.this);

		// select current mensa
		int selected = adapter.getPosition(MensaUtils
				.getFavouriteMensa(IFretta.this));
		actionBar.setSelectedNavigationItem(selected);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// NOT YET IMPLEMENTED FAVOURITE MENSA FUNCTIONALITY
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_only_loading, menu);

		refreshButton = menu.findItem(R.id.action_refresh);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;

		case R.id.action_refresh:
			// a check if happens that favourite canteens are not set
			int index = getSupportActionBar().getSelectedNavigationIndex();
			if (index >= 0) {
				loadWebcamImage(adapter.getItem(index));
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadWebcamImage(Mensa mensa) {

		if (IFameUtils.isUserConnectedToInternet(IFretta.this)) {

			Date now = new Date();
			if (now.after(getStart()) && now.before(getEnd())) {
				// retrieve the online image
				new RetrieveWebcamImageTask().execute(mensa
						.getMensa_link_online());
			} else {
				// otherwise retrieve the offline image
				new RetrieveWebcamImageTask().execute(mensa
						.getMensa_link_offline());
			}
		} else {
			// show image image not avaiable
			Toast.makeText(IFretta.this,
					getString(R.string.errorInternetConnectionRequired),
					Toast.LENGTH_SHORT).show();
		}
	}

	private static Date getStart() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 11);
		c.set(Calendar.MINUTE, 55);
		return c.getTime();
	}

	private static Date getEnd() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 14);
		c.set(Calendar.MINUTE, 30);
		return c.getTime();
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// se sono connesso richiedo i giudizi
		if (IFameUtils.isUserConnectedToInternet(IFretta.this)) {
			currentTabSelected = itemPosition;
			loadWebcamImage(adapter.getItem(itemPosition));
		} else {
			// non sono connesso mostro il toast e torno alla tab precedente
			// controllo l'item per evitare la ricorsione di chiamate
			if (currentTabSelected != itemPosition) {

				getSupportActionBar().setSelectedNavigationItem(
						currentTabSelected);

				Toast.makeText(IFretta.this,
						getString(R.string.errorInternetConnectionRequired),
						Toast.LENGTH_SHORT).show();
			}
		}
		return false;
	}

	private class RetrieveWebcamImageTask extends
			AsyncTask<String, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			IFameUtils.setActionBarLoading(refreshButton);
			progress.setVisibility(View.VISIBLE);
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap bmap_image = null;

			try {

				InputStream in = new java.net.URL(urldisplay).openStream();
				bmap_image = BitmapFactory.decodeStream(in);

			} catch (Exception e) {
				Log.e(getClass().getName(), e.getMessage());
				return null;
			}
			return bmap_image;
		}

		@Override
		protected void onPostExecute(Bitmap result) {

			if (result != null) {
				webcamImage.setImageBitmap(result);
				webcamImage.setVisibility(View.VISIBLE);

			} else {
				Toast.makeText(IFretta.this,
						getString(R.string.errorLoadingWebcamImage),
						Toast.LENGTH_SHORT).show();
			}
			progress.setVisibility(View.GONE);
			IFameUtils.removeActionBarLoading(refreshButton);
		}
	}

}
