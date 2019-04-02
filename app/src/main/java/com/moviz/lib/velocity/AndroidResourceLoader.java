package com.moviz.lib.velocity;

import android.content.res.Resources;

import org.apache.velocity.util.ExtProperties;;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import java.io.InputStream;

public class AndroidResourceLoader extends FileResourceLoader {
    private Resources resources;
    private String packageName;

    public void commonInit(RuntimeServices rs, ExtProperties configuration) {
        super.commonInit(rs, configuration);
        this.resources = (Resources) rs.getProperty("android.content.res.Resources");
        this.packageName = (String) rs.getProperty("packageName");
    }

    public long getLastModified(Resource resource) {
        return 0;
    }

    public InputStream getResourceStream(String pth) {
        if (pth.endsWith(".vm"))
            pth = pth.substring(0,pth.length()-3);
        int id = resources.getIdentifier(pth, "raw", this.packageName);
        return resources.openRawResource(id);
    }

    public boolean isSourceModified(Resource resource) {
        return false;
    }

    public boolean resourceExists(String templateName) {
        return resources.getIdentifier(templateName, "raw", this.packageName) != 0;
    }
}