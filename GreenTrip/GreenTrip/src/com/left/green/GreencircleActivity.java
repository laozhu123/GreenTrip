package com.left.green;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import com.dodola.model.DuitangInfo;
import com.dodola.waterex.StaggeredAdapter;
import com.example.android.bitmapfun.util.Helper;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.origamilabs.library.views.StaggeredGridView;
import com.pinstame.view.SwipeRefreshAndLoadLayout;

@SuppressLint({ "InlinedApi", "ShowToast" }) public class GreencircleActivity extends Activity implements SwipeRefreshAndLoadLayout.OnRefreshListener {
	private ImageFetcher mImageFetcher;
	private StaggeredAdapter mAdapter;
	private ContentTask task = new ContentTask(this, 2);
    private ImageView back;
	private SwipeRefreshAndLoadLayout swipeLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_greencircle);
		mImageFetcher = new ImageFetcher(this, 240);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		StaggeredGridView gridView = (StaggeredGridView) this.findViewById(R.id.staggeredGridView1);
        back=(ImageView) findViewById(R.id.back);
		gridView.setFastScrollEnabled(true);
		mAdapter = new StaggeredAdapter(this, mImageFetcher);
		gridView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
		AddItemToContainer(1, 1);
		AddItemToContainer(2, 1);
		AddItemToContainer(3, 1);
		
		swipeLayout = (SwipeRefreshAndLoadLayout) this.findViewById(R.id.swipe_refresh);
		swipeLayout.setOnRefreshListener(this);
		swipeLayout.setmMode(SwipeRefreshAndLoadLayout.Mode.BOTH);
		
		// 顶部刷新的样式
		swipeLayout.setColorScheme(android.R.color.holo_red_light, android.R
				.color.holo_green_light, android.R.color.holo_blue_bright, 
				android.R.color.holo_orange_light);
	    back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	@Override
    public void onRefresh() {
	
		
		Toast.makeText(this, "refresh", 2000).show();
		
		new Handler().postDelayed(new Runnable() {
			public void run() {
				swipeLayout.setRefreshing(false);
				
			}
		}, 5000);
		
    }

    @Override
    public void onLoadMore() {
    	
    	Toast.makeText(this, "refresh", 2000).show();
		
		new Handler().postDelayed(new Runnable() {
			public void run() {
				swipeLayout.setRefreshing(false);
				
			}
		}, 5000);
    }
    
	private void AddItemToContainer(int pageindex, int type) {
		if (task.getStatus() != Status.RUNNING) {
			String url = "http://www.duitang.com/album/1733789/masn/p/" + pageindex + "/24/";
			Log.d("MainActivity", "current url:" + url);
			ContentTask task = new ContentTask(this, type);
			task.execute(url);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class ContentTask extends AsyncTask<String, Integer, List<DuitangInfo>> {

		private Context mContext;
		private int mType = 1;

		public ContentTask(Context context, int type) {
			super();
			mContext = context;
			mType = type;
		}

		@Override
		protected List<DuitangInfo> doInBackground(String... params) {
			try {
				return parseNewsJSON(params[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<DuitangInfo> result) {
			if (mType == 1) {

				mAdapter.addItemTop(result);
				mAdapter.notifyDataSetChanged();

			} else if (mType == 2) {
				mAdapter.addItemLast(result);
				mAdapter.notifyDataSetChanged();

			}

		}

		@Override
		protected void onPreExecute() {
		}

		public List<DuitangInfo> parseNewsJSON(String url) throws IOException {
			List<DuitangInfo> duitangs = new ArrayList<DuitangInfo>();
			String json = "";
			if (Helper.checkConnection(mContext)) {
				try {
					json = Helper.getStringFromUrl(url);

				} catch (IOException e) {
					Log.e("IOException is : ", e.toString());
					e.printStackTrace();
					return duitangs;
				}
			}
			Log.d("MainActiivty", "json:" + json);

			try {
				if (null != json) {
					JSONObject newsObject = new JSONObject(json);
					JSONObject jsonObject = newsObject.getJSONObject("data");
					JSONArray blogsJson = jsonObject.getJSONArray("blogs");

					for (int i = 0; i < blogsJson.length(); i++) {
						JSONObject newsInfoLeftObject = blogsJson.getJSONObject(i);
						DuitangInfo newsInfo1 = new DuitangInfo();
						newsInfo1.setAlbid(newsInfoLeftObject.isNull("albid") ? "" : newsInfoLeftObject.getString("albid"));
						newsInfo1.setIsrc(newsInfoLeftObject.isNull("isrc") ? "" : newsInfoLeftObject.getString("isrc"));
						newsInfo1.setMsg(newsInfoLeftObject.isNull("msg") ? "" : newsInfoLeftObject.getString("msg"));
						newsInfo1.setHeight(newsInfoLeftObject.getInt("iht"));
						duitangs.add(newsInfo1);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return duitangs;
		}
	}

}
