package com.moviz.lib.velocity;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.webkit.WebView;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeInstance;

import java.io.InputStream;

/**
 * Created by Fujitsu on 10/11/2016.
 */
public abstract class AndroidVelocitySheet extends VelocitySheet {
    protected final Resources res;
    protected final SharedPreferences sharedPref;
    protected final WebView webview;

    @Override
    protected String getTemplateFilePath(String templateName) {
        return sharedPref.getString("pref_temp_"+templateName,"r");
    }

    @Override
    protected void initInternalResourceLoader(RuntimeInstance ri) {
        ri.setProperty("resource.loader", "android");
    }

    @Override
    protected void runtimeInstanceCommonInit(RuntimeInstance ri) {
        ri.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS,
                VelocityLogger.class.getName());

        ri.setProperty("android.resource.loader.class",
                AndroidResourceLoader.class.getName());
        ri.setProperty("android.content.res.Resources", res);

        ri.setProperty("packageName", "com.moviz.gui");
    }

    @Override
    protected String getResourceAsString(String pth) {
        String out = "";

        try {
            if (pth.endsWith(".vm"))
                pth = pth.substring(0,pth.length()-3);
            int id = res.getIdentifier(pth, "raw", "com.moviz.gui");
            InputStream is = res.openRawResource(id);
            out = readAllFile(is);
        }
        catch (Exception e) {
            out = "";
        }

        return out;
    }

    @Override
    protected void putStringToSheet(String string) {
        webview.loadDataWithBaseURL(null, string, "text/html", "UTF-8",
                "UTF-8");
    }

    public AndroidVelocitySheet(Resources r, SharedPreferences shp, WebView wv) {
        super();
        res = r;
        sharedPref = shp;
        webview = wv;
    }
}
