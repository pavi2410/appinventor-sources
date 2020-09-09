package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.storage.StorageUtil;

import javax.servlet.ServletException;
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

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        long projectId = Long.parseLong(req.getParameter("projectId"));
        String type = req.getParameter("type");

        String mockScriptFile = getMockScript(projectId, type);

        req.setAttribute("mockScriptFile", mockScriptFile);
        req.getRequestDispatcher("/VCE_SDK_template.jsp").forward(req, res);
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

        return storageIo.downloadFile(userId, projectId, fileId, StorageUtil.DEFAULT_CHARSET);
    }
}