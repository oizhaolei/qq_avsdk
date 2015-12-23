package com.tencent.avsdk;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.av.sdk.AVConstants;
import com.tencent.avsdk.control.QavsdkControl;

public class MultiVideoMembersControlUI extends RelativeLayout {

	private static final String TAG = "MultiVideoMembersControlUI";
	boolean mAttached = false;

	public interface MultiVideoMembersClickListener {
		public void onMembersClick(final String identifier, final int videoSrcType,
				final boolean needRequest);

		public void onMembersHolderTouch();
	}

	private Resources mResources = null;

	private ViewPager mMembersContainer = null; // 用来展示成员列表
	private LinearLayout mPageIndicator = null; // 用来展示下方的indicator

	private View mRootView = null;

	private ArrayList<GridView> mGridViewContainer = null;

	private LayoutInflater mInFlater = null;

	private GridViewPagerAdapter mAdapter = null;

	private int mCurrentPage = 0;
	private int mTotalPageNum = 1;
	private String mSelectedIdentifier = null;
	private int mSelectedVideoSrcType = AVConstants.VIDEO_SRC_UNKNOWN;

	private int mMode = MODE_TWO_LINE;
	private int mMaxPageNum = MAX_PAGE_MEMBERS_NUM_TWO_LINE;

	private static final int MAX_ROW_NUM = 3;
	private static final int MAX_PAGE_MEMBERS_NUM_TWO_LINE = 6;
	private static final int MAX_PAGE_MEMBERS_NUM_ONE_LINE = 3;

	public static final int MODE_TWO_LINE = 0;
	public static final int MODE_ONE_LINE = 1;

	private static final int MAX_PAGE_NUM = 3;

	private MultiVideoMembersClickListener mMemberClickListener = null;
	private QavsdkControl mQavsdkControl = null;

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(Util.ACTION_MEMBER_CHANGE)) {
				notifyDataSetChanged(mQavsdkControl.getMemberList());
			}
		}
	};

	@SuppressLint({ "InlinedApi", "NewApi" })
	public MultiVideoMembersControlUI(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		super(context, attrs);
		mResources = getResources();

		initModeAndMaxPageNum();

		mInFlater = LayoutInflater.from(context);
		mRootView = mInFlater.inflate(
				R.layout.qav_multi_video_members_control_ui, null);
		mRootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		mPageIndicator = (LinearLayout) mRootView
				.findViewById(R.id.qav_members_pagers_indicator);
		mMembersContainer = (ViewPager) mRootView
				.findViewById(R.id.qav_members_container);

		if (Build.VERSION.SDK_INT >= 9) {
			mMembersContainer.setOverScrollMode(View.OVER_SCROLL_NEVER);
		}

		mMembersContainer.setOnPageChangeListener(mOnPageChangeListener);

		mGridViewContainer = new ArrayList<GridView>();

		LayoutParams layoutParams = (LayoutParams) mRootView.getLayoutParams();
		int height = 0;
		if (mMode == MODE_ONE_LINE) {
			height = mResources
					.getDimensionPixelSize(R.dimen.qav_gaudio_members_holder_height_one_line);
			layoutParams.height = height;
			mRootView.setLayoutParams(layoutParams);
		}

		mAdapter = new GridViewPagerAdapter();
		mMembersContainer.setAdapter(mAdapter);

		this.addView(mRootView);
		mQavsdkControl = ((QavsdkApplication) getContext().getApplicationContext()).getQavsdkControl();
	}

	private void refreshDataSource() {
		initPages();
	}

	private void initPages() {
		ArrayList<MemberInfo> membersList = mQavsdkControl.getMemberList();
		int size = membersList.size();
		Log.d(TAG, "WL_DEBUG initPages size = " + size);
		Log.d(TAG, "WL_DEBUG initPages mMaxPageNum = " + mMaxPageNum);
		mTotalPageNum = (size + mMaxPageNum - 1) / mMaxPageNum;

		// 群模式下，最多只允许3页
		if (mTotalPageNum > MAX_PAGE_NUM) {
			mTotalPageNum = MAX_PAGE_NUM;
		}

		Log.d(TAG, "WL_DEBUG initPages mTotalPageNum = " + mTotalPageNum);
	}

	private void initModeAndMaxPageNum() {
		int screenHeight = Util.getScreenHeight(getContext());

		float density = mResources.getDisplayMetrics().densityDpi;
		float scale = density / 160;
		float height = (float) screenHeight * (float) screenHeight / 1136
				/ scale;
		Log.d(TAG, "initModeAndPageNum-->density=" + density + " scale="
				+ scale + " height=" + height);
		if (height >= 480) {
			mMode = MODE_TWO_LINE;
			mMaxPageNum = MAX_PAGE_MEMBERS_NUM_TWO_LINE;
		} else {
			// TODO wadesheng 一行的时候有bug，底下的 滑动按钮 没有出来，需要再调整
			mMode = MODE_ONE_LINE;
			mMaxPageNum = MAX_PAGE_MEMBERS_NUM_ONE_LINE;
		}
	}

	public int getMode() {
		return mMode;
	}

	private void fireDataChange() {
		mCurrentPage = mMembersContainer.getCurrentItem();
		int pageNum = mGridViewContainer.size();
		boolean needSetCurrentItem = false;

		if (pageNum != mTotalPageNum) {

			mMembersContainer.removeAllViews();
			mGridViewContainer.clear();

			for (int i = 0; i < mTotalPageNum; i++) {
				GridViewBaseAdapter adapter = new GridViewBaseAdapter(i);
				GridView gridView = initGridView();
				gridView.setAdapter(adapter);
				if (mOnTouchListener != null) {
					gridView.setOnTouchListener(mOnTouchListener);
				}
				if (mItemClickListener != null) {
					gridView.setOnItemClickListener(mItemClickListener);
				}
				mGridViewContainer.add(gridView);
			}
			needSetCurrentItem = true;
			setIndicatorSelected(mCurrentPage);

		} else {
			for (int i = 0; i < mTotalPageNum; i++) {
				GridView gridView = mGridViewContainer.get(i);
				GridViewBaseAdapter adapter = (GridViewBaseAdapter) gridView
						.getAdapter();
				adapter.notifyDataSetChanged();
			}
		}

		adapterPageSize();
		mAdapter.notifyDataSetChanged();

		if (needSetCurrentItem) {
			mMembersContainer.setCurrentItem(mCurrentPage, true);
		}
	}

	private void adapterPageSize() {
		Log.d(TAG, "adaptPaperSize");

		int screenwidth = Util.getScreenWidth(getContext());
		int itemWidth = mResources
				.getDimensionPixelSize(R.dimen.qav_gaudio_grid_item_width);

		int space = (screenwidth - itemWidth * MAX_ROW_NUM) / (MAX_ROW_NUM + 1);
		int left = (screenwidth - itemWidth * MAX_ROW_NUM - space
				* (MAX_ROW_NUM - 1)) / 2;

		int top = 0;
		if (mMode == MODE_TWO_LINE) {
			top = mResources
					.getDimensionPixelSize(R.dimen.qav_gaudio_members_holder_margin_top_large);
		} else {
			top = mResources
					.getDimensionPixelSize(R.dimen.qav_gaudio_members_holder_margin_top_small);
		}

		ArrayList<MemberInfo> membersList = mQavsdkControl.getMemberList();
		
		if (membersList.size() <= MAX_ROW_NUM) {
			int num = membersList.size();
			left = (screenwidth - itemWidth * num - space * (num - 1)) / 2;
		} else {
			top = mResources
					.getDimensionPixelSize(R.dimen.qav_gaudio_members_holder_margin_top_small);
		}

		LayoutParams sParams = (LayoutParams) this.getLayoutParams();
		LayoutParams mParams = (LayoutParams) mMembersContainer
				.getLayoutParams();
		sParams.topMargin = top;
		mParams.leftMargin = left;
		this.setLayoutParams(sParams);
		mMembersContainer.setLayoutParams(mParams);
	}

	private void notifyCurrentPageDataSetChanged() {
		GridView gridView = mGridViewContainer.get(mCurrentPage);
		GridViewBaseAdapter adapter = (GridViewBaseAdapter) gridView
				.getAdapter();

		adapter.notifyDataSetChanged();
	}

	public void notifyDataSetChanged(ArrayList<MemberInfo> friends) {
		Log.d(TAG, "WL_DEBUG notifyDataSetChanged");
		if (friends != null) {
			refreshDataSource();
			fireDataChange();
		}
	}

	public void setOnMemberClickListener(MultiVideoMembersClickListener lisnter) {
		Log.d(TAG, "setOnMemberClickListener");
		if (lisnter == null) {
			Log.e(TAG, "setOnMemberClickListener-->listener is null");
			return;
		}
		mMemberClickListener = lisnter;
	}

	private Bitmap getDefaultBitmap() {
		Drawable temp = mResources.getDrawable(R.drawable.h001);
		BitmapDrawable bitmapDrawable = (BitmapDrawable) temp;
		return bitmapDrawable.getBitmap();
	}

	private GridView initGridView() {
		Log.d(TAG, "initGridView");
		int screenWidth = Util.getScreenWidth(getContext());
		int screenHeight = Util.getScreenHeight(getContext());

		int spacingV = mResources.getDimensionPixelSize(R.dimen.gaudio_spacing);
		if (screenHeight <= 320) {
			spacingV = mResources
					.getDimensionPixelSize(R.dimen.gaudio_spacing_320);
		}

		int itemWidth = mResources
				.getDimensionPixelSize(R.dimen.qav_gaudio_grid_item_width);
		int spacingH = (screenWidth - itemWidth * MAX_ROW_NUM)
				/ (MAX_ROW_NUM + 1);

		GridView gridView = new GridView(getContext());
		if (mMode == MODE_TWO_LINE) {
			gridView.setLayoutParams(new LayoutParams(
					screenWidth,
					mResources
							.getDimensionPixelSize(R.dimen.qav_gaudio_members_container_two_line)));
		} else {
			gridView.setLayoutParams(new LayoutParams(
					screenWidth,
					mResources
							.getDimensionPixelSize(R.dimen.qav_gaudio_members_container_one_line)));
		}

		gridView.setColumnWidth(itemWidth);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setGravity(Gravity.CENTER_HORIZONTAL);
		gridView.setVerticalScrollBarEnabled(false);
		gridView.setNumColumns(MAX_ROW_NUM);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		gridView.setCacheColorHint(0);
		gridView.setHorizontalSpacing(spacingH);
		gridView.setVerticalSpacing(spacingV);

		return gridView;
	}

	private class GridViewPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mGridViewContainer.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			container.addView(mGridViewContainer.get(position));
			return mGridViewContainer.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			// super.destroyItem(container, position, object);
			if (position >= mGridViewContainer.size()) {
				return;
			}
			container.removeView(mGridViewContainer.get(position));
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return PagerAdapter.POSITION_NONE;
		}
	}

	private class GridViewBaseAdapter extends BaseAdapter {
		private int pageIndex;

		public GridViewBaseAdapter(int currentPage) {
			// TODO Auto-generated constructor stub
			pageIndex = currentPage;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (mTotalPageNum == 0) {
				Log.e(TAG, "mTotalPageNum == 0!!!Why?");
				return 0;
			}

			int count = 0;
			ArrayList<MemberInfo> membersList = mQavsdkControl.getMemberList();
			if (membersList.size() == 0) {
				return 0;
			}

			if (mMaxPageNum * mTotalPageNum <= membersList.size()) {
				return mMaxPageNum;
			}

			if (this.pageIndex == mTotalPageNum - 1) {
				if (membersList.size() % mMaxPageNum == 0) {
					count = mMaxPageNum;
				} else {
					count = membersList.size() % mMaxPageNum;
				}
			} else {
				count = mMaxPageNum;
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			ArrayList<MemberInfo> membersList = mQavsdkControl.getMemberList();
			if (membersList != null) {
				int displayPos = position + this.pageIndex * mMaxPageNum;
				return membersList.get(displayPos);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position + this.pageIndex * mMaxPageNum;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stu
			if (position == 0 && convertView != null) {
				if (parent.getChildCount() != position) {
					return convertView;
				}
			}

			int type = GET_VIEW_TYPE.NOMARL;
			ArrayList<MemberInfo> membersList = mQavsdkControl.getMemberList();
			if (position == mMaxPageNum - 1
					&& membersList.size() > MAX_PAGE_NUM * mMaxPageNum
					&& this.pageIndex == mTotalPageNum - 1) {
				type = GET_VIEW_TYPE.LAST_ITEM_IN_GROUP_MODE;
			}

			return getView(position, convertView, type);
		}

		private View getView(int position, View convertView, int type) {

			int displayPos = position + this.pageIndex * mMaxPageNum;
			ArrayList<MemberInfo> membersList = mQavsdkControl.getMemberList();
			MemberInfo friend = membersList.get(displayPos);

			Log.d(TAG, "getView-->identifier=" + friend.identifier + " position="
					+ position);

			if (type == GET_VIEW_TYPE.LAST_ITEM_IN_GROUP_MODE) {
				if (convertView == null) {
					convertView = mInFlater.inflate(R.layout.qav_gaudio_item,
							null);
				}

				ImageView head = (ImageView) convertView
						.findViewById(R.id.qav_gaudio_face);
				TextView name = (TextView) convertView
						.findViewById(R.id.qav_gaudio_name);
				name.setText("更多成员");
				head.setImageResource(R.drawable.qav_more_members_item);
				return convertView;
			}

			if (friend.name == null || friend.faceBitmap == null
					|| !friend.hasGetInfo) {

				friend.name = friend.name == null ? "暂无姓名" : friend.name;

				if (friend.name.compareTo(friend.identifier) != 0) {
					friend.hasGetInfo = true;
				} else {
					friend.hasGetInfo = false;
				}

				friend.faceBitmap = null;
				friend.hasGetInfo = false;
			}
			
			if (friend.name != null && friend.name.equals(mQavsdkControl.getSelfIdentifier())) {
				friend.name = MultiVideoMembersControlUI.this.getContext().getString(R.string.me);
			}

			convertView = assembleConvertView(convertView, friend,
					friend.identifier.equals(mSelectedIdentifier));
			return convertView;
		}

		private View assembleConvertView(View view, MemberInfo info,
				boolean isSelected) {
			Log.d(TAG, "WL_DEBUG assembleConvertView info = " + info);
			Holder holder = null;
			if (view == null) {
				view = mInFlater.inflate(R.layout.qav_gaudio_item, null);
				holder = new Holder();
				ImageView head = (ImageView) view
						.findViewById(R.id.qav_gaudio_face);
				TextView name = (TextView) view
						.findViewById(R.id.qav_gaudio_name);
				holder.head = head;
				holder.name = name;
				holder.cover = (ImageView) view
						.findViewById(R.id.qav_gaudio_face_cover);
				holder.speakingIcon = (ImageView) view
						.findViewById(R.id.qav_gaudio_speaking_icon);
				view.setTag(holder);
			} else {
				holder = (Holder) view.getTag();
			}

			holder.isSelected = isSelected;
			holder.speaking = info.isSpeaking;
			holder.acc_type = ACC_TEXT_TYPE.NONE;

			if (info.name != null) {
				holder.name.setText(info.name);
			}
			if (info.faceBitmap != null) {
				holder.head.setImageBitmap(info.faceBitmap);
			} else {
				holder.head.setImageBitmap(getDefaultBitmap());
			}

			// 添加border
			if (holder.speaking) {
				holder.speakingIcon.setVisibility(View.VISIBLE);
				holder.acc_type = ACC_TEXT_TYPE.SPEAKING;
			} else {
				holder.speakingIcon.setVisibility(View.GONE);
			}

			// 添加蒙层
			if (info.isShareSrc) {
				holder.cover.setVisibility(View.VISIBLE);
				holder.cover.setImageResource(R.drawable.qav_screen_press);
				holder.acc_type = ACC_TEXT_TYPE.REQUEST_SCREEN;

			} else if (info.isVideoIn) {
				holder.cover.setVisibility(View.VISIBLE);
				holder.cover.setImageResource(R.drawable.qav_camera_press);
				holder.acc_type = ACC_TEXT_TYPE.REQUEST_VIDEO;
			} else if (info.isShareMovie) {
				holder.cover.setVisibility(View.VISIBLE);
				holder.cover
						.setImageResource(R.drawable.qav_gaudio_share_movie_press);
				holder.acc_type = ACC_TEXT_TYPE.REQUEST_MOVIE;
			} else {
				holder.cover.setVisibility(View.GONE);
			}

			// 增加选中态
			if (holder.isSelected) {
				if (info.isShareSrc && info.isVideoIn) {
					if (mSelectedVideoSrcType == AVConstants.VIDEO_SRC_CAMERA) {
						holder.cover.setVisibility(View.VISIBLE);
						holder.cover
								.setImageResource(R.drawable.qav_gvideo_screen_normal);
						holder.acc_type = ACC_TEXT_TYPE.PLAYING_VIDEO;
					} else if (mSelectedVideoSrcType == AVConstants.VIDEO_SRC_SHARESCREEN) {
						holder.cover.setVisibility(View.VISIBLE);
						holder.cover
								.setImageResource(R.drawable.qav_gvideo_camera_normal);
						holder.acc_type = ACC_TEXT_TYPE.PLAYING_SCREEN;
					} else {
						// holder.selected.setVisibility(View.GONE);
					}
				} else if (info.isShareSrc) {
					holder.cover.setVisibility(View.VISIBLE);
					holder.cover
							.setImageResource(R.drawable.qav_gvideo_screen_normal);
					holder.acc_type = ACC_TEXT_TYPE.PLAYING_SCREEN;

				} else if (info.isVideoIn) {
					holder.cover.setVisibility(View.VISIBLE);
					holder.cover
							.setImageResource(R.drawable.qav_gvideo_camera_normal);
					holder.acc_type = ACC_TEXT_TYPE.PLAYING_VIDEO;
				} else if (info.isShareMovie) {
					holder.cover.setVisibility(View.VISIBLE);
					holder.cover
							.setImageResource(R.drawable.qav_share_movie_select);
					holder.acc_type = ACC_TEXT_TYPE.PLAYING_MOVIE;
				} else {
					// holder.selected.setVisibility(View.GONE);
				}
			}

			switch (holder.acc_type) {
			case ACC_TEXT_TYPE.NONE:
				view.setContentDescription(null);
				break;
			case ACC_TEXT_TYPE.PLAYING_SCREEN:
				view.setContentDescription(mResources
						.getString(R.string.gvideo_play_screen_acc_txt));
				break;
			case ACC_TEXT_TYPE.PLAYING_VIDEO:
				view.setContentDescription(mResources
						.getString(R.string.gvideo_play_video_acc_txt));
				break;
			case ACC_TEXT_TYPE.REQUEST_SCREEN:
				view.setContentDescription(mResources
						.getString(R.string.gvideo_request_screen_acc_txt));
				break;
			case ACC_TEXT_TYPE.REQUEST_VIDEO:
				view.setContentDescription(mResources
						.getString(R.string.gvideo_request_video_acc_txt));
				break;
			case ACC_TEXT_TYPE.SPEAKING:
				view.setContentDescription(mResources
						.getString(R.string.gvideo_speaking_acc_txt));
				break;
			default:
				view.setContentDescription(null);
				break;
			}

			return view;
		}
	}

	private class ACC_TEXT_TYPE {
		public static final int NONE = 0;
		public static final int REQUEST_VIDEO = 1;
		public static final int REQUEST_SCREEN = 2;
		public static final int REQUEST_MOVIE = 3;
		public static final int PLAYING_VIDEO = 4;
		public static final int PLAYING_SCREEN = 5;
		public static final int PLAYING_MOVIE = 6;
		public static final int SPEAKING = 7;
	}

	private class GET_VIEW_TYPE {
		public static final int NOMARL = 0;
		public static final int LAST_ITEM_IN_GROUP_MODE = 1;
	}

	private class Holder {
		private ImageView head = null;
		private ImageView cover = null;
		private ImageView speakingIcon = null;

		private TextView name = null;
		private int acc_type = ACC_TEXT_TYPE.NONE;
		private boolean speaking = false;
		private boolean isSelected = false;
	}

	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			Log.d(TAG, "OnPageChangeListenerSelected-->Position=" + position);
			mCurrentPage = position;
			setIndicatorSelected(position);
			notifyCurrentPageDataSetChanged();
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	};

	private OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (mMemberClickListener != null) {
				mMemberClickListener.onMembersHolderTouch();
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				return true;
			} else {
				return false;
			}

		}
	};

	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Log.e(TAG, "onItemClick INGridView POSITION=" + position + "ID="
					+ id);
			int index = (int) id;

			if (id == mMaxPageNum * MAX_PAGE_NUM - 1) {
				if (mMemberClickListener != null) {
					mMemberClickListener.onMembersClick(null, 0, true);
				}
				return;
			}
			ArrayList<MemberInfo> membersList = mQavsdkControl.getMemberList();
			MemberInfo info = membersList.get(index);
			boolean needRequest = setSelectedItem(info, index);
			int videoSrcType = AVConstants.VIDEO_SRC_UNKNOWN;

			if (!needRequest && !info.isShareSrc && !info.isVideoIn) {
				videoSrcType = AVConstants.VIDEO_SRC_UNKNOWN;
			} else {
				videoSrcType = mSelectedVideoSrcType;
			}

			if (mMemberClickListener != null && info.isVideoIn) {
				mMemberClickListener.onMembersClick(info.identifier, videoSrcType,
						needRequest);
			}

			if (!info.isShareSrc && !info.isVideoIn) {
				Animation animation = AnimationUtils.loadAnimation(
						getContext(), R.anim.qav_member_click_animition);
				view.startAnimation(animation);
			} else {
				((GridViewBaseAdapter) parent.getAdapter())
						.notifyDataSetChanged();
			}
		}
	};

	private void setIndicatorSelected(int index) {
		Log.e(TAG, "setIndicatorSelected-->index = " + index);
		if (mTotalPageNum <= 1) {
			if (mPageIndicator != null) {
				mPageIndicator.removeAllViews();
			}
			return;
		}
		int padding = mResources.getDimensionPixelSize(R.dimen.gaudio_padding);
		int count = mPageIndicator.getChildCount();
		if (count < mTotalPageNum) {
			int i = mTotalPageNum - count;
			for (int j = 0; j < i; j++) {
				ImageView indicator = new ImageView(getContext());
				indicator.setPadding(padding, padding, padding, padding);
				indicator
						.setImageResource(R.drawable.qav_gaudio_indicator_selector);
				mPageIndicator.addView(indicator, count + j);
			}
		}
		if (count > mTotalPageNum) {
			mPageIndicator.removeViews(0, count - mTotalPageNum);
		}
		for (int i = 0; i < mPageIndicator.getChildCount(); i++) {
			if (i == index) {
				mPageIndicator.getChildAt(i).setSelected(false);
			} else {
				mPageIndicator.getChildAt(i).setSelected(true);
			}
		}
	}

	private boolean setSelectedItem(MemberInfo item, int index) {
		if (item == null) {
			Log.e(TAG, "setSelectedItem-->Item is null");
			return false;
		}

		int src = AVConstants.VIDEO_SRC_UNKNOWN;
		if (item.identifier.equals(mSelectedIdentifier)) {
			// 已经被选中过，只处理video和share互切
			if (!(item.isVideoIn && item.isShareSrc)) {
				return false;
			}
			if (mSelectedVideoSrcType == AVConstants.VIDEO_SRC_CAMERA) {
				src = AVConstants.VIDEO_SRC_SHARESCREEN;
			} else if (mSelectedVideoSrcType == AVConstants.VIDEO_SRC_SHARESCREEN) {
				src = AVConstants.VIDEO_SRC_CAMERA;
			} else {
				Log.e(TAG, "WRONG TYPE OF VIDEOSRC");
				return false;
			}
		} else {
			if (item.isShareSrc) {
				src = AVConstants.VIDEO_SRC_SHARESCREEN;
			} else if (item.isVideoIn) {
				src = AVConstants.VIDEO_SRC_CAMERA;
			} else {
				src = AVConstants.VIDEO_SRC_UNKNOWN;
				return false;
			}
		}
		mSelectedIdentifier = item.identifier;
		mSelectedVideoSrcType = src;
		return true;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (!mAttached) {
			mAttached = true;
			IntentFilter filter = new IntentFilter();
			filter.addAction(Util.ACTION_MEMBER_CHANGE);
			getContext().registerReceiver(mBroadcastReceiver, filter);
			notifyDataSetChanged(mQavsdkControl.getMemberList());
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAttached) {
			getContext().unregisterReceiver(mBroadcastReceiver);
			mAttached = false;
		}
	}
}
