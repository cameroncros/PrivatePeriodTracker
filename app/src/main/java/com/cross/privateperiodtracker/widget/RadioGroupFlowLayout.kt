package com.cross.privateperiodtracker.widget

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillValue
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.nex3z.flowlayout.FlowLayout

class RadioGroupFlowLayout : FlowLayout {
    /**
     *
     * Returns the identifier of the selected radio button in this group.
     * Upon empty selection, the returned value is -1.
     *
     * @return the unique id of the selected radio button in this group
     * @attr ref android.R.styleable#ConstraintRadioGroup_checkedButton
     * @see .check
     * @see .clearCheck
     */
    // holds the checked id; the selection is empty by default
    @get:IdRes
    var checkedRadioButtonId = -1
        private set

    // tracks children radio buttons checked state
    //@UnsupportedAppUsage
    private var mChildOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null

    // when true, mOnCheckedChangeListener discards events
    private var mProtectFromCheckedChange = false

    // @UnsupportedAppUsage
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null
    private var mPassThroughListener: PassThroughHierarchyChangeListener? = null

    // Indicates whether the child was set from resources or dynamically, so it can be used
    // to sanitize autofill requests.
    private var mInitialCheckedId = NO_ID

    /**
     * {@inheritDoc}
     */
    constructor(context: Context?) : super(context!!) {
        init()
    }

    /**
     * {@inheritDoc}
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        // ConstraintRadioGroup is important by default, unless app developer overrode attribute.
        /*if (getImportantForAutofill() == IMPORTANT_FOR_AUTOFILL_AUTO) {
            setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_YES);
        }*/

//        // retrieve selected radio button as requested by the user in the
//        // XML layout file
//        val attributes = context.obtainStyledAttributes(
//            attrs, R.styleable.ConstraintRadioGroup, R.attr.radioButtonStyle, 0
//        )

//        //For debugging purposes
//        /*saveAttributeDataForStyleable(context, R.styleable.ConstraintRadioGroup,
//                attrs, attributes, R.attr.radioButtonStyle, 0);*/
//        val value = attributes.getResourceId(R.styleable.ConstraintRadioGroup_checkedButton, NO_ID)
//        if (value != NO_ID) {
//            checkedRadioButtonId = value
//            mInitialCheckedId = value
//        }
//        attributes.recycle()
        init()
    }

    private fun init() {
        mChildOnCheckedChangeListener = CheckedStateTracker()
        mPassThroughListener = PassThroughHierarchyChangeListener()
        super.setOnHierarchyChangeListener(mPassThroughListener)
    }

    /**
     * {@inheritDoc}
     */
    override fun setOnHierarchyChangeListener(listener: OnHierarchyChangeListener) {
        // the user listener is delegated to our pass-through listener
        mPassThroughListener!!.mOnHierarchyChangeListener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun onFinishInflate() {
        super.onFinishInflate()

        // checks the appropriate radio button as requested in the XML file
        if (checkedRadioButtonId != -1) {
            mProtectFromCheckedChange = true
            setCheckedStateForView(checkedRadioButtonId, true)
            mProtectFromCheckedChange = false
            setCheckedId(checkedRadioButtonId)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is RadioButton) {
            val button = child
            if (button.isChecked) {
                mProtectFromCheckedChange = true
                if (checkedRadioButtonId != -1) {
                    setCheckedStateForView(checkedRadioButtonId, false)
                }
                mProtectFromCheckedChange = false
                setCheckedId(button.id)
            }
        }
        super.addView(child, index, params)
    }

    /**
     *
     * Sets the selection to the radio button whose identifier is passed in
     * parameter. Using -1 as the selection identifier clears the selection;
     * such an operation is equivalent to invoking [.clearCheck].
     *
     * @param id the unique id of the radio button to select in this group
     * @see .getCheckedRadioButtonId
     * @see .clearCheck
     */
    fun check(@IdRes id: Int) {
        // don't even bother
        if (id != -1 && id == checkedRadioButtonId) {
            return
        }
        if (checkedRadioButtonId != -1) {
            setCheckedStateForView(checkedRadioButtonId, false)
        }
        if (id != -1) {
            setCheckedStateForView(id, true)
        }
        setCheckedId(id)
    }

    private fun setCheckedId(@IdRes id: Int) {
        //boolean changed = id != mCheckedId;
        checkedRadioButtonId = id
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener!!.onCheckedChanged(this, checkedRadioButtonId)
        }
        /*if (changed) {
            final AutofillManager afm = mContext.getSystemService(AutofillManager.class);
            if (afm != null) {
                afm.notifyValueChanged(this);
            }
        }*/
    }

    private fun setCheckedStateForView(viewId: Int, checked: Boolean) {
        val checkedView = findViewById<View>(viewId)
        if (checkedView != null && checkedView is RadioButton) {
            checkedView.isChecked = checked
        }
    }

    /**
     *
     * Clears the selection. When the selection is cleared, no radio button
     * in this group is selected and [.getCheckedRadioButtonId] returns
     * null.
     *
     * @see .check
     * @see .getCheckedRadioButtonId
     */
    fun clearCheck() {
        check(-1)
    }

    /**
     *
     * Register a callback to be invoked when the checked radio button
     * changes in this group.
     *
     * @param listener the callback to call on checked state change
     */
    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        mOnCheckedChangeListener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(context, attrs)
    }

    /**
     * {@inheritDoc}
     */
    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): ConstraintLayout.LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun getAccessibilityClassName(): CharSequence {
        return RadioGroupFlowLayout::class.java.name
    }

    /**
     * @hide
     */
    /*@RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onProvideStructure(@NonNull ViewStructure structure,
                                      int viewFor, int flags) {
        onProvideStructure(structure, viewFor, flags);

        if (viewFor == VIEW_STRUCTURE_FOR_AUTOFILL) {
            structure.setDataIsSensitive(mCheckedId != mInitialCheckedId);
        }
    }*/
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun autofill(value: AutofillValue) {
        if (!isEnabled) return
        if (!value.isList) {
            Log.w(
                LOG_TAG,
                "$value could not be autofilled into $this"
            )
            return
        }
        val index = value.listValue
        val child = getChildAt(index)
        if (child == null) {
            Log.w(
                VIEW_LOG_TAG,
                "ConstraintRadioGroup.autoFill(): no child with index $index"
            )
            return
        }
        check(child.id)
    }

    override fun getAutofillType(): Int {
        return if (isEnabled) AUTOFILL_TYPE_LIST else AUTOFILL_TYPE_NONE
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun getAutofillValue(): AutofillValue? {
        if (!isEnabled) return null
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.id == checkedRadioButtonId) {
                return AutofillValue.forList(i)
            }
        }
        return null
    }

    /**
     *
     * Interface definition for a callback to be invoked when the checked
     * radio button changed in this group.
     */
    interface OnCheckedChangeListener {
        /**
         *
         * Called when the checked radio button has changed. When the
         * selection is cleared, checkedId is -1.
         *
         * @param group     the group in which the checked radio button has changed
         * @param checkedId the unique identifier of the newly checked radio button
         */
        fun onCheckedChanged(group: RadioGroupFlowLayout?, @IdRes checkedId: Int)
    }

    /**
     *
     * This set of layout parameters defaults the width and the height of
     * the children to [.WRAP_CONTENT] when they are not specified in the
     * XML file. Otherwise, this class ussed the value read from the XML file.
     */
    class LayoutParams : ConstraintLayout.LayoutParams {
        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs)
        constructor(w: Int, h: Int) : super(w, h)
        constructor(p: ViewGroup.LayoutParams?) : super(p)
        constructor(source: MarginLayoutParams?) : super(source)

        /**
         *
         * Fixes the child's width to
         * [android.view.ViewGroup.LayoutParams.WRAP_CONTENT] and the child's
         * height to  [android.view.ViewGroup.LayoutParams.WRAP_CONTENT]
         * when not specified in the XML file.
         *
         * @param a          the styled attributes set
         * @param widthAttr  the width attribute to fetch
         * @param heightAttr the height attribute to fetch
         */
        override fun setBaseAttributes(
            a: TypedArray,
            widthAttr: Int, heightAttr: Int
        ) {
            width = if (a.hasValue(widthAttr)) {
                a.getLayoutDimension(widthAttr, "layout_width")
            } else {
                WRAP_CONTENT
            }
            height = if (a.hasValue(heightAttr)) {
                a.getLayoutDimension(heightAttr, "layout_height")
            } else {
                WRAP_CONTENT
            }
        }
    }

    private inner class CheckedStateTracker : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            // prevents from infinite recursion
            if (mProtectFromCheckedChange) {
                return
            }
            mProtectFromCheckedChange = true
            if (checkedRadioButtonId != -1) {
                setCheckedStateForView(checkedRadioButtonId, false)
            }
            mProtectFromCheckedChange = false
            val id = buttonView.id
            setCheckedId(id)
        }
    }

    /**
     *
     * A pass-through listener acts upon the events and dispatches them
     * to another listener. This allows the table layout to set its own internal
     * hierarchy change listener without preventing the user to setup his.
     */
    private inner class PassThroughHierarchyChangeListener :
        OnHierarchyChangeListener {
        var mOnHierarchyChangeListener: OnHierarchyChangeListener? = null

        /**
         * {@inheritDoc}
         */
        override fun onChildViewAdded(parent: View, child: View) {
            if (parent === this@RadioGroupFlowLayout && child is RadioButton) {
                var id = child.getId()
                // generates an id if it's missing
                if (id == NO_ID) {
                    id = generateViewId()
                    child.setId(id)
                }
                child.setOnCheckedChangeListener(
                    mChildOnCheckedChangeListener
                )
            }
            mOnHierarchyChangeListener?.onChildViewAdded(parent, child)
        }

        /**
         * {@inheritDoc}
         */
        override fun onChildViewRemoved(parent: View, child: View) {
            if (parent === this@RadioGroupFlowLayout && child is RadioButton) {
                child.setOnCheckedChangeListener(null)
            }
            mOnHierarchyChangeListener?.onChildViewRemoved(parent, child)
        }
    }

    companion object {
        private val LOG_TAG = RadioGroupFlowLayout::class.java.simpleName
    }
}