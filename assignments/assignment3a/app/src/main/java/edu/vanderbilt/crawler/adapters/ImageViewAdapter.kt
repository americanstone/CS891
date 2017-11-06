package edu.vanderbilt.crawler.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import edu.vanderbilt.crawler.R
import edu.vanderbilt.crawler.extensions.asyncLoad
import edu.vanderbilt.crawler.extensions.ctx
import edu.vanderbilt.crawler.ui.adapters.MultiSelectAdapter
import edu.vanderbilt.crawler.ui.adapters.SelectableViewHolder
import edu.vanderbilt.crawler.utils.KtLogger
import edu.vanderbilt.crawler.viewmodels.Resource
import org.jetbrains.anko.find

/**
 * Adapter that displays an image URL and its associated image.
 */
class ImageViewAdapter(context: Context,
                       list: MutableList<Resource> = mutableListOf(),
                       val gridLayout: Boolean = false,
                       onSelectionListener: OnSelectionListener? = null)
    : MultiSelectAdapter<Resource, ImageViewAdapter.Holder>(
        context, list, onSelectionListener), KtLogger {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(
                if (gridLayout) R.layout.image_grid_item else R.layout.image_list_item,
                parent,
                false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
        super.onBindViewHolder(holder, position)
    }

    /**
     * Updates the state of an existing item.
     */
    override fun replaceItem(position: Int, item: Resource) {
        val oldItem = getItem(position)
        validateStateChange(oldItem.state, item.state)

        if (!DiffUtilCallback.areItemsTheSame(oldItem, item)) {
            throw Exception("Replace items must be logically equal")
        }

        if (!DiffUtilCallback.areContentsTheSame(oldItem, item)) {
            super.replaceItem(position, item)
        }
    }

    fun updateItems(newItems: List<Resource>) {
        val oldItems = items
        val result = DiffUtil.calculateDiff(
                DiffUtilCallback(oldItems, newItems))
        setItems(newItems, false)
        result.dispatchUpdatesTo(this)
    }

    /**
     * State change verification. Expected transitions are:
     *   [CREATE|LOAD] -> [READ|WRITE|PROCESS|DOWNLOAD] -> CLOSE
     */
    private fun validateStateChange(oldState: Resource.State,
                                    newState: Resource.State): Boolean {
        when (newState) {
            Resource.State.CREATE, Resource.State.LOAD ->
                error("Illegal state change " +
                      "${oldState.name} -> ${newState.name}")

            Resource.State.PROCESS,
            Resource.State.READ,
            Resource.State.WRITE,
            Resource.State.DOWNLOAD ->
                if (oldState != Resource.State.CREATE) {
                    error("Illegal state change " +
                          "${oldState.name} -> ${newState.name}")
                }

            Resource.State.CLOSE ->
                if (oldState == Resource.State.CLOSE) {
                    error("Illegal state change " +
                          "${oldState.name} -> ${newState.name}")
                }
        }

        return true
    }

    /**
     * Sets all items to CLOSE state so that transient
     * gif animations are stopped.
     */
    fun crawlStopped() {
        items.forEachIndexed { pos, item ->
            if (item.state != Resource.State.CLOSE) {
                super.replaceItem(pos, item.copy(state = Resource.State.CLOSE))
            }
        }
    }

    inner class Holder(val view: View) : SelectableViewHolder(view) {
        private lateinit var url: String
        private val textView: TextView? = if (gridLayout) null else view.find<TextView>(R.id.textView)
        private val imageView = view.find<ImageView>(R.id.imageView)
        private val imageSize = view.find<TextView>(R.id.imageSize)
        private val progressBar = view.find<ProgressBar>(R.id.progressBar)
        private val indeterminateProgressBar = view.find<ProgressBar>(R.id.indeterminateProgressBar)

        fun bind(resource: Resource) {
            url = resource.url
            textView?.text = url
            imageSize.text = view.ctx.getString(
                    R.string.image_size_format, resource.size / 1e3)
            progressBar.max = 100
            setImage(resource)

            when (resource.state) {
                Resource.State.LOAD -> hideProgressBar()
                Resource.State.DOWNLOAD -> setProgress(resource.progress, Color.RED)
                Resource.State.CREATE -> hideProgressBar()
                Resource.State.WRITE -> setProgress(resource.progress, Color.BLUE)
                Resource.State.READ -> setProgress(resource.progress, Color.GREEN)
                Resource.State.PROCESS -> setProgress(resource.progress, Color.CYAN)
                Resource.State.CLOSE -> hideProgressBar()
            }
        }

        private fun setProgress(progress: Float,
                                color: Int,
                                indeterminate: Boolean = false) {
            with(if (indeterminate) indeterminateProgressBar else progressBar) {
                progressDrawable.setColorFilter(
                        color, android.graphics.PorterDuff.Mode.SRC_IN)
                this.progress = (progress * max).toInt()
                visibility = View.VISIBLE
            }
        }

        private fun setImage(resource: Resource) {
            imageView.run {
                when (resource.state) {
                    Resource.State.DOWNLOAD -> asyncLoad(R.drawable.down_arrow, asGif = true)
                    Resource.State.CREATE -> asyncLoad(R.drawable.placeholder, asGif = true)
                    Resource.State.READ,
                    Resource.State.WRITE -> asyncLoad(R.drawable.spinning_cdrom, asGif = true)
                    Resource.State.PROCESS -> asyncLoad(R.drawable.filter, asGif = true)
                    Resource.State.LOAD,
                    Resource.State.CLOSE -> {
                        if (!resource.filePath.isNullOrEmpty()) {
                            asyncLoad(resource.filePath!!)
                        } else {
                            asyncLoad(R.drawable.error)
                        }
                    }
                }
            }
        }

        private fun hideProgressBar() {
            indeterminateProgressBar.visibility = View.INVISIBLE
            progressBar.visibility = View.INVISIBLE
        }

        private fun showProgressBar() {
            indeterminateProgressBar.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
        }

        private fun showIndeterminateProgressBar() {
            indeterminateProgressBar.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            indeterminateProgressBar.progress = 100
        }

        override fun toString(): String {
            return super.toString() + " '$url'"
        }
    }

    class DiffUtilCallback(private val oldList: List<Resource>,
                           private val newList: List<Resource>)
        : DiffUtil.Callback() {

        companion object {
            fun areContentsTheSame(oldItem: Resource, newItem: Resource): Boolean
                    = oldItem.state == newItem.state
                      && oldItem.size == newItem.size
                      && oldItem.progress == newItem.progress

            fun areItemsTheSame(oldItem: Resource, newItem: Resource)
                    = oldItem == newItem
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int,
                                     newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItemPosition: Int,
                                        newItemPosition: Int): Boolean
                = areContentsTheSame(oldList[oldItemPosition],
                                     newList[newItemPosition])
    }
}