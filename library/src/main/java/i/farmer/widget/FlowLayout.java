package i.farmer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.text.TextUtilsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author i-farmer
 * @created-time 2020-11-19 10:45:07
 * @description 流式布局
 * <p>
 * 子组件的margin设置将会失效，采用spacingHorizontal、spacingVertical
 * 支持左右对齐方式flowGravity，默认 LTR=左部对齐 RTL=右部对齐
 * 支持一行上下对齐方式rowGravity，默认顶部对齐
 */
public class FlowLayout extends ViewGroup {
    private final int FLOW_GRAVITY_LEFT = 1;            // 左右对齐方式
    private final int FLOW_GRAVITY_CENTER = 2;
    private final int FLOW_GRAVITY_RIGHT = 3;

    private final int ROW_GRAVITY_TOP = 1;              // 行上下对齐方式
    private final int ROW_GRAVITY_CENTER = 2;
    private final int ROW_GRAVITY_BOTTOM = 3;

    private int spacingHorizontal = 0;                  // item左右间距
    private int spacingVertical = 0;                    // item上下间距
    private int flowGravity;
    private int rowGravity = ROW_GRAVITY_TOP;
    private int layoutDirection;                        // 布局方向

    private List<Row> lines = new ArrayList<>();        // 所有行信息
    private SparseArray<Rect> allItemFrames = new SparseArray<>();  // 所有item的位置信息

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        layoutDirection = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault());
        // 默认对齐方式
        flowGravity = layoutDirection == LayoutDirection.RTL ? FLOW_GRAVITY_RIGHT : FLOW_GRAVITY_LEFT;
        if (null != attrs) {
            TypedArray typedArray = null;
            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
                flowGravity = typedArray.getInt(R.styleable.FlowLayout_flowGravity, flowGravity);
                rowGravity = typedArray.getInt(R.styleable.FlowLayout_rowGravity, rowGravity);
                spacingHorizontal = typedArray.getDimensionPixelOffset(R.styleable.FlowLayout_spacingHorizontal, spacingHorizontal);
                spacingVertical = typedArray.getDimensionPixelOffset(R.styleable.FlowLayout_spacingVertical, spacingVertical);
            } catch (Exception ex) {

            } finally {
                if (null != typedArray) {
                    typedArray.recycle();
                    typedArray = null;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        lines.clear();
        allItemFrames.clear();
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        int measureWidth = 0;     // 根据位置计算出来的宽高
        int measureHeight = 0;
        int useWidth = sizeWidth - getPaddingLeft() - getPaddingRight();
        int rowWidth = 0;         // 行宽
        int rowTop = 0;
        int maxRowHeight = 0;
        Row row = null;     // 行
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() == View.GONE) {
                // 不显示
                continue;
            }
            // 进行测量，获得宽高，忽略margin
            measureChild(childAt, widthMeasureSpec, heightMeasureSpec);
            int childAtWidth = childAt.getMeasuredWidth();
            int childAtHeight = childAt.getMeasuredHeight();
            int childLeft = rowWidth + (rowWidth > 0 ? spacingHorizontal : 0);   // 加上间隔
            if (childLeft + childAtWidth > useWidth) {
                // 超出需要换行
                addRow(row);
                measureWidth = Math.max(measureWidth, rowWidth);    // 计算总宽度
                measureHeight = rowTop + maxRowHeight;              // 计算总高度
                rowTop += maxRowHeight + spacingVertical;  // 加上间隔
                rowWidth = 0;       // 清零
                childLeft = 0;
                maxRowHeight = 0;
                row = null;
            }
            Rect frame = allItemFrames.get(i);
            if (null == frame) {
                frame = new Rect();
            }
            frame.set(childLeft, rowTop, childLeft + childAtWidth, rowTop + childAtHeight);
            allItemFrames.put(i, frame);    // 更新缓存
            rowWidth = frame.right;         // 更新当前行宽
            maxRowHeight = Math.max(maxRowHeight, childAtHeight);   // 更新行高
            if (null == row) {
                row = new Row();
                row.setTop(rowTop);
            }
            row.addChild(i);
            row.setSize(rowWidth, maxRowHeight);

        }
        // 最后收尾
        if (null != row) {
            addRow(row);
            measureWidth = Math.max(measureWidth, rowWidth);    // 计算总宽度
            measureHeight = rowTop + maxRowHeight;              // 计算总高度
        }
        int measuredWidth = modeWidth == MeasureSpec.EXACTLY ? sizeWidth : measureWidth + getPaddingLeft() + getPaddingRight();
        int measuredHeight = modeHeight == MeasureSpec.EXACTLY ? sizeHeight : measureHeight + getPaddingTop() + getPaddingBottom();
        if (measuredWidth < getMinimumWidth()) {
            measuredWidth = getMinimumWidth();
        }
        if (measuredHeight < getMinimumHeight()) {
            measuredHeight = getMinimumHeight();
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
        if (measuredWidth == 0 || measuredHeight == 0) {
            // 如果计算出来的宽高为0，则隐藏，避免margin照成的空白
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    private void addRow(Row row) {
        // 增加行记录
        if (null == row) {
            return;
        }
        if (rowGravity == ROW_GRAVITY_CENTER || rowGravity == ROW_GRAVITY_BOTTOM) {
            for (Integer index : row.children) {
                Rect item = allItemFrames.get(index);
                float itemHeight = item.bottom - item.top;  // Item 高度
                if (rowGravity == ROW_GRAVITY_CENTER) {
                    // 上下居中对齐
                    float newTop = row.top + (row.height - itemHeight) / 2;
                    if (item.top < newTop) {
                        // 更新
                        item.set(item.left, (int) newTop, item.right, (int) (newTop + itemHeight));
                    }
                } else if (rowGravity == ROW_GRAVITY_BOTTOM) {
                    // 底部对其
                    float bottom = row.top + row.height;
                    if (item.bottom != bottom) {
                        // 更新
                        item.set(item.left, (int) (bottom - itemHeight), item.right, (int) bottom);
                    }
                }
            }
        }
        lines.add(row);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();     // 计算位置的时候没有考虑 padding，这里需要加上
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        for (int i = 0; i < lines.size(); i++) {
            Row row = lines.get(i);
            for (int j = 0; j < row.children.size(); j++) {
                int index = row.children.get(j);
                Rect frame = allItemFrames.get(index);
                // 根据对其方式 调整left、right
                int left;
                int measuredWidth = r - l;  // 宽度
                if (layoutDirection == LayoutDirection.RTL) {
                    // 反向排列
                    // 此处之所以使用r - l / measuredWidth，是因为childView的layout只需要计算在父容器中的位置坐标
                    left = measuredWidth - paddingRight - frame.right; // 默认 居右对齐
                    if (flowGravity == FLOW_GRAVITY_CENTER) {
                        // 左右居中对齐，往左偏移剩余空间的一半
                        left = left - (int) ((measuredWidth - paddingLeft - paddingRight - row.width) / 2);
                    } else if (flowGravity == FLOW_GRAVITY_LEFT) {
                        // 左右居左对齐，往左偏移剩余空间
                        left = left - (int) (measuredWidth - paddingLeft - paddingRight - row.width);
                    }
                } else {
                    // 正向排列
                    left = paddingLeft + frame.left;    // 默认 居左对齐
                    if (flowGravity == FLOW_GRAVITY_CENTER) {
                        // 左右居中对齐，往右偏移剩余空间的一半
                        left += (int) ((measuredWidth - paddingLeft - paddingRight - row.width) / 2);
                    } else if (flowGravity == FLOW_GRAVITY_RIGHT) {
                        // 左右居右对齐，往右偏移剩余空间
                        left += (int) (measuredWidth - paddingLeft - paddingRight - row.width);
                    }
                }
                int top = paddingTop + frame.top;
                int bottom = paddingTop + frame.bottom;
                int right = left + frame.right - frame.left;
                getChildAt(index).layout(left, top, right, bottom);
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    public void setSpacingHorizontal(int spacingHorizontal) {
        this.spacingHorizontal = spacingHorizontal;
    }

    public void setSpacingVertical(int spacingVertical) {
        this.spacingVertical = spacingVertical;
    }

    public void setFlowGravity(int flowGravity) {
        this.flowGravity = flowGravity;
    }

    public void setRowGravity(int rowGravity) {
        this.rowGravity = rowGravity;
    }

    class Row {
        float top;      // 每一行的top位置
        float height;   // 每一行的最大高度
        float width;    // 每一行的最大宽度
        List<Integer> children = new ArrayList<>();

        void addChild(Integer childIndex) {
            children.add(childIndex);
        }

        void setTop(float top) {
            this.top = top;
        }

        void setSize(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }
}
