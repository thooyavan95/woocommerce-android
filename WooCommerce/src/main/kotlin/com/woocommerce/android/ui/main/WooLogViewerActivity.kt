package com.woocommerce.android.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.woocommerce.android.R
import com.woocommerce.android.R.layout
import com.woocommerce.android.extensions.setHtmlText
import com.woocommerce.android.util.WooLog
import com.woocommerce.android.util.WooLog.T
import com.woocommerce.android.widgets.AlignedDividerDecoration
import kotlinx.android.synthetic.main.activity_logviewer.*
import org.wordpress.android.util.ToastUtils
import java.lang.String.format
import java.util.ArrayList
import java.util.Locale

class WooLogViewerActivity : AppCompatActivity() {
    companion object {
        private const val ID_SHARE = 1
        private const val ID_COPY_TO_CLIPBOARD = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logviewer)

        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val divider = AlignedDividerDecoration(this,
                DividerItemDecoration.VERTICAL, 0, clipToMargin = false)
        ContextCompat.getDrawable(this, R.drawable.list_divider)?.let { drawable ->
            divider.setDrawable(drawable)
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.addItemDecoration(divider)
        recycler.adapter = LogAdapter(this)
    }

    private fun shareAppLog() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, WooLog.toString())
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " + title)
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        } catch (ex: android.content.ActivityNotFoundException) {
            ToastUtils.showToast(this, R.string.logviewer_share_error)
        }
    }

    private fun copyAppLogToClipboard() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("AppLog", WooLog.toString())
            ToastUtils.showToast(this, R.string.logviewer_copied_to_clipboard)
        } catch (e: Exception) {
            WooLog.e(T.UTILS, e)
            ToastUtils.showToast(this, R.string.logviewer_error_copy_to_clipboard)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        val mnuCopy = menu.add(Menu.NONE, ID_COPY_TO_CLIPBOARD, Menu.NONE, android.R.string.copy)
        mnuCopy.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        mnuCopy.setIcon(R.drawable.ic_copy_white_24dp)

        val mnuShare = menu.add(Menu.NONE, ID_SHARE, Menu.NONE, R.string.share)
        mnuShare.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        mnuShare.setIcon(R.drawable.ic_share_white_24dp)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            ID_SHARE -> {
                shareAppLog()
                true
            }
            ID_COPY_TO_CLIPBOARD -> {
                copyAppLogToClipboard()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class LogAdapter constructor(context: Context) : RecyclerView.Adapter<LogViewHolder>() {
        private val entries: ArrayList<String> = WooLog.toHtmlList()
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun getItemCount() = entries.size

        override fun getItemId(position: Int) = position.toLong()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            return LogViewHolder(inflater.inflate(layout.logviewer_listitem, parent, false))
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            holder.txtLineNumber.text = format(Locale.US, "%02d", position + 1)
            holder.txtLogEntry.setHtmlText(entries[position])
        }
    }

    private inner class LogViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        val txtLineNumber: TextView = view.findViewById(R.id.text_line) as TextView
        val txtLogEntry: TextView = view.findViewById(R.id.text_log) as TextView
    }
}
