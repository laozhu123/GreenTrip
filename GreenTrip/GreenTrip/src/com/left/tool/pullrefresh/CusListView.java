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
	// ʵ�ʵ�padding�ľ����������ƫ�ƾ���ı���
	private final static int RATIO = 3;
	private LayoutInflater inflater;
	private LinearLayout headView;
	private TextView tipsTextview;
	private TextView lastUpdatedTextView;
	private ImageView arrowImageView;
	private ProgressBar progressBar;

	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	// ���ڱ�֤startY��ֵ��һ��������touch�¼���ֻ����¼һ��
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
	 * �Ƴ�item��Ļص��ӿ�
	 */

	private int totalItem = 0;

	// ���ظ���
	private LinearLayout mLoadMoreFooterView; // ���ظ���
	private TextView mLoadMoreText; // ��ʾ�ı�
	private ProgressBar mLoadMoreProgress; // ���ظ��������


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

		// �ײ�
		mLoadMoreFooterView = (LinearLayout) inflater.inflate(R.layout.zhu_loadmore_footer, this, false);
		mLoadMoreText = (TextView) mLoadMoreFooterView.findViewById(R.id.loadmore_text);
		mLoadMoreProgress = (ProgressBar) mLoadMoreFooterView.findViewById(R.id.loadmore_progress);

		measureView(mLoadMoreFooterView);
		footContentHeight = mLoadMoreFooterView.getMeasuredHeight();
		mLoadMoreFooterView.setPadding(0, 0, 0, -1 * footContentHeight);
		mLoadMoreFooterView.invalidate();

		addHeaderView(headView, null, false);
		addFooterView(mLoadMoreFooterView, null, false); // ����β����ͼ
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
					Log.v(TAG, "��downʱ���¼��ǰλ�á�");
				}
				break;

			case MotionEvent.ACTION_UP:

				if (state != REFRESHING && state != LOADING) {
					if (state == DONE) {
						// ʲô������
					}
					if (state == PULL_To_REFRESH) {
						state = DONE;
						changeHeaderViewByState();

						Log.v(TAG, "������ˢ��״̬����done״̬");
					}
					if (state == RELEASE_To_REFRESH) {
						state = REFRESHING;
						changeHeaderViewByState();
						onRefresh();

						Log.v(TAG, "���ɿ�ˢ��״̬����done״̬");
					}
				}

				isRecored = false;
				isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!isRecored && firstItemIndex == 0) {
					Log.v(TAG, "��moveʱ���¼��λ��");
					isRecored = true;
					startY = tempY;
				}

				if (state != REFRESHING && isRecored && state != LOADING) {
					// ��֤������padding�Ĺ����У���ǰ��λ��һֱ����head������������б�����Ļ�Ļ����������Ƶ�ʱ���б��ͬʱ���й���
					// ��������ȥˢ����
					if (state == RELEASE_To_REFRESH) {
						setSelection(0);
						// �������ˣ��Ƶ�����Ļ�㹻�ڸ�head�ĳ̶ȣ����ǻ�û���Ƶ�ȫ���ڸǵĵز�
						if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();

							Log.v(TAG, "���ɿ�ˢ��״̬ת�䵽����ˢ��״̬");
						}
						// һ�����Ƶ�����
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							Log.v(TAG, "���ɿ�ˢ��״̬ת�䵽done״̬");
						}
						// �������ˣ����߻�û�����Ƶ���Ļ�����ڸ�head�ĵز�
						else {
							// ���ý����ر�Ĳ�����ֻ�ø���paddingTop��ֵ������
						}
					}
					// ��û�е�����ʾ�ɿ�ˢ�µ�ʱ��,DONE������PULL_To_REFRESH״̬
					if (state == PULL_To_REFRESH) {

						setSelection(0);

						// ���������Խ���RELEASE_TO_REFRESH��״̬
						if ((tempY - startY) / RATIO >= headContentHeight) {
							state = RELEASE_To_REFRESH;
							isBack = true;
							changeHeaderViewByState();

							Log.v(TAG, "��done��������ˢ��״̬ת�䵽�ɿ�ˢ��");
						}
						// ���Ƶ�����
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();

							Log.v(TAG, "��DOne��������ˢ��״̬ת�䵽done״̬");
						}
					}

					// done״̬��
					if (state == DONE) {
						if (tempY - startY > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}

					// ����headView��size
					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight + (tempY - startY) / RATIO, 0, 0);

					}

					// ����headView��paddingTop
					if (state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO - headContentHeight, 0, 0);
					}

				}

				break;
			}
		}

		if (isLoadMoreable) {
			// System.out.println("����");
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
						// ʲô������
					}
					if (loadState == LOADMORE_PULL_To_REFRESH) {
						loadState = LOADMORE_DONE;
						changeFootViewByState();

						Log.v(TAG, "������ˢ��״̬����done״̬");
					}
					if (loadState == LOADMORE_RELEASE_To_REFRESH) {
						loadState = LOADMORE_REFRESHING;
						changeFootViewByState();
						OnLoadMore();

						Log.v(TAG, "���ɿ�ˢ��״̬����done״̬");
					}
				}

				loadmoreIsRecored = false;
				isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!loadmoreIsRecored && isOnBottom) {
					Log.v(TAG, "��moveʱ���¼��λ��");
					loadmoreIsRecored = true;
					startY = tempY;
				}

				if (loadState != LOADMORE_REFRESHING && loadmoreIsRecored && loadState != LOADMORE_LOADING) {

					// ��������ȥˢ����
					if (loadState == LOADMORE_RELEASE_To_REFRESH) {
						setSelection(totalItem);
						if (((startY - tempY) / RATIO < footContentHeight) && (startY - tempY) > 0) {
							loadState = LOADMORE_PULL_To_REFRESH;
							changeFootViewByState();
							Log.v(TAG, "���ɿ�ˢ��״̬ת�䵽����ˢ��״̬");
						}
						// һ�����Ƶ�����
						else if (startY - tempY <= 0) {
							loadState = LOADMORE_DONE;
							changeHeaderViewByState();

							Log.v(TAG, "���ɿ�ˢ��״̬ת�䵽done״̬");
						}
					}
					// ��û�е�����ʾ�ɿ�ˢ�µ�ʱ��,DONE������PULL_To_REFRESH״̬
					if (loadState == LOADMORE_PULL_To_REFRESH) {

						setSelection(totalItem);
						if ((startY - tempY) / RATIO >= footContentHeight) {
							loadState = LOADMORE_RELEASE_To_REFRESH;
							isBack = true;
							changeFootViewByState();

							Log.v(TAG, "��done��������ˢ��״̬ת�䵽�ɿ�ˢ��");
						}
						// ���Ƶ�����
						else if (startY - tempY <= 0) {
							loadState = LOADMORE_DONE;
							changeFootViewByState();

							Log.v(TAG, "��DOne��������ˢ��״̬ת�䵽done״̬");
						}
					}

					// done״̬��
					if (loadState == LOADMORE_DONE) {
						if (startY - tempY > 0 && isOnBottom) {
							loadState = LOADMORE_PULL_To_REFRESH;
							changeFootViewByState();
						}
					}

					// ����footview��size
					if (loadState == LOADMORE_PULL_To_REFRESH) {
						mLoadMoreFooterView.setPadding(0, 0, 0, (-1 * footContentHeight) + (startY - tempY) / RATIO);

					}

					// ����footview��paddingTop
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
			mLoadMoreText.setText("�ɿ�����");
			Log.v(TAG, "��ǰ״̬���ɿ�ˢ��");
			break;
		case LOADMORE_PULL_To_REFRESH:
			mLoadMoreProgress.setVisibility(View.INVISIBLE);
			mLoadMoreText.setText("�������ظ���");
			break;

		case LOADMORE_REFRESHING:
			mLoadMoreProgress.setVisibility(View.VISIBLE);
			mLoadMoreText.setText("���ڼ��أ����Ժ�...");
			Log.v(TAG, "��ǰ״̬,����ˢ��...");
			break;
		case LOADMORE_DONE:
			mLoadMoreProgress.setVisibility(View.INVISIBLE);
			mLoadMoreText.setText("���ظ���");
			mLoadMoreFooterView.setPadding(0, 0, 0, -1 * footContentHeight);
			break;
		}
	}

	// ��״̬�ı�ʱ�򣬵��ø÷������Ը��½���
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);

			tipsTextview.setText("�ɿ�ˢ��");

			Log.v(TAG, "��ǰ״̬���ɿ�ˢ��");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// ����RELEASE_To_REFRESH״̬ת������
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("����ˢ��");
			} else {
				tipsTextview.setText("����ˢ��");
			}
			Log.v(TAG, "��ǰ״̬������ˢ��");
			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("����ˢ��...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.v(TAG, "��ǰ״̬,����ˢ��...");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);

			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.arrow1);
			tipsTextview.setText("����ˢ��");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.v(TAG, "��ǰ״̬��done");
			break;
		}
	}

	// �˷���ֱ���հ��������ϵ�һ������ˢ�µ�demo���˴��ǡ����ơ�headView��width�Լ�height
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
		lastUpdatedTextView.setText("�������:" + new Date().toLocaleString());
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
		lastUpdatedTextView.setText("�������:" + new Date().toLocaleString());
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
	 * �ж��Ƿ���Լ�������
	 */
	public void isHaveMoreDate(Boolean isLoadMoreable){
		this.isLoadMoreable = isLoadMoreable;
	}
}