package com.hootsuite.nachos.chip;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.ChipDrawable;
import android.support.v4.content.ContextCompat;
import android.text.style.ImageSpan;

import com.hootsuite.nachos.R;

/**
 * A Span that displays text and an optional icon inside of a material design chip. The chip's dimensions, colors etc. can be extensively customized
 * through the various setter methods available in this class.
 *     The basic structure of the chip is the following:
 * For chips with the icon on right:
 * <pre>
 *
 *                                  (chip vertical spacing / 2)
 *                  ----------------------------------------------------------
 *                |                                                            |
 * (left margin)  |  (padding edge)   text   (padding between image)   icon    |   (right margin)
 *                |                                                            |
 *                  ----------------------------------------------------------
 *                                  (chip vertical spacing / 2)
 *
 *      </pre>
 * For chips with the icon on the left (see {@link #setShowIconOnLeft(boolean)}):
 * <pre>
 *
 *                                  (chip vertical spacing / 2)
 *                  ----------------------------------------------------------
 *                |                                                            |
 * (left margin)  |   icon  (padding between image)   text   (padding edge)    |   (right margin)
 *                |                                                            |
 *                  ----------------------------------------------------------
 *                                  (chip vertical spacing / 2)
 *     </pre>
 */
public class ChipSpan extends ImageSpan implements Chip {

    private static final float SCALE_PERCENT_OF_CHIP_HEIGHT = 0.70f;
    private static final boolean ICON_ON_LEFT_DEFAULT = true;

    private int[] mStateSet = new int[]{};

    private String mEllipsis;

    private int mTextColor;
    private int mCornerRadius = -1;
    private int mIconBackgroundColor;

    private int mTextSize = -1;
    private int mPaddingEdgePx;
    private int mPaddingBetweenImagePx;
    private int mLeftMarginPx;
    private int mRightMarginPx;
    private int mMaxAvailableWidth = -1;


    private Drawable mIcon;
    private boolean mShowIconOnLeft = ICON_ON_LEFT_DEFAULT;
    private ChipDrawable mChipDrawable;

    private int mChipVerticalSpacing = 0;
    private int mChipHeight = -1;
    private int mChipWidth = -1;
    private int mIconWidth;

    private int mCachedSize = -1;

    private Object mData;

    /**
     * Constructs a new ChipSpan.
     *
     * @param context a {@link Context} that will be used to retrieve default configurations from resource files
     * @param text    the text for the ChipSpan to display
     * @param icon    an optional icon (can be {@code null}) for the ChipSpan to display
     */
    public ChipSpan(@NonNull Context context, @NonNull CharSequence text, @Nullable Drawable icon, Object data) {
        super(ChipDrawable.createFromResource(context, R.xml.chip));
        mChipDrawable = (ChipDrawable) getDrawable();
        mChipDrawable.setChipText(text);
        mChipDrawable.setBounds(0, 0, mChipDrawable.getIntrinsicWidth(), mChipDrawable.getIntrinsicHeight());
        mIcon = icon;

        mEllipsis = context.getString(R.string.chip_ellipsis);

        mTextColor = ContextCompat.getColor(context, R.color.chip_default_text_color);
        mIconBackgroundColor = ContextCompat.getColor(context, R.color.chip_default_icon_background_color);

        Resources resources = context.getResources();
        mPaddingEdgePx = resources.getDimensionPixelSize(R.dimen.chip_default_padding_edge);
        mPaddingBetweenImagePx = resources.getDimensionPixelSize(R.dimen.chip_default_padding_between_image);
        mLeftMarginPx = resources.getDimensionPixelSize(R.dimen.chip_default_left_margin);
        mRightMarginPx = resources.getDimensionPixelSize(R.dimen.chip_default_right_margin);

        mData = data;
    }

    /**
     * Copy constructor to recreate a ChipSpan from an existing one
     *
     * @param context  a {@link Context} that will be used to retrieve default configurations from resource files
     * @param chipSpan the ChipSpan to copy
     */
    public ChipSpan(@NonNull Context context, @NonNull ChipSpan chipSpan) {
        this(context, chipSpan.getText(), chipSpan.getDrawable(), chipSpan.getData());

        // mDefaultBackgroundColor = chipSpan.mDefaultBackgroundColor;
        mTextColor = chipSpan.mTextColor;
        mIconBackgroundColor = chipSpan.mIconBackgroundColor;
        mCornerRadius = chipSpan.mCornerRadius;

        mTextSize = chipSpan.mTextSize;
        mPaddingEdgePx = chipSpan.mPaddingEdgePx;
        mPaddingBetweenImagePx = chipSpan.mPaddingBetweenImagePx;
        mLeftMarginPx = chipSpan.mLeftMarginPx;
        mRightMarginPx = chipSpan.mRightMarginPx;
        mMaxAvailableWidth = chipSpan.mMaxAvailableWidth;

        mShowIconOnLeft = chipSpan.mShowIconOnLeft;

        mChipVerticalSpacing = chipSpan.mChipVerticalSpacing;
        mChipHeight = chipSpan.mChipHeight;

        mStateSet = chipSpan.mStateSet;
    }

    @Override
    public Object getData() {
        return mData;
    }

    /**
     * Sets the height of the chip. This height should not include any extra spacing (for extra vertical spacing call {@link #setChipVerticalSpacing(int)}).
     * The background of the chip will fill the full height provided here. If this method is never called, the chip will have the height of one full line
     * of text by default. If {@code -1} is passed here, the chip will revert to this default behavior.
     *
     * @param chipHeight the height to set in pixels
     */
    public void setChipHeight(int chipHeight) {
        mChipDrawable.setChipMinHeight(chipHeight);
    }

    /**
     * Sets the vertical spacing to include in between chips. Half of the value set here will be placed as empty space above the chip and half the value
     * will be placed as empty space below the chip. Therefore chips on consecutive lines will have the full value as vertical space in between them.
     * This spacing is achieved by adjusting the font metrics used by the text view containing these chips; however it does not come into effect until
     * at least one chip is created. Note that vertical spacing is dependent on having a fixed chip height (set in {@link #setChipHeight(int)}). If a
     * height is not specified in that method, the value set here will be ignored.
     *
     * @param chipVerticalSpacing the vertical spacing to set in pixels
     */
    public void setChipVerticalSpacing(int chipVerticalSpacing) {
        mChipVerticalSpacing = chipVerticalSpacing;
    }

    // /**
    //  * Sets the font size for the chip's text. If this method is never called, the chip text will have the same font size as the text in the TextView
    //  * containing this chip by default. If {@code -1} is passed here, the chip will revert to this default behavior.
    //  *
    //  * @param size the font size to set in pixels
    //  */
    // public void setTextSize(int size) {
    //     mTextSize = size;
    //     invalidateCachedSize();
    // }
    //
    // /**
    //  * Sets the color for the chip's text.
    //  *
    //  * @param color the color to set (as a hexadecimal number in the form 0xAARRGGBB)
    //  */
    // public void setTextColor(int color) {
    //     mTextColor = color;
    // }

    // /**
    //  * Sets where the icon (if an icon was provided in the constructor) will appear.
    //  *
    //  * @param showIconOnLeft if true, the icon will appear on the left, otherwise the icon will appear on the right
    //  */
    // public void setShowIconOnLeft(boolean showIconOnLeft) {
    //     this.mShowIconOnLeft = showIconOnLeft;
    //     invalidateCachedSize();
    // }

    /**
     * Sets the left margin. This margin will appear as empty space (it will not share the chip's background color) to the left of the chip.
     *
     * @param leftMarginPx the left margin to set in pixels
     */
    public void setLeftMargin(int leftMarginPx) {
        mChipDrawable.setChipStartPadding(leftMarginPx);
    }

    /**
     * Sets the right margin. This margin will appear as empty space (it will not share the chip's background color) to the right of the chip.
     *
     * @param rightMarginPx the right margin to set in pixels
     */
    public void setRightMargin(int rightMarginPx) {
        mChipDrawable.setChipEndPadding(rightMarginPx);
    }

    /**
     * Sets the background color. To configure which color in the {@link ColorStateList} is shown you can call {@link #setState(int[])}.
     * Passing {@code null} here will cause the chip to revert to it's default background.
     *
     * @param backgroundColor a {@link ColorStateList} containing backgrounds for different states.
     * @see #setState(int[])
     */
    public void setBackgroundColor(@Nullable ColorStateList backgroundColor) {
        mChipDrawable.setChipBackgroundColor(backgroundColor);
    }

    /**
     * Sets the chip background corner radius.
     *
     * @param cornerRadius The corner radius value, in pixels.
     */
    public void setCornerRadius(@Dimension int cornerRadius) {
        mChipDrawable.setChipCornerRadius(cornerRadius);
    }


    public void setMaxAvailableWidth(int maxAvailableWidth) {
        mMaxAvailableWidth = maxAvailableWidth;
        // invalidateCachedSize();
    }

    /**
     * Sets the UI state. This state will be reflected in the background color drawn for the chip.
     *
     * @param stateSet one of the state constants in {@link android.view.View}
     * @see #setBackgroundColor(ColorStateList)
     */
    @Override
    public void setState(int[] stateSet) {
        mChipDrawable.setState(stateSet);
        mChipDrawable.invalidateSelf();
    }

    @Override
    public CharSequence getText() {
        return mChipDrawable.getChipText();
    }

    @Override
    public int getWidth() {
        Rect rect = mChipDrawable.getBounds();
        return rect.right - rect.left;
    }

    @Override
    public String toString() {
        return mChipDrawable.getChipText().toString();
    }
}
