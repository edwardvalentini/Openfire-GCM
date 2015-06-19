<%@ page
   import="org.jivesoftware.openfire.XMPPServer,
           de.meisterfuu.openfire.gcm.GcmPlugin,
           org.jivesoftware.util.ParamUtils,
           java.util.HashMap,
           java.util.Map"
   errorPage="error.jsp"%>

<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>

<%
	boolean save = request.getParameter("save") != null;	
	boolean gcmhDebug = ParamUtils.getBooleanParameter(request, "gcmhDebug", false);
	boolean gcmhMode = ParamUtils.getBooleanParameter(request, "gcmhMode", false);
	String gcmhHost = ParamUtils.getParameter(request, "gcmhHost");
	int gcmhPort = ParamUtils.getParameter(request, "gcmhPort");
	String gcmhSenderId = ParamUtils.getParameter(request, "gcmhSenderId");
	String gcmhApiKey = ParamUtils.getParameter(request, "gcmhApiKey");

	GcmPlugin plugin = (GcmPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("gcmh");

	Map<String, String> errors = new HashMap<String, String>();	
	if (save) {
	/*
	  if (gcmhURL == null || gcmhURL.trim().length() < 1) {
	     errors.put("missinggcmhURL", "missinggcmhURL");
	  }
	  */
              
	  if (errors.size() == 0) {
	     //plugin.setEnabled(gcmhDebug);
	     plugin.setHost(gcmhHost);
	     plugin.setPort(gcmhPort);
	     plugin.setSenderId(gcmhSenderId);
	     plugin.setApiKey(gcmhApiKey);
         plugin.setDebug(gcmhDebug);
         if(gcmhMode){
        	 plugin.setMode(GcmPlugin.MODE_OFFLINE);
         } else {
        	 plugin.setMode(GcmPlugin.MODE_ALL);
         }
	     
	     response.sendRedirect("gcmh-form.jsp?settingsSaved=true");
	     return;
	  }		
	}
    
	//gcmhDebug = plugin.isEnabled();
	gcmhHost = plugin.getHost();
	gcmhPort = plugin.getPort();
	gcmhSenderId = plugin.getSenderId();
	gcmhApiKey = plugin.getApiKey();
	gcmhDebug = plugin.getDebug();
	if(plugin.getMode().equals(GcmPlugin.MODE_ALL)){
		gcmhMode = false;
	} else {
		gcmhMode = true;
	}

%>

<html>
	<head>
	  <title><fmt:message key="gcmh.title" /></title>
	  <meta name="pageID" content="gcmh-form"/>
	</head>
	<body>

<form action="gcmh-form.jsp?save" method="post">

<div class="jive-contentBoxHeader"><fmt:message key="gcmh.options" /></div>
<div class="jive-contentBox">
   
	<% if (ParamUtils.getBooleanParameter(request, "settingsSaved")) { %>
   
	<div class="jive-success">
	<table cellpadding="0" cellspacing="0" border="0">
	<tbody>
	  <tr>
	     <td class="jive-icon"><img src="images/success-16x16.gif" width="16" height="16" border="0"></td>
	     <td class="jive-icon-label"><fmt:message key="gcmh.saved.success" /></td>
	  </tr>
	</tbody>
	</table>
	</div>
   
	<% } %>
   	
   <br>
	<p><fmt:message key="gcmh.directions" /></p>
   
	<table cellpadding="3" cellspacing="0" border="0" width="100%">
	<tbody>
	  <tr>
	     <td width="5%" valign="top"><fmt:message key="gcmh.host" />:&nbsp;</td>
	     <td width="95%"><input type="text" name="gcmhHost" value="<%= gcmhHost %>"></td>
	     <% if (errors.containsKey("missinggcmhHost")) { %>
	        <span class="jive-error-text"><fmt:message key="gcmh.host.missing" /></span>
	     <% } %> 
	  </tr>
	  <tr>
	     <td width="5%" valign="top"><fmt:message key="gcmh.port" />:&nbsp;</td>
	     <td width="95%"><input type="text" name="gcmhPort" value="<%= gcmhPort %>"></td>
	     <% if (errors.containsKey("missinggcmhPort")) { %>
	        <span class="jive-error-text"><fmt:message key="gcmh.port.missing" /></span>
	     <% } %>
	  </tr>
	  <tr>
	     <td width="5%" valign="top"><fmt:message key="gcmh.sender.id" />:&nbsp;</td>
	     <td width="95%"><input type="text" name="gcmhSenderId" value="<%= gcmhSenderId %>"></td>
	     <% if (errors.containsKey("missinggcmhSenderId")) { %>
	        <span class="jive-error-text"><fmt:message key="gcmh.sender.id.missing" /></span>
	     <% } %>
	  </tr>
	  <tr>
	     <td width="5%" valign="top"><fmt:message key="gcmh.api.key" />:&nbsp;</td>
	     <td width="95%"><input type="text" name="gcmhApiKey" value="<%= gcmhApiKey %>"></td>
	     <% if (errors.containsKey("missinggcmhApiKey")) { %>
	        <span class="jive-error-text"><fmt:message key="gcmh.api.key.missing" /></span>
	     <% } %>
	  </tr>
	  <tr>
	     <td width="1%" align="center" nowrap><input type="checkbox" name="gcmhMode" <%=gcmhMode ? "checked" : "" %>></td>
	     <td width="99%" align="left"><fmt:message key="gcmh.mode" /></td>
	  </tr>
	  <tr>
	     <td width="1%" align="center" nowrap><input type="checkbox" name="gcmhDebug" <%=gcmhDebug ? "checked" : "" %>></td>
	     <td width="99%" align="left"><fmt:message key="gcmh.enable" /></td>
	  </tr>
	</tbody>
	</table>
</div>
<input type="submit" value="<fmt:message key="gcmh.button.save" />"/>
</form>

</body>
</html>
