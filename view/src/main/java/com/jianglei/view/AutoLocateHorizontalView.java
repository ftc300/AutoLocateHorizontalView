package com.jianglei.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class AutoLocateHorizontalView extends RecyclerView {
    /**
     * 一个屏幕中显示多少个item，必须为奇数
     */
    private int itemCount = 7;
    private Paint mBackgroudPaint;
    private Paint mDashLinePaint;
    private  float mY  = 0;
    private Path mDashPath;
    /**
     * 初始时选中的位置
     */
    private int initPos = 0;
    private int deltaX;
    private WrapperAdapter wrapAdapter;
    private Adapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean isInit;
    private OnSelectedPositionChangedListener listener;
    private boolean isFirstPosChanged = true;        //刚初始化时是否触发位置改变的监听
    private int oldSelectedPos = initPos;   //记录上次选中的位置
    /**
     * 当前被选中的位置
     */
    private int selectPos = initPos;

    public AutoLocateHorizontalView(Context context) {
        super(context);
    }

    public AutoLocateHorizontalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoLocateHorizontalView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isInit) {
                    if (initPos >= adapter.getItemCount()) {
                        initPos = adapter.getItemCount() - 1;
                    }
                    if (isFirstPosChanged && listener != null) {
                        listener.selectedPositionChanged(initPos);
                    }
                    linearLayoutManager.scrollToPositionWithOffset(0, -initPos * (wrapAdapter.getItemWidth()));
                    isInit = false;
                }
            }
        });
        mBackgroudPaint = new Paint();
        mBackgroudPaint.setColor(Color.parseColor("#232225"));
        mDashLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDashLinePaint.setStyle(Paint.Style.STROKE);
        mDashLinePaint.setColor(Color.WHITE);
        mDashLinePaint.setStrokeWidth(1);
        PathEffect effects = new DashPathEffect(new float[]{4, 4}, 0);
        mDashLinePaint.setPathEffect(effects);
        mDashPath = new Path();

    }



    /**
     * 设置初始化时选中的位置,该方法必须在{@link AutoLocateHorizontalView#setAdapter(android.support.v7.widget.RecyclerView.Adapter) }之前调用
     *
     * @param initPos 初始位置，如果位置超过了item的数量则默认选中最后一项item
     */
    public void setInitPos(int initPos) {
        if(adapter != null){
            throw new RuntimeException("This method should be called before setAdapter()!");
        }
        this.initPos = initPos;
        selectPos = initPos;
        oldSelectedPos = initPos;
    }

    /**
     * 设置每次显示多少个item,该方法必须在{@link AutoLocateHorizontalView#setAdapter(android.support.v7.widget.RecyclerView.Adapter) }之前调用
     *
     * @param itemCount 必须为奇数，否则默认会设置成小于它的最大奇数
     */
    public void setItemCount(int itemCount) {
        if(adapter != null){
            throw new RuntimeException("This method should be called before setAdapter()!");
        }
        if (itemCount % 2 == 0) {
            this.itemCount = itemCount - 1;
        }else {
            this.itemCount = itemCount;
        }
    }

    /**
     * 删除item后偏移距离可能需要重新计算，从而保证selectPos的正确
     *
     * @param adapter
     */
    private void correctDeltax(Adapter adapter) {
        if (adapter.getItemCount() <= selectPos) {
            deltaX -= wrapAdapter.getItemWidth() * (selectPos - adapter.getItemCount() + 1);
        }
        calculateSelectedPos();
    }

    /**
     * 删除时选中的数据发生改变，要重新回调方法
     * @param startPos
     */
    private void reCallListenerWhenRemove(int startPos){
        if(startPos <= selectPos && listener != null){
            correctDeltax(adapter);
            listener.selectedPositionChanged(selectPos);
        }else{
            correctDeltax(adapter);
        }
    }

    /**
     * 添加数据时选中的数据发生改变，要重新回调方法
     * @param startPos
     */
    private void reCallListenerWhenAdd(int startPos){
        if(startPos <= selectPos && listener != null){
            listener.selectedPositionChanged(selectPos);
        }
    }

    /**
     * 当使用整体刷新时要重新回调方法
     */
    private void reCallListenerWhenChanged(){
        if( listener != null){
            listener.selectedPositionChanged(selectPos);
        }
    }
    @Override
    public void setAdapter(final Adapter adapter) {
        this.adapter = adapter;
        this.wrapAdapter = new WrapperAdapter(adapter, getContext(), itemCount);
        adapter.registerAdapterDataObserver(new AdapterDataObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                wrapAdapter.notifyDataSetChanged();
                reCallListenerWhenChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                wrapAdapter.notifyDataSetChanged();
                reCallListenerWhenAdd(positionStart);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                wrapAdapter.notifyDataSetChanged();
                reCallListenerWhenRemove(positionStart);
            }
        });
        deltaX = 0;
        if (linearLayoutManager == null) {
            linearLayoutManager = new LinearLayoutManager(getContext());
        }
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        super.setLayoutManager(linearLayoutManager);
        super.setAdapter(this.wrapAdapter);
        isInit = true;
    }


    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        //设置的item文字height为20dp
        c.drawRect(0,0,getWidth(),getHeight()- dp2px(getContext(),20), mBackgroudPaint);
        mDashPath.moveTo(0,300);
        mDashPath.lineTo(getWidth(),300);
        c.drawPath(mDashPath,mDashLinePaint);
    }

    public  void setY(float y) {
         mY = y;
        invalidate();
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (!(layout instanceof LinearLayoutManager)) {
            throw new IllegalStateException("The LayoutManager here must be LinearLayoutManager!");
        }
        this.linearLayoutManager = (LinearLayoutManager) layout;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        if (state == SCROLL_STATE_IDLE) {
            if (wrapAdapter == null) {
                return;
            }
            int itemWidth = wrapAdapter.getItemWidth();
            int headerFooterWidth = wrapAdapter.getHeaderFooterWidth();
            if (itemWidth == 0 || headerFooterWidth == 0) {
                //此时adapter还没有准备好，忽略此次调用
                return;
            }
            //超出上个item的位置
            int overLastPosOffset = deltaX % itemWidth;
            if (overLastPosOffset == 0) {
                //刚好处于一个item选中位置，无需滑动偏移纠正
            } else if (Math.abs(overLastPosOffset) <= itemWidth / 2) {
                scrollBy(-overLastPosOffset, 0);
            } else if (overLastPosOffset > 0) {
                scrollBy((itemWidth - overLastPosOffset), 0);
            } else {
                scrollBy(-(itemWidth + overLastPosOffset), 0);
            }
            calculateSelectedPos();
            //此处通知刷新是为了重新绘制之前被选中的位置以及刚刚被选中的位置
            wrapAdapter.notifyItemChanged(oldSelectedPos + 1);
            wrapAdapter.notifyItemChanged(selectPos + 1);
            oldSelectedPos = selectPos;
            if (listener != null) {
                listener.selectedPositionChanged(selectPos);
            }
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        deltaX += dx;
        calculateSelectedPos();
    }

    private void calculateSelectedPos() {
        int itemWidth = wrapAdapter.getItemWidth();
        if (deltaX > 0) {
            selectPos = (deltaX) / itemWidth + initPos;
        } else {
            selectPos = initPos + (deltaX) / itemWidth;
        }
    }

    class WrapperAdapter extends RecyclerView.Adapter {
        private Context context;
        private RecyclerView.Adapter adapter;
        private int itemCount;
        private static final int HEADER_FOOTER_TYPE = -1;
        private View itemView;
        /**
         * 头部或尾部的宽度
         */
        private int headerFooterWidth;

        /**
         * 每个item的宽度
         */
        private int itemWidth;

        public WrapperAdapter(Adapter adapter, Context context, int itemCount) {
            this.adapter = adapter;
            this.context = context;
            this.itemCount = itemCount;
            if (adapter instanceof IAutoLocateHorizontalView) {
                itemView = ((IAutoLocateHorizontalView) adapter).getItemView();
            } else {
                throw new RuntimeException(adapter.getClass().getSimpleName() + " should implements com.jianglei.view.AutoLocateHorizontalView.IAutoLocateHorizontalView !");
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == HEADER_FOOTER_TYPE) {
                View view = new View(context);
                headerFooterWidth = parent.getMeasuredWidth() / 2 - (parent.getMeasuredWidth() / itemCount) / 2;
                RecyclerView.LayoutParams params = new LayoutParams(headerFooterWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(params);
                return new HeaderFooterViewHolder(view);
            }
            ViewHolder holder = adapter.onCreateViewHolder(parent, viewType);
            itemView = ((IAutoLocateHorizontalView) adapter).getItemView();
            int width = parent.getMeasuredWidth() / itemCount;
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            if (params != null) {
                params.width = width;
                itemWidth = width;
                itemView.setLayoutParams(params);
            }
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveItemToCenter(view);
                }
            });
            return holder;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (!isHeaderOrFooter(position)) {
                adapter.onBindViewHolder(holder, position - 1);
                if (selectPos == position - 1) {
                    ((IAutoLocateHorizontalView) adapter).onViewSelected(true, position - 1, holder,itemWidth);
                } else {
                    ((IAutoLocateHorizontalView) adapter).onViewSelected(false, position - 1, holder,itemWidth);
                }
            }
        }


        @Override
        public int getItemCount() {
            return adapter.getItemCount() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == getItemCount() - 1) {
                return HEADER_FOOTER_TYPE;
            }
            return adapter.getItemViewType(position - 1);
        }



        private boolean isHeaderOrFooter(int pos) {
            if (pos == 0 || pos == getItemCount() - 1) {
                return true;
            }
            return false;
        }

        public int getHeaderFooterWidth() {
            return headerFooterWidth;
        }

        public int getItemWidth() {
            return itemWidth;
        }

        /**
         * 将item尽量移至中央位置
         * @param itemView
         */
        private void moveItemToCenter(View itemView)
        {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;
            int[] locations = new int[2];
            itemView.getLocationInWindow(locations);
            int rbWidth = itemView.getWidth();
            smoothScrollBy((locations[0] + rbWidth / 2 - screenWidth / 2),0);
        }

        class HeaderFooterViewHolder extends RecyclerView.ViewHolder {

            HeaderFooterViewHolder(View itemView) {
                super(itemView);
            }
        }
    }


    public interface IAutoLocateHorizontalView {
        /**
         * 获取item的根布局
         */
        View getItemView();

        /**
         * 当item被选中时会触发这个回调，可以修改被选中时的样式
         *
         * @param isSelected 是否被选中
         * @param pos        当前view的位置
         * @param holder
         * @param itemWidth 当前整个item的宽度
         */
        void onViewSelected(boolean isSelected, int pos, ViewHolder holder,int itemWidth);
    }

    /***
     * 选中位置改变时的监听
     */
    public interface OnSelectedPositionChangedListener {
        void selectedPositionChanged(int pos);
    }

    public void setOnSelectedPositionChangedListener(OnSelectedPositionChangedListener listener) {
        this.listener = listener;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private   int dp2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }


}
