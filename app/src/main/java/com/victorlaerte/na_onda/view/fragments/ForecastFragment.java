package com.victorlaerte.na_onda.view.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TableRow;
import android.widget.TextView;

import com.victorlaerte.na_onda.R;
import com.victorlaerte.na_onda.model.City;
import com.victorlaerte.na_onda.model.CompleteForecast;
import com.victorlaerte.na_onda.model.DayForecast;
import com.victorlaerte.na_onda.model.Forecast;
import com.victorlaerte.na_onda.model.impl.CustomPagerAdapter;
import com.victorlaerte.na_onda.util.AndroidUtil;
import com.victorlaerte.na_onda.util.CharPool;
import com.victorlaerte.na_onda.util.Constants;
import com.victorlaerte.na_onda.util.ContentTypeUtil;
import com.victorlaerte.na_onda.util.Validator;
import com.victorlaerte.na_onda.view.activities.MainViewActivity;

public class ForecastFragment extends Fragment {

	private static final String LOG_TAG = ForecastFragment.class.getName();
	private CompleteForecast completeForecast;
	private View view;
	private ViewPager mViewPager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_forecast, container, false);

		Bundle bundle = getArguments();

		if (Validator.isNotNull(bundle)) {

			completeForecast = bundle.getParcelable(CompleteForecast.ID);

			fillCityInfo();

			addTabsToActionBar();

			addListners();
		}

		return view;
	}

	private void setShareOptions(int dayIndex) {

		DayForecast dayForecast = completeForecast.getForecastByDay().get(dayIndex);

		Activity activity = getActivity();

		if (activity instanceof MainViewActivity) {

			MainViewActivity mainViewActivity = (MainViewActivity) activity;

			ShareActionProvider mShareActionProvider = mainViewActivity.getmShareActionProvider();

			if (Validator.isNotNull(mShareActionProvider)) {

				String textToShare = buildTextToShare(dayForecast);

				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_SUBJECT,
						AndroidUtil.getString(getActivity(), R.string.shareSubjectSelectionFrag));
				shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
				shareIntent.setType(ContentTypeUtil.TEXT_PLAIN);

				mainViewActivity.setShareIntent(shareIntent);
			}
		}
	}

	private String buildTextToShare(DayForecast dayForecast) {

		StringBuilder sb = new StringBuilder(AndroidUtil.getString(getActivity(), R.string.forecast));

		sb.append(CharPool.SPACE);
		sb.append(completeForecast.getCity().getUf());
		sb.append(CharPool.SPACE);
		sb.append(completeForecast.getCity().getName());
		sb.append(CharPool.SPACE);
		sb.append(CharPool.DASH);
		sb.append(CharPool.SPACE);
		sb.append(getDayWithMonthAndYear(dayForecast.getDay()));
		sb.append(CharPool.NEW_LINE);
		sb.append(CharPool.NEW_LINE);
		sb.append(dayForecast.getForecast().get(2)
				.getSharebleForecast(getActivity(), AndroidUtil.getString(getActivity(), R.string.manha)));
		sb.append(CharPool.NEW_LINE);
		sb.append(CharPool.NEW_LINE);
		sb.append(dayForecast.getForecast().get(4)
				.getSharebleForecast(getActivity(), AndroidUtil.getString(getActivity(), R.string.tarde)));
		sb.append(CharPool.NEW_LINE);
		sb.append(CharPool.NEW_LINE);
		sb.append(dayForecast.getForecast().get(6)
				.getSharebleForecast(getActivity(), AndroidUtil.getString(getActivity(), R.string.final_tarde)));
		sb.append(CharPool.NEW_LINE);
		sb.append(CharPool.NEW_LINE);
		sb.append(AndroidUtil.getString(getActivity(), R.string.shareTextSelectionFrag));
		sb.append(Constants.GOOGLE_PLAY_URL + getActivity().getPackageName());

		return sb.toString();
	}

	private void addListners() {

		SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREFS_FILE, getActivity().MODE_PRIVATE);
		final Set<String> stringSet = prefs.getStringSet(City.SHARED_PREFS_FAV_CITIES, new HashSet<String>());

		CheckBox checkBoxFav = (CheckBox) view.findViewById(R.id.addFav);

		if (stringSet.contains(completeForecast.getCity().getSharedPreferencesId())) {
			checkBoxFav.setChecked(true);
		}

		checkBoxFav.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				SharedPreferences.Editor editor = getActivity().getSharedPreferences(Constants.PREFS_FILE,
						getActivity().MODE_PRIVATE).edit();

				String sharedPreferencesId = completeForecast.getCity().getSharedPreferencesId();

				Set<String> newStringSet = new HashSet<String>();

				newStringSet.addAll(stringSet);

				if (isChecked) {

					newStringSet.add(sharedPreferencesId);
					editor.putStringSet(City.SHARED_PREFS_FAV_CITIES, newStringSet);

				} else {

					if (newStringSet.contains(sharedPreferencesId)) {
						newStringSet.remove(sharedPreferencesId);
						editor.putStringSet(City.SHARED_PREFS_FAV_CITIES, newStringSet);
					}
				}

				editor.commit();
			}
		});
	}

	private void fillTable(int dayIndex) {

		clearTable();

		DayForecast dayForecast = completeForecast.getForecastByDay().get(dayIndex);
		List<Forecast> forecastList = dayForecast.getForecast();

		TableRow waveHeightTableRow = (TableRow) view.findViewById(R.id.waveHeightTableRow);
		TableRow waveDirectionTableRow = (TableRow) view.findViewById(R.id.waveDirectionTableRow);
		TableRow unrestTableRow = (TableRow) view.findViewById(R.id.unrestTableRow);
		TableRow windSpeedTableRow = (TableRow) view.findViewById(R.id.windSpeedTableRow);
		TableRow windDirectionTableRow = (TableRow) view.findViewById(R.id.windDirectionTableRow);

		for (Forecast forecast : forecastList) {

			ImageView heightImage = imageViewFactory(getResources().getDrawable(R.drawable.wave_height));
			TextView heightTxtView = textViewFactory(String.valueOf(forecast.getWaveHeight()) + CharPool.LOWER_CASE_M);

			waveHeightTableRow.addView(linearLayoutFactory(heightImage, heightTxtView));

			ImageView waveDirectionImage = imageViewFactory(forecast.getWaveDirection().getDrawable(getActivity()));
			// ImageView waveDirectionImage = imageViewFactory(getResources().getDrawable(R.drawable.direction));
			TextView waveDirectionTxtView = textViewFactory(forecast.getWaveDirection().getAcronym().toString());
			waveDirectionTableRow.addView(linearLayoutFactory(waveDirectionImage, waveDirectionTxtView));

			ImageView unrestImage = imageViewFactory(getResources().getDrawable(R.drawable.unrest));
			TextView unrestTxtView = textViewFactory(forecast.getUnrest().toString());

			unrestTableRow.addView(linearLayoutFactory(unrestImage, unrestTxtView));

			ImageView windSpeedImage = imageViewFactory(getResources().getDrawable(R.drawable.wind_speed));
			TextView windSpeedTxtView = textViewFactory(String.valueOf(forecast.getWindSpeed()) + CharPool.LOWER_CASE_K + CharPool.LOWER_CASE_M + CharPool.FORWARD_SLASH + CharPool.LOWER_CASE_H);
			windSpeedTableRow.addView(linearLayoutFactory(windSpeedImage, windSpeedTxtView));

			ImageView windDirectionImage = imageViewFactory(forecast.getWindDirection().getDrawable(getActivity()));
			// ImageView windDirectionImage = imageViewFactory(getResources().getDrawable(R.drawable.direction));
			TextView windDirectionTxtView = textViewFactory(forecast.getWindDirection().getAcronym().toString());
			windDirectionTableRow.addView(linearLayoutFactory(windDirectionImage, windDirectionTxtView));
		}
	}

	protected void loadView(int position) {

		fillTable(position);

		showGraph(position);
	}

	private void showGraph(int position) {

		clearGraph();

		DayForecast dayForecast = completeForecast.getForecastByDay().get(position);

		List<String> graphUrlList = dayForecast.getGraphUrlList();

		CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(getActivity(), graphUrlList);

		if (Validator.isNull(mViewPager)) {

			mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
		}

		mViewPager.setAdapter(mCustomPagerAdapter);

	}

	private void clearGraph() {

		if (Validator.isNotNull(mViewPager)) {

			mViewPager.setAdapter(null);
		}
	}

	private LinearLayout linearLayoutFactory(View... viewList) {

		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout
				.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		for (View view : viewList) {
			linearLayout.addView(view);
		}

		return linearLayout;
	}

	private TextView textViewFactory(String text) {

		TextView textView = new TextView(getActivity());
		textView.setText(text);
		android.widget.TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1f);
		layoutParams.setMargins(AndroidUtil.convertDpToPixel(15, getActivity()), 0,
				AndroidUtil.convertDpToPixel(15, getActivity()), AndroidUtil.convertDpToPixel(15, getActivity()));
		textView.setLayoutParams(layoutParams);
		textView.setGravity(Gravity.CENTER_HORIZONTAL);
		textView.setTextColor(getResources().getColor(R.color.white));

		return textView;
	}

	private ImageView imageViewFactory(Drawable drawable) {

		ImageView imageView = new ImageView(getActivity());
		imageView.setImageDrawable(drawable);
		imageView.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

		return imageView;
	}

	private void clearTable() {

		List<TableRow> viewsToClean = new ArrayList();

		viewsToClean.add((TableRow) view.findViewById(R.id.waveHeightTableRow));
		viewsToClean.add((TableRow) view.findViewById(R.id.waveDirectionTableRow));
		viewsToClean.add((TableRow) view.findViewById(R.id.unrestTableRow));
		viewsToClean.add((TableRow) view.findViewById(R.id.windSpeedTableRow));
		viewsToClean.add((TableRow) view.findViewById(R.id.windDirectionTableRow));

		for (TableRow tableRow : viewsToClean) {

			clearTableRow(tableRow);
		}
	}

	private void clearTableRow(TableRow tableRow) {

		tableRow.removeAllViews();
	}

	private void fillCityInfo() {

		TextView breadcrumbLabel = (TextView) view.findViewById(R.id.breadcrumbLabel);

		String breadcrumbText = getBreadcrumbText();

		breadcrumbLabel.setText(breadcrumbText);
	}

	private String getBreadcrumbText() {

		StringBuilder sb = new StringBuilder();

		sb.append(completeForecast.getCity().getUf().toUpperCase());
		sb.append("  -  ");
		sb.append(completeForecast.getCity().getName());

		return sb.toString();
	}

	private void addTabsToActionBar() {

		ActionBar actionBar = getActivity().getActionBar();

		actionBar.removeAllTabs();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.light_blue)));

		List<Tab> allTabs = getAllTabs(actionBar);

		for (int i = 0; i < allTabs.size(); i++) {

			if (i == 0) {
				actionBar.addTab(allTabs.get(i), i, true);
			} else {
				actionBar.addTab(allTabs.get(i), i);
			}
		}
	}

	private List<Tab> getAllTabs(ActionBar actionBar) {

		List<Tab> tabs = new ArrayList<ActionBar.Tab>();

		List<DayForecast> forecastByDay = completeForecast.getForecastByDay();

		for (DayForecast dayForecast : forecastByDay) {

			tabs.add(getTab(actionBar, dayForecast));
		}

		return tabs;
	}

	private Tab getTab(ActionBar actionBar, final DayForecast dayForecast) {

		final Tab tab = actionBar.newTab();

		tab.setText(getDayWithMonth(dayForecast.getDay()));
		tab.setTabListener(new TabListener() {

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {

			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {

				setShareOptions(tab.getPosition());
				loadView(tab.getPosition());
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {

			}
		});

		return tab;
	}

	private String getDayWithMonth(Calendar calendar) {

		StringBuilder sb = new StringBuilder();

		sb.append(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
		sb.append("/");

		int month = calendar.get(Calendar.MONTH) + 1;

		String monthString = (month < 10) ? "0" + String.valueOf(month) : String.valueOf(month);

		sb.append(monthString);

		return sb.toString();
	}

	private String getDayWithMonthAndYear(Calendar calendar) {

		StringBuilder sb = new StringBuilder(getDayWithMonth(calendar));
		sb.append("/");
		sb.append(String.valueOf(calendar.get(Calendar.YEAR)));
		return sb.toString();
	}

	@Override
	public void onDestroy() {

		ActionBar actionBar = getActivity().getActionBar();
		actionBar.removeAllTabs();
		actionBar.setStackedBackgroundDrawable(null);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		super.onDestroy();
	}
}
