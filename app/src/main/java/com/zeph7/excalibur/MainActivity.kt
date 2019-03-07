package com.zeph7.excalibur

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var share_url:String? = null

    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //loads home
        webview.loadUrl("https://www.google.com/")
        edit_text.setText("")
        //toolbar set as actionbar
        setSupportActionBar(toolbar)
        //javascript enabled
        webview.settings.javaScriptEnabled = true

        //downloadlistener enabled
        webview.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            //filename of downloading file
            var filename = URLUtil.guessFileName(url, contentDisposition, mimetype)

            //alertdialog builder created
            val builder = AlertDialog.Builder(this@MainActivity)
            //alertdialog title set
            builder.setTitle("Download")
            //alertdialog message set
            builder.setMessage("Do you want to save $filename")
            //if yes clicks,following code will executed
            builder.setPositiveButton("Yes") { dialog, which ->
                //DownloadManager request created based on url
                val request = DownloadManager.Request(Uri.parse(url))
                //get cookie
                val cookie = CookieManager.getInstance().getCookie(url)
                //add cookie to request
                request.addRequestHeader("Cookie",cookie)
                //add User-agent to request
                request.addRequestHeader("User-Agent",userAgent)
                //Files are scanned before downloading
                request.allowScanningByMediaScanner()
                //download notification is visible while downloading and after download completion
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                //DownloadManager Service created
                val downloadmanager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                //Files are downloaded to Download folder
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename)
                //download starts
                downloadmanager.enqueue(request)
            }
            builder.setNegativeButton("Cancel")
            {dialog, which ->
                //dialog cancels
                dialog.cancel()
            }
            //alertdialog created
            val dialog:AlertDialog=builder.create()
            //shows alertdialog
            dialog.show()
        }

        webview.webChromeClient = object : WebChromeClient()
        {
            override fun onProgressChanged(view: WebView?, newProgress: Int)
            {
                //it calls when progress changed
                progressbar.progress = newProgress
                super.onProgressChanged(view, newProgress)

                if(newProgress == 100)
                {
                    //if progress completes, progressbar gets hidden
                    progressbar.visibility = View.GONE
                }

            }


            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                super.onReceivedIcon(view, icon)
                //favicon of webpage
                toolbar_search_imageview_favicon.setImageBitmap(icon)
            }
        }

        webview.webViewClient = object: WebViewClient()
        {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)
            {
                //it calls when webpage gets started and shows progressbar
                progressbar.visibility = View.VISIBLE

                if("https://www.google.com/" != url)
                {
                    //if url is demo html url, then don't set to edittext
                    edit_text.setText(url)
                }
                else
                {
                    //edittext cleared
                    edit_text.text.clear()
                }

                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?)
            {
                //if webpage gets finished
                share_url = url

                super.onPageFinished(view, url)
            }

        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
    {
        //when back button taps
        if(keyCode == KeyEvent.KEYCODE_BACK && this.webview.canGoBack())
        {
            webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun goBack(view:View?)
    {
        //webview goes to previous page if there is one.
        if(webview.canGoBack())
            webview.goBack()
    }

    fun goForward(view: View?)
    {
        //webview goes to forward page if there is one.
        if(webview.canGoForward())
            webview.goForward()
    }

    fun goHome(view:View?)
    {
        webview.loadUrl("https://www.google.com/")
    }

    fun refresh(view: View?)
    {
        //it reload the current page
        webview.reload()
    }

    fun share(view: View?)
    {
        //Intent created
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        //url of the sharing page
        shareIntent.putExtra(Intent.EXTRA_TEXT, share_url)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"URL")

        startActivity(Intent.createChooser(shareIntent,"Share with your friends"))

    }

    fun go(view:View?)
    {
        //get text from edittext
        var text= edit_text.text.toString()
        searchOrLoad(text)
    }

    fun searchOrLoad(text:String)
    {
        //checks if it's a url or a string
        if(Patterns.WEB_URL.matcher(text.toLowerCase()).matches())
        {
            if (text.contains("http://") || text.contains("https://"))
            {
                webview.loadUrl(text)
            }
            else
            {
                webview.loadUrl("http://$text")
            }
        }
        else
        {
            //google search url
            webview.loadUrl("https://www.google.com/search?q=$text")
        }

        hideKeyboard()

    }

    private fun hideKeyboard()
    {
        //INPUTMETHODMANAGER service created.
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //it hides softkeyboard
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken,InputMethodManager.SHOW_FORCED)
    }

    override fun onPause()
    {
        super.onPause()
        webview.onPause()
        webview.pauseTimers()
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
        webview.resumeTimers()
    }

}
