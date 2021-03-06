package com.aimluck.eip.cayenne.om.portlet.auto;

import java.util.List;

/** Class _EipTWorkflowCategory was generated by Cayenne.
  * It is probably a good idea to avoid changing this class manually, 
  * since it may be overwritten next time code is regenerated. 
  * If you need to make any customizations, please use subclass. 
  */
public class _EipTWorkflowCategory extends org.apache.cayenne.CayenneDataObject {

    public static final String CATEGORY_NAME_PROPERTY = "categoryName";
    public static final String CREATE_DATE_PROPERTY = "createDate";
    public static final String NOTE_PROPERTY = "note";
    public static final String TEMPLATE_PROPERTY = "template";
    public static final String UPDATE_DATE_PROPERTY = "updateDate";
    public static final String USER_ID_PROPERTY = "userId";
    public static final String EIP_TWORKFLOW_REQUEST_PROPERTY = "eipTWorkflowRequest";
    public static final String EIP_TWORKFLOW_ROUTE_PROPERTY = "eipTWorkflowRoute";

    public static final String CATEGORY_ID_PK_COLUMN = "CATEGORY_ID";

    public void setCategoryName(String categoryName) {
        writeProperty("categoryName", categoryName);
    }
    public String getCategoryName() {
        return (String)readProperty("categoryName");
    }
    
    
    public void setCreateDate(java.util.Date createDate) {
        writeProperty("createDate", createDate);
    }
    public java.util.Date getCreateDate() {
        return (java.util.Date)readProperty("createDate");
    }
    
    
    public void setNote(String note) {
        writeProperty("note", note);
    }
    public String getNote() {
        return (String)readProperty("note");
    }
    
    
    public void setTemplate(String template) {
        writeProperty("template", template);
    }
    public String getTemplate() {
        return (String)readProperty("template");
    }
    
    
    public void setUpdateDate(java.util.Date updateDate) {
        writeProperty("updateDate", updateDate);
    }
    public java.util.Date getUpdateDate() {
        return (java.util.Date)readProperty("updateDate");
    }
    
    
    public void setUserId(Integer userId) {
        writeProperty("userId", userId);
    }
    public Integer getUserId() {
        return (Integer)readProperty("userId");
    }
    
    
    public void addToEipTWorkflowRequest(com.aimluck.eip.cayenne.om.portlet.EipTWorkflowRequest obj) {
        addToManyTarget("eipTWorkflowRequest", obj, true);
    }
    public void removeFromEipTWorkflowRequest(com.aimluck.eip.cayenne.om.portlet.EipTWorkflowRequest obj) {
        removeToManyTarget("eipTWorkflowRequest", obj, true);
    }
    public List getEipTWorkflowRequest() {
        return (List)readProperty("eipTWorkflowRequest");
    }
    
    
    public void setEipTWorkflowRoute(com.aimluck.eip.cayenne.om.portlet.EipTWorkflowRoute eipTWorkflowRoute) {
        setToOneTarget("eipTWorkflowRoute", eipTWorkflowRoute, true);
    }

    public com.aimluck.eip.cayenne.om.portlet.EipTWorkflowRoute getEipTWorkflowRoute() {
        return (com.aimluck.eip.cayenne.om.portlet.EipTWorkflowRoute)readProperty("eipTWorkflowRoute");
    } 
    
    
}
