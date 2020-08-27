package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.storage.StorageUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Implementation of the mock scripts service.
 */
public class MockScriptsServlet extends OdeServlet {
    private static final Logger LOG = Logger.getLogger(MockScriptsServlet.class.getName());
    private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

    // Minified code
    // Sauce https://gist.github.com/pavi2410/18195e3e6096aa257aa0341524d0da9e
    private static final String SDK_CODE_START = "<html><body><script>" +
            "let registry={},mockInstances={};class MockComponentRegistry{static register(a,b){registry[a]=b,console.log(\"<iframe>\",\"registering VCE\",a,\"<UUID = undefined>\"),postMessage(\"registerMockComponent\",[],a,\"\")}}window.addEventListener(\"message\",a=>{let{action:b,args:c,type:d,uuid:e}=JSON.parse(a.data);messageInterpreter(b,c,d,e)},!1);function messageInterpreter(a,b,c,d){switch(console.log(\"<iframe>\",\"got message\",a,b,c,d),a){case\"instantiateComponent\":{let a=registry[c];mockInstances[d]=new a(d);break}case\"getName.callback\":{console.log(\"getName.cb\",c,d,b);break}case\"getPropertyValue.callback\":{console.log(\"getPropVal.cb\",c,d,b);break}case\"onPropertyChange\":{mockInstances[d].onPropertyChange(b[0],b[1]);break}}}function postMessage(a,b,c,d){let e=JSON.stringify({action:a,args:b,type:c,uuid:d});window.top.postMessage(e,\"*\")}function genUUID(){return\"xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx\".replace(/[xy]/g,function(a){var b=0|16*Math.random(),c=\"x\"==a?b:8|3&b;return c.toString(16)})}class MockVisibleExtension{constructor(a,b){this.type=a,this.uuid=b}initComponent(a){this.$el=a,postMessage(\"initializeComponent\",[a.outerHTML],this.type,this.uuid)}getName(){postMessage(\"getName\",[],this.type,this.uuid)}getPropertyValue(a){postMessage(\"getPropertyValue\",[a],this.type,this.uuid)}changeProperty(a,b){postMessage(\"changeProperty\",[a,b],this.type,this.uuid)}refresh(a){a=a||this.$el,postMessage(\"updateMockComponent\",[a.outerHTML],this.type,this.uuid)}onPropertyChange(){this.refresh(this.$el)}}";
    private static final String SDK_CODE_END = "</script></body></html>";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        long projectId = Long.parseLong(req.getParameter("projectId"));
        String type = req.getParameter("type");

        String mockScriptFile = getMockScript(projectId, type);

        res.setContentType("text/html");
        res.getWriter().println(mockScriptFile);
    }

    /**
     * @param projectId id of the project
     * @param type component type (FQCN)
     * @return mock script file
     */
    public String getMockScript(long projectId, String type) {
        String userId = userInfoProvider.getUserId();

        String pkgName = type.substring(0, type.lastIndexOf('.'));
        String simpleName = type.substring(type.lastIndexOf('.') + 1);

        String fileId = "assets/external_comps/" + pkgName + "/Mock" + simpleName + ".js";

        String mockFile = storageIo.downloadFile(userId, projectId, fileId, StorageUtil.DEFAULT_CHARSET);
        return SDK_CODE_START + mockFile + SDK_CODE_END;
    }
}