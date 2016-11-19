package com.left.tool.pullrefresh;


import java.util.Date;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.left.green.R;

public class CusListView extends ExpandableListView implements OnScrollListener {
	private static final String TAG = "listview";
	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	private final static int LOADMORE_RELEASE_To_REFRESH = 7;
	private final static int LOADMORE_PULL_To_REFRESH = 8;
	private final static int LOADMORE_REFRESHING = 9;
	private final static int LOADMORE_DONE = 10;
	private final static int LOADMORE_LOADING = 11;
	// 实际的padding的距离与界面上偏移距离的比例
	private final static int RATIO = 3;
	private LayoutInflater inflater;
	private LinearLayout headView;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;

	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;
	private boolean loadmoreIsRecored;

	// private int headContentWidth;
	private int headContentHeight;
	private int footContentHeight;

	private int startY;
	private int firstItemIndex;

	private boolean isOnBottom;

	private int state;
	private int loadState;

	private boolean isBack;

	private OnRefreshListener refreshListener;
	private OnLoadMoreListener loadMoreListener;

	private boolean isRefreshable;
	private boolean isLoadMoreable;

	private GestureDetector mGestureDetector;
	View.OnTouchListener mGestureListener;

	/**
	 * 移除item后的回调接口
	 */

	private int totalItem = 0;

	// 加载更多
	private LinearLayout mLoadMoreFooterView; // 加载更多
	private TextView mLoadMoreText; // 提示文本
	private ProgressBar mLoadMoreProgress; // 加载更多进度条


	public void setViewPager(ViewPager viewPager) {
	}

	public CusListView(Context context) {
		super(context);
		init(context);
	}

	public CusListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
		init(context);
	}

	public CusListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setCacheColorHint(context.getResources().getColor(R.color.transparent));
		inflater = LayoutInflater.from(context);

		headView = (LinearLayout) inflater.inflate(R.layout.zhu_head_listview, null);

		arrowImageView = (ImageView) headView.findViewById(R.id.head_arrowImageView);
		arrowImageView.setMinimumWidth(70);
		arrowImageView.setMinimumHeight(50);
		progressBar = (ProgressBar) headView.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView.findViewById(R.id.head_lastUpdatedTextView);

		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();

		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();

		// 底部
		mLoadMoreFooterView = (LinearLayout) inflater.inflate(R.layout.zhu_loadmore_footer, this, false);
		mLoadMoreText = (TextView) mLoadMoreFooterView.findViewById(R.id.loadmore_text);
		mLoadMoreProgress = (ProgressBar) mLoadMoreFooterView.findViewById(R.id.loadmore_progress);

		measureView(mLoadMoreFooterView);
		footContentHeight = mLoadMoreFooterView.getMeasuredHeight();
		mLoadMoreFooterView.setPadding(0, 0, 0, -1 * footContentHeight);
		mLoadMoreFooterView.invalidate();

		addHeaderView(headView, null, false);
		addFooterView(mLoadMoreFooterView, null, false); // 增加尾部视图
		setOnScrollListener(this);

		animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		state = DONE;
		loadState = LOADMORE_DONE;

		isRefreshable = false;
		isLoadMoreable = false;
	}

	public void onScroll(AbsListView arg0, int firstVisiableItem, int visibleItemCount, int totalItemCount) {
		firstItemIndex = firstVisiableItem;
		totalItem = totalItemCount;
		if (firstVisiableItem + visibleItemCount == totalItemCount) {
			isOnBottom = true;
			return;
		} else {
			isOnBottom = false;
		}
	}

	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	public boolean onTouchEvent(MotionEvent event) {
		// System.out.println("isRefreshable->" + isRefreshable);
		if (isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (firstItemIndex == 0 && !isRecored) {
					isRecored = true;
					startY = (int) event.getY();
					Log.v(TAG, "在down时候记录当前位置‘");
				}
				break;

			case MotionEvent.ACTION_UP:

				if (state != REFRESHING && state != LOADING) {
					if (state == DONE) {
						// 什么都不做
					}
					if (state == PULL_To_REFRESH) {
						state = DONE;
						changeHeaderViewByState();

						Log.v(TAG, "由下拉刷新状态，到done状态");
					}
					if (state == RELEASE_To_REFRESH) {
						state = REFRESHING;
						changeHeaderViewByState();
						onRefresh();

						Log.v(TAG, "由松开刷新状态，到done状态");
					}
				}

				isRecored = false;
				isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!isRecored && firstItemIndex == 0) {
					Log.v(TAG, "在move时候记录下位置");
					isRecored = true;
					startY = tempY;
				}

				if (state != REFRESHING && isRecored && state != LOADING) {
					// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动
					// 可以松手去刷新了
					if (state == RELEASE_To_REFRESH) {
						setSelection(0);
						// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
						if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();

							Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
						}
						// 一下子推到顶了
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							Log.v(TAG, "由松开刷新状态转变到done状态");
						}
						// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
						else {
							// 不用进行特别的操作，只用更新paddingTop的值就行了
						}
					}
					// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
					if (state == PULL_To_REFRESH) {

						setSelection(0);

						// 下拉到可以进入RELEASE_TO_REFRESH的状态
						if ((tempY - startY) / RATIO >= headContentHeight) {
							state = RELEASE_To_REFRESH;
							isBack = true;
							changeHeaderViewByState();

							Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
						}
						// 上推到顶了
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							Log.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
						}
					}

					// done状态下
					if (state == DONE) {
						if (tempY - startY > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}

					// 更新headView的size
					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight + (tempY - startY) / RATIO, 0, 0);

					}

					// 更新headView的paddingTop
					if (state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO - headContentHeight, 0, 0);
					}

				}

				break;
			}
		}

		if (isLoadMoreable) {
			// System.out.println("上拉");
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (isOnBottom && !loadmoreIsRecored) {
					loadmoreIsRecored = true;
					startY = (int) event.getY();
				}
				break;

			case MotionEvent.ACTION_UP:

				if (loadState != LOADMORE_REFRESHING && loadState != LOADMORE_LOADING) {
					if (loadState == DONE) {
						// 什么都不做
					}
					if (loadState == LOADMORE_PULL_To_REFRESH) {
						loadState = LOADMORE_DONE;
						changeFootViewByState();

						Log.v(TAG, "由下拉刷新状态，到done状态");
					}
					if (loadState == LOADMORE_RELEASE_To_REFRESH) {
						loadState = LOADMORE_REFRESHING;
						changeFootViewByState();
						OnLoadMore();

						Log.v(TAG, "由松开刷新状态，到done状态");
					}
				}

				loadmoreIsRecored = false;
				isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!loadmoreIsRecored && isOnBottom) {
					Log.v(TAG, "在move时候记录下位置");
					loadmoreIsRecored = true;
					startY = tempY;
				}

				if (loadState != LOADMORE_REFRESHING && loadmoreIsRecored && loadState != LOADMORE_LOADING) {

					// 可以松手去刷新了
					if (loadState == LOADMORE_RELEASE_To_REFRESH) {
						setSelection(totalItem);
						if (((startY - tempY) / RATIO < footContentHeight) && (startY - tempY) > 0) {
							loadState = LOADMORE_PULL_To_REFRESH;
							changeFootViewByState();
							Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
						}
						// 一下子推到顶了
						else if (startY - tempY <= 0) {
							loadState = LOADMORE_DONE;
							changeHeaderViewByState();

							Log.v(TAG, "由松开刷新状态转变到done状态");
						}
					}
					// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
					if (loadState == LOADMORE_PULL_To_REFRESH) {

						setSelection(totalItem);
						if ((startY - tempY) / RATIO >= footContentHeight) {
							loadState = LOADMORE_RELEASE_To_REFRESH;
							isBack = true;
							changeFootViewByState();

							Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
						}
						// 上推到顶了
						else if (startY - tempY <= 0) {
							loadState = LOADMORE_DONE;
							changeFootViewByState();

							Log.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
						}
					}

					// done状态下
					if (loadState == LOADMORE_DONE) {
						if (startY - tempY > 0 && isOnBottom) {
							loadState = LOADMORE_PULL_To_REFRESH;
							changeFootViewByState();
						}
					}

					// 更新footview的size
					if (loadState == LOADMORE_PULL_To_REFRESH) {
						mLoadMoreFooterView.setPadding(0, 0, 0, (-1 * footContentHeight) + (startY - tempY) / RATIO);

					}

					// 更新footview的paddingTop
					if (loadState == LOADMORE_RELEASE_To_REFRESH) {
						mLoadMoreFooterView.setPadding(0, 0, 0, 0);
					}

				}

				break;
			}
		}
		

		return super.onTouchEvent(event);
	}

	private void changeFootViewByState() {
		switch (loadState) {
		case LOADMORE_RELEASE_To_REFRESH:
			mLoadMoreProgress.setVisibility(View.INVISIBLE);
			mLoadMoreText.setText("松开加载");
			Log.v(TAG, "当前状态，松开刷新");
			break;
		case LOADMORE_PULL_To_REFRESH:
			mLoadMoreProgress.setVisibility(View.INVISIBLE);
			mLoadMoreText.setText("上拉加载更多");
			break;

		case LOADMORE_REFRESHING:
			mLoadMoreProgress.setVisibility(View.VISIBLE);
			mLoadMoreText.setText("正在加载，请稍候...");
			Log.v(TAG, "当前状态,正在刷新...");
			break;
		case LOADMORE_DONE:
			mLoadMoreProgress.setVisibility(View.INVISIBLE);
			mLoadMoreText.setText("加载更多");
			mLoadMoreFooterView.setPadding(0, 0, 0, -1 * footContentHeight);
			break;
		}
	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);

			tipsTextview.setText("松开刷新");

			Log.v(TAG, "当前状态，松开刷新");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("下拉刷新");
			} else {
				tipsTextview.setText("下拉刷新");
			}
			Log.v(TAG, "当前状态，下拉刷新");
			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("正在刷新...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.v(TAG, "当前状态,正在刷新...");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);

			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.arrow1);
			tipsTextview.setText("下拉刷新");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.v(TAG, "当前状态，done");
			break;
		}
	}

	// 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setAdapter(BaseAdapter adapter) {
		lastUpdatedTextView.setText("最近更新:" + new Date().toLocaleString());
		super.setAdapter(adapter);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		super.onInterceptTouchEvent(ev);
		return mGestureDetector.onTouchEvent(ev);
	}

	class YScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (Math.abs(distanceY) >= Math.abs(distanceX)) {
				return true;
			}
			return false;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return super.onSingleTapUp(e);
		}
	}


	
	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void onRefreshComplete() {
		state = DONE;
		lastUpdatedTextView.setText("最近更新:" + new Date().toLocaleString());
		changeHeaderViewByState();
	}

	private void onRefresh() {
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
	}
	
	public void OnLoadMore() {
		if (loadMoreListener != null) {
			loadMoreListener.onLoadMore();
		}
	}

	public void onLoadMoreComplete() {
		// resetFooter();
		loadState = LOADMORE_DONE;
		changeFootViewByState();
	}

	public void setonLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
		isLoadMoreable = true;
	}

	public interface OnLoadMoreListener {
		public void onLoadMore();
	}

	/**
	 * 判断是否可以继续加载
	 */
	public void isHaveMoreDate(Boolean isLoadMoreable){
		this.isLoadMoreable = isLoadMoreable;
	}
}