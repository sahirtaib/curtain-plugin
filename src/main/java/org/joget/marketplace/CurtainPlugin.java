package org.joget.marketplace;

import org.joget.apps.app.model.PluginWebFilterAbstract;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.plugin.base.SystemConfigurablePlugin;
import org.joget.workflow.model.service.WorkflowUserManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.sql.ResultSet;
import javax.sql.DataSource;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class CurtainPlugin extends PluginWebFilterAbstract implements SystemConfigurablePlugin, PluginWebSupport {

    @Override
    public String getName() {
        return "CurtainPlugin";
    }

    @Override
    public String getVersion() {
        return "8.2.1";
    }
    
    @Override
    public String getLabel() {
        return "Curtain Plugin";
    }

    @Override
    public String getDescription() {
        return "Show a \"curtain\" in your selected apps, preventing non-admin access until admin manually lifts the \"curtain\".";
    }

    @Override
    public String[] getUrlPatterns() {
        // TODO: exclude static resources js, css, images here instead of doFilter()
        return new String[]{"/web/userview/**"};
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties.json", null, false, null);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {        
        WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        HttpServletRequest request = (HttpServletRequest) req;

        // TODO: do not skip static assets in App Resources
        if (isStaticAssets(request) || !isEnabled() || !appIsCurtained(request) || wum.isCurrentUserInRole("ROLE_ADMIN")) {
            chain.doFilter(req, res);
            return;
        }

        HttpServletResponse response = (HttpServletResponse) res;
        showPageNotFound(request, response);
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String ask = request.getParameter("ask");

        if (ask.equals("apps")) {
            response.getWriter().write(listApps().toString());
        }

        response.getWriter().flush();
    }

    private boolean isStaticAssets(HttpServletRequest request) {
        return request.getRequestURI().matches(".*\\.(css|js|png|jpg|jpeg|gif|ico|svg|woff2?|ttf|eot|map|webp)$");
    }

    private boolean isEnabled() {
        return "true".equalsIgnoreCase(getPropertyString("enabled"));
    }

    private boolean appIsCurtained(HttpServletRequest request) {
        boolean isCurtained = false;

        String regex = "(?<=/web/userview/)[^/]+";
        Matcher matcher = Pattern.compile(regex).matcher(request.getRequestURI());
        String requestedAppId = "";
        if (matcher.find()) {
            requestedAppId = matcher.group();
        }

        if (requestedAppId.isEmpty()) {
            return isCurtained;
        }
        
        Object[] urlMappings = (Object[]) properties.get("apps");

        if (urlMappings != null) {
            Map mapping = null;
            for (Object urlMapping : urlMappings) {
                mapping = (HashMap) urlMapping;
                String curtainedAppId = mapping.get("appId").toString();

                if (requestedAppId.equals(curtainedAppId)) {
                    isCurtained = true;
                    break;
                }
            }
        }

        return isCurtained;
    }

    private void showCurtain(ServletResponse res) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Map data = new HashMap();
        // data.put("plugin", this);

        String html = pluginManager.getPluginFreeMarkerTemplate(data, getClass().getName(), "/html.ftl", null);

        try {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(html);
            response.getWriter().flush();
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
    }

    private void showPageNotFound(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String html = "/WEB-INF/jsp/error404.jsp"; 
            RequestDispatcher dispatcher = request.getRequestDispatcher(html);
            dispatcher.forward(request, response);
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
    }

    private JSONArray listApps() {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        JSONArray apps = new JSONArray();

        try {
            ResultSet rs = ds.getConnection().createStatement().executeQuery("select appId as value, name as label, appVersion, published from app_app");

            while (rs.next()) {
                JSONObject app = new JSONObject();
                app.put("value", rs.getString("appId"));
                app.put("label", rs.getString("name") + " (v" + rs.getString("appVersion") + ") - " + getReadableStatus(rs.getString("published")));
                apps.put(app);
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }

        return apps;
    }

    private String getReadableStatus(String value) {
        if ("1".equalsIgnoreCase(value)) {
            return "Published";
        } else if ("0".equalsIgnoreCase(value)) {
            return "Unpublished";
        } else {
            return "UNKNOWN";
        }
    }
}
