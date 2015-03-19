package com.kd.filmstrip.listeners;

import android.view.View;
import android.widget.AbsListView;

public abstract class OnVerticalScrollDirectionListener implements AbsListView.OnScrollListener {
	 
    /**
     * TODO: Make it changeable
     */
    public static final int DEFAULT_SCROLL_OFFSET_THRESHOLD = 6; /* In pixels */
 
    private ListenerArgs mListenerArgs;
    private int mPrevScrollY;
    private int mPrevFirstVisibleItem;
    private int mPrevFirstVisibleItemHeight;
 
    public OnVerticalScrollDirectionListener() {
        mListenerArgs = new ListenerArgs();
    }
 
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) { }
 
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mListenerArgs.listView = view;
        mListenerArgs.firstVisibleItem = firstVisibleItem;
        mListenerArgs.visibleItemCount = visibleItemCount;
        mListenerArgs.totalItemCount = totalItemCount;
 
        if (totalItemCount > visibleItemCount) {
            // The ListView overflows with items.
            // That means we can scroll up (or down) to see hidden items.
 
            // Get essential parameters to determine direction of vertical scrolling (in pixels)
            View firstVisibleItemView = view.getChildAt(0);
            int newScrollY = firstVisibleItemView.getTop();
            int firstVisibleItemHeight = firstVisibleItemView.getHeight();
 
            // mPrevScrollY need to be adjusted when firstVisibleItem has changed
            if (firstVisibleItem > mPrevFirstVisibleItem) {
                // A visible item at the top just disappeared from the visible area.
                // newScrollY now reflects the top of the next item.
                // So, we gotta compute mPrevScrollY of the next item too.
                mPrevScrollY += (firstVisibleItem - mPrevFirstVisibleItem) * mPrevFirstVisibleItemHeight;
            } else if (firstVisibleItem < mPrevFirstVisibleItem) {
                // A new visible item at the top just appeared.
                // Likewise, compute mPrevScrollY of the new visible item.
                mPrevScrollY += (firstVisibleItem - mPrevFirstVisibleItem) * firstVisibleItemHeight;
            }
 
            // Require that scrolling must be fast enough
            boolean isScrollChanged = (Math.abs(newScrollY - mPrevScrollY) >= DEFAULT_SCROLL_OFFSET_THRESHOLD);
            if (isScrollChanged) {
                mListenerArgs.prevScrollY = mPrevScrollY;
                mListenerArgs.newScrollY = newScrollY;
 
                onVerticalDirectionChanged(mListenerArgs);
            }
 
            // Prepare parameters for the next scrolling
            mPrevFirstVisibleItem = firstVisibleItem;
            mPrevScrollY = newScrollY;
            mPrevFirstVisibleItemHeight = firstVisibleItemHeight;
        } else {
            // The ListView is not scrollable.
            mListenerArgs.newScrollY = mListenerArgs.prevScrollY = 0;
            onVerticalDirectionChanged(mListenerArgs);
        }
    }
 
    public abstract void onVerticalDirectionChanged(ListenerArgs args);
 
    public static class ListenerArgs {
 
        public int firstVisibleItem;
        public int visibleItemCount;
        public int totalItemCount;
        public int prevScrollY; /* In pixels */
        public int newScrollY; /* In pixels */
        public AbsListView listView;
 
        public boolean isScrollingUp() {
            return newScrollY > prevScrollY;
        }
 
        public boolean isTopItemReached() {
            return firstVisibleItem == 0;
        }
 
        public boolean isScrollable() {
            return totalItemCount > visibleItemCount;
        }
    }
}