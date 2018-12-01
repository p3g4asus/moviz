package com.moviz.lib.velocity;

import com.moviz.lib.comunication.DeviceType;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.Holder;
import com.moviz.lib.comunication.holder.HolderSetter;
import com.moviz.lib.comunication.holder.Holderable;
import com.moviz.lib.comunication.holder.Id2Key;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class VelocitySheet {
    private static class TemplateOutput {
        public Template t;
        public Map<Object, Object> out = null;

        public TemplateOutput(Template temp) {
            t = temp;
        }

        public boolean getFromContext(PVelocityContext vel) {
            boolean rv = false;
            Object o = vel.get("MOUT");
            out = null;
            if (o != null) {
                if (o instanceof Map) {
                    out = (Map<Object, Object>) o;
                    //System.out.println(out);
                    rv = true;
                }
                vel.remove("MOUT");
            }
            //else
            //    System.out.println("{upd_OUTNULL");
            return rv;
        }

        public boolean putInContext(PVelocityContext vel) {
            if (out != null) {
                vel.put("MIN", out);
                return true;
            } else
                return false;
        }
    }

    private RuntimeInstance ri = new RuntimeInstance();
    private PVelocityContext mainCtx = new PVelocityContext();
    private PVelocityContext setCtx = null;
    private Map<DeviceType, TemplateOutput> templates = new HashMap<>();
    private TemplateOutput customTemplate;
    protected Id2Key convId = new Id2Key() {

        @Override
        public String convert(String id) {
            return id.replace('.', '_');
        }
    };

    protected abstract String getTemplateFilePath(String templateName);

    protected abstract void initInternalResourceLoader(RuntimeInstance ri);

    protected abstract void runtimeInstanceCommonInit(RuntimeInstance ri);
    protected abstract String getResourceAsString(String pth);


    
    public void init() throws Exception {
        String tn = getTemplateName(), fn = getTemplateFilePath(tn);
        File f;
        ri.setProperty("velocimacro.library", "macros_" + tn + ".vm");
        ri.setProperty("runtime.introspector.uberspect",
                "com.moviz.lib.velocity.PublicFieldUberspect");
        runtimeInstanceCommonInit(ri);
        if (!fn.isEmpty() && (f = new File(fn)).isFile() && f.exists()
                && f.canRead()) {
            String tempPath = f.getParent();
            ri.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                    tempPath == null ? "/." : tempPath);
            initTemplates(f);
        } else {
            initInternalResourceLoader(ri);
            initTemplates(null);
        }
    }

    public static class SheetUpdate {
        public Map<? extends DeviceHolder, ? extends Holderable> m;
        public String varName;
        public SheetUpdate(Map<? extends DeviceHolder, ? extends Holderable> s, String v) {
            m = s;
            varName = v;
        }
    }
    
    public void updateValues(SheetUpdate... vals) {
        PVelocityContext ctx = null;
        SheetUpdate last = null;
        for (SheetUpdate sh:vals) {
            ctx = buildContext(sh.m, sh.varName,mainCtx);
            last = sh;
        }
        if (last!=null) {
            String s;
            //System.out.println("{upd_START "+ctx);
            if (customTemplate == null)
                s = updateVelocitySet(last.m, ctx);
            else {
                addToContext(ctx);
                s = velocityUpdateSingle(customTemplate, ctx);
            }
            //System.out.println("{upd_FINISH "+ctx);
            putStringToSheet(s);
            mainCtx.clear();
        }
    }
    
    private String velocityUpdateSingle(TemplateOutput to, PVelocityContext ctx) {
        StringWriter sw = new StringWriter();
        // add stuff to your context.
        to.putInContext(ctx);
        // if (rv) printContext(ctx,"PRE ->");
        to.t.merge(ctx, sw);
        to.getFromContext(ctx);
        // if (rv) printContext(ctx,"POST ->");
        return sw.toString();
    }
    
    private String updateVelocitySet(
            Map<? extends DeviceHolder, ? extends Holderable> updates, PVelocityContext ctx) {
        Set<? extends DeviceHolder> devs = updates.keySet();
        String page = "";
        int i = 0;
        // System.out.println("len "+updates.size());
        for (DeviceHolder devh : devs) {

            // System.out.println(devh.getAlias()+" "+ctx.get(devh.getAlias())+" "+devs.size()+" "+i);
            setCtx.put("D", ctx.get(devh.getAlias()));
            setCtx.put("T", devh);
            setCtx.put("first__", i == 0);
            setCtx.put("last__", i == devs.size() - 1);
            addToContext(setCtx);
            try {
                page += velocityUpdateSingle(templates.get(devh.getType()),
                        setCtx);
                /*
                 * webview.postDelayed(new Runnable() {
                 * 
                 * @Override public void run() {
                 * webview.loadDataWithBaseURL(null, page, "text/html", "UTF-8",
                 * "UTF-8"); } }, 100);
                 */

                // String html_value =
                // "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"><title>Lorem Ipsum</title></head><body style=\"width:300px; color: #00000; \"><p><strong> About us</strong> </p><p><strong> Lorem Ipsum</strong> is simply dummy text .</p><p><strong> Lorem Ipsum</strong> is simply dummy text </p><p><strong> Lorem Ipsum</strong> is simply dummy text </p></body></html>";
                // webview.loadData(page, "text/html", "UTF-8");
                // engine.loadUrl("http://www.google.com");
                // engine.loadDataWithBaseURL("",
                // "<HTML><BODY><H3>Test</H3></BODY></HTML>","text/html","utf-8","");
                // engine.loadUrl("http://developer.android.com/reference/android/widget/ProgressBar.html");
            } catch (Exception e) {
                e.printStackTrace();
            }
            setCtx.clear();
            i++;
        }
        // System.out.println(page+" "+i);
        return page;
    }
    
    private void initTemplates(File custom) {
        String tn = getTemplateName();
        ri.init();
        if (custom != null) {
            try {
                customTemplate = new TemplateOutput(loadTemplate(ri, tn,
                        getCustomTemplateAsText(custom)));
            } catch (Exception e) {
                e.printStackTrace();
                customTemplate = null;
            }
        }
        if (customTemplate == null) {
            String tmp;
            setCtx = new PVelocityContext();
            for (DeviceType tp : DeviceType.types) {
                tmp = tp.name() + "_" + tn;
                try {
                    templates.put(
                            tp,
                            new TemplateOutput(loadTemplate(ri, tmp,
                                    getResourceAsString(tmp + ".vm"))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private String getCustomTemplateAsText(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            return readAllFile(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static Template loadTemplate(RuntimeInstance ri, String tn,
            String bufferForYourTemplate) throws Exception {
        StringReader reader = new StringReader(bufferForYourTemplate);
        SimpleNode node = ri.parse(reader, tn);
        Template template = new Template();
        template.setRuntimeServices(ri);
        template.setData(node);
        template.initDocument();
        return template;
    }
    
    public static String readAllFile(InputStream is) {
        String out = "";
        try {
            String ln;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                ln = br.readLine();
                if (ln != null)
                    out += ln + "\n";
                else
                    break;
            }
        } catch (Exception e) {
            out = "";
        }
        return out;
    }

    protected void printContext(PVelocityContext vct, String prefix) {
        String rv = prefix + "{";
        for (Object k : vct.getKeys()) {
            rv += "[" + k.toString() + "]->(" + vct.get(k.toString()) + ")";
        }
        System.out.println(rv);
    }


    public VelocitySheet() {
    }



    protected PVelocityContext buildContext(
            Map<? extends DeviceHolder, ? extends Holderable> updates, String varName, PVelocityContext context) {
        String al;
        Map<String, ? extends Holder> m;
        for (Map.Entry<? extends DeviceHolder, ? extends Holderable> entry : updates
                .entrySet()) {
            al = entry.getKey().getAlias();
            m = entry.getValue()
                    .toHolder(Holder.class, HolderSetter.class,
                            varName + ".").toMap(convId);
            if (context.containsKey(al))
                ((Map<String, Holder>)context.get(al)).putAll(m);
            else
                context.put(al,m);
        }

        return context;
    }

    protected void addToContext(PVelocityContext context) {
        context.put("String", String.class);
    }

    protected abstract void putStringToSheet(String string);

    protected abstract String getTemplateName();
}
