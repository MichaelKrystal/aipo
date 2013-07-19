/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aimluck.eip.portal.controls;

// Turbine stuff
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.jetspeed.om.profile.Entry;
import org.apache.jetspeed.om.profile.Portlets;
import org.apache.jetspeed.om.profile.Profile;
import org.apache.jetspeed.om.security.JetspeedUser;
import org.apache.jetspeed.portal.BasePortletSet;
import org.apache.jetspeed.portal.PanedPortletController;
import org.apache.jetspeed.portal.Portlet;
import org.apache.jetspeed.portal.PortletInstance;
import org.apache.jetspeed.portal.PortletSet;
import org.apache.jetspeed.portal.PortletState;
import org.apache.jetspeed.portal.controls.AbstractPortletControl;
import org.apache.jetspeed.portal.security.portlets.PortletWrapper;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.PortalToolkit;
import org.apache.jetspeed.services.TemplateLocator;
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.jetspeed.services.persistence.PersistenceManager;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.jetspeed.services.security.PortalResource;
import org.apache.jetspeed.services.statemanager.SessionState;
import org.apache.jetspeed.util.template.JetspeedLink;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.jetspeed.util.template.JetspeedTool;
import org.apache.turbine.services.pull.TurbinePull;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.DynamicURI;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.apache.velocity.context.Context;

import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.common.ALApplication;
import com.aimluck.eip.common.ALEipConstants;
import com.aimluck.eip.common.ALEipInformation;
import com.aimluck.eip.common.ALFunction;
import com.aimluck.eip.orm.query.ResultList;
import com.aimluck.eip.services.accessctl.ALAccessControlConstants;
import com.aimluck.eip.services.social.ALApplicationService;
import com.aimluck.eip.services.social.model.ALApplicationGetRequest;
import com.aimluck.eip.util.ALCommonUtils;
import com.aimluck.eip.util.ALEipUtils;

/**
 * A Velocity based portlet control which implements all PortletState action
 * 
 * <p>
 * To use this control you need to define in your registry the following entry
 * or similar:
 * </p>
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 *              &lt;portlet-control-entry name=&quot;TitlePortletControl&quot;&gt;
 *                &lt;classname&gt;org.apache.jetspeed.portal.controls.VelocityPortletControl&lt;/classname&gt;
 *                &lt;parameter name=&quot;theme&quot; value=&quot;default.vm&quot;/&gt;
 *                &lt;meta-info&gt;
 *                  &lt;title&gt;TitleControl&lt;/title&gt;
 *                  &lt;description&gt;The standard Jetspeed boxed control&lt;/description&gt;
 *                  &lt;image&gt;url of image (icon)&lt;/description&gt;
 *                &lt;/meta-info&gt;
 *                &lt;media-type ref=&quot;html&quot;/&gt;
 *              &lt;/portlet-control-entry&gt;
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * </pre>
 * 
 * 
 * @author <a href="mailto:re_carrasco@bco011.sonda.cl">Roberto Carrasco </a>
 * @author <a href="mailto:raphael@apache.org">Rapha�l Luta </a>
 * @author <a href="mailto:morciuch@apache.org">Mark Orciuch </a>
 * 
 * 
 */
public class ALVelocityPortletControl extends AbstractPortletControl {

  private static final long serialVersionUID = 5276591650472642917L;

  /**
   * Static initialization of the logger for this class
   */
  private static final JetspeedLogger logger = JetspeedLogFactoryService
    .getLogger(ALVelocityPortletControl.class.getName());

  /** Disable content caching */
  @Override
  public boolean isCacheable() {
    return false;
  }

  private List<Entry> getPortletList(RunData rundata)
      throws NullPointerException {
    JetspeedRunData jdata = (JetspeedRunData) rundata;
    Profile profile = jdata.getProfile();
    List<Entry> portletList = new ArrayList<Entry>();
    String pid = rundata.getParameters().get("js_pane");
    Portlets tabPortlets = profile.getDocument().getPortletsById(pid);
    Entry[] currentPortletEntries = tabPortlets.getEntriesArray();
    for (Entry entry : currentPortletEntries) {
      portletList.add(entry);
    }
    return portletList;
  }

  /**
   * Handles the content generation for this control using Velocity
   */
  @Override
  public ConcreteElement getContent(RunData rundata) {
    Portlet portlet = getPortlet();
    JetspeedRunData jdata = (JetspeedRunData) rundata;

    // Check to see if the portlet allows view
    // If the current security context disallows view,
    // do not display the portlet OR the control decorator
    if (portlet instanceof PortletWrapper) {
      PortletWrapper wrapper = (PortletWrapper) portlet;
      if (!wrapper.getAllowView(rundata)) {
        if (JetspeedResources.getBoolean(
          "defaultportletcontrol.hide.decorator",
          true)) {
          return new StringElement("");
        }
      }
    }

    // Create a new Velocity context and load default
    // application pull tools
    Context context = TurbineVelocity.getContext();

    // 修正 ：ノーマル表示時のポートレットの右上にメニューを配置できるように，
    // パラメータ functions を追加した．
    context.put("data", rundata);
    context.put("actions", buildActionList(rundata, portlet, context));
    context.put("functions", buildFunctionList(rundata, portlet));
    context.put("conf", getConfig());
    context.put("skin", portlet.getPortletConfig().getPortletSkin());
    context.put("utils", new ALCommonUtils());
    try {
      context.put("runs", getPortletList(rundata));
    } catch (NullPointerException e) {

    }

    // アクセス権限がなかった場合の削除表示フラグ
    boolean hasAuthority =
      ALEipUtils.getHasAuthority(
        rundata,
        context,
        ALAccessControlConstants.VALUE_ACL_DELETE);
    context.put(ALEipConstants.SECURE_ID, rundata.getUser().getTemp(
      ALEipConstants.SECURE_ID));
    String showDelete = "false";
    if (hasAuthority) {
      showDelete = "true";
    }
    context.put("accessControl", showDelete);

    context.put("client", ALEipUtils.getClient(rundata));
    context.put("clientVer", ALEipUtils.getClientVersion(rundata));

    // Put the request and session based contexts
    TurbinePull.populateContext(context, rundata);

    if ((jdata.getCustomized() != null)
      && portlet.getName().equals(jdata.getCustomized().getName())
      && (!portlet.providesCustomization())) {
      context.put("portlet", JetspeedTool.getCustomizer(portlet));
      context.put("portlet_instance", JetspeedTool.getCustomizer(portlet));
    } else {
      context.put("portlet", portlet);
      if (PersistenceManager.getInstance(portlet, jdata) == null) {
        context.put("portlet_instance", portlet);
      } else {
        context.put("portlet_instance", PersistenceManager.getInstance(
          portlet,
          jdata));
      }
    }

    // allow subclasses to add elements to the context
    buildContext(rundata, context);

    // 修正 ：タブの表示／非表示を切り替え可能にした（jetspeed.vm）．
    String showTab = rundata.getParameters().getString("showTab");
    if (showTab == null || showTab.equals("") || !showTab.equals("false")) {
      showTab = "true";
    }
    context.put("showTab", showTab);

    // お知らせ用
    ALEipInformation information = ALEipInformation.getInstance();
    String informationCookie = "";
    String informationText = "";
    if (information != null) {
      informationCookie = information.getInformationCookie();
      informationText = information.getInformationText();
      if (informationCookie != null && !"".equals(informationCookie)) {
        String auiInfoDispCookieValue = null;
        if (rundata.getRequest().getCookies() != null) {
          auiInfoDispCookieValue =
            rundata.getCookies().getString(informationCookie, "");
        }
        if (auiInfoDispCookieValue != null
          && !"".equals(auiInfoDispCookieValue)) {
          context.put("information_display", Boolean
            .valueOf(auiInfoDispCookieValue));
        } else {
          context.put("information_display", true);
        }
      } else {
        context.put("information_display", false);
      }
    } else {
      context.put("information_display", false);
    }
    context.put("information_cookie_name", informationCookie);
    context.put("information_text", informationText);
    context.put("information_title", ALEipInformation.INFORMATION_TITLE);

    // 修正 ：ポートレットの最大化画面にタブを常に表示するように修正した．
    try {
      boolean customized = (jdata.getMode() == JetspeedRunData.CUSTOMIZE);
      boolean maximized =
        customized || (jdata.getMode() == JetspeedRunData.MAXIMIZE);

      if (maximized && "true".equals(showTab)) {
        Portlets portlets =
          ((JetspeedRunData) rundata).getProfile().getDocument().getPortlets();

        Collection<PortletTab> tabs =
          getTabs(PortalToolkit.getSet(portlets), rundata, context);

        // remove "個人設定"
        for (Iterator<PortletTab> i = tabs.iterator(); i.hasNext();) {
          PortletTab tab = i.next();
          if (tab.getTitle().toString().equals("個人設定")) {
            i.remove();
          }
          if (tab.getTitle().toString().equals("システム管理")) {
            i.remove();
          }
        }

        context.put("tabs", tabs);
        context.put("menus", getMenus(portlets, rundata, context));

        String mypageId = "";
        for (Portlets p : portlets.getPortletsArray()) {
          if ("マイページ".equals(p.getTitle())) {
            mypageId = p.getId();
          }
        }
        context.put("mypageId", mypageId);

      }
    } catch (Exception e) {
      logger.error("ALVelocityPortletControl.getContent", e);
    }

    String theme = getConfig().getInitParameter("theme", "default.vm");

    String s = "";
    try {
      String template = TemplateLocator.locateControlTemplate(rundata, theme);
      TurbineVelocity.handleRequest(context, template, rundata.getOut());
    } catch (Exception e) {
      logger.error("Exception while creating content ", e);
      s = e.toString();
    }

    TurbineVelocity.requestFinished(context);

    return new StringElement(s);
  }

  /**
   * This method allows subclasses of the VelocityPortletControl to populate the
   * context of this control before rendering by the template engine.
   * 
   * @param rundata
   *          the RunData object for this request
   * @param context
   *          the Context used by the template
   */
  public void buildContext(RunData rundata, Context context) {
    // empty, used by subclasses to populate the context
  }

  /**
   * Builds a list of possible window actions for this portlet instance. For
   * best results, the portlet should also implement the PortletState interface.
   * 
   * @param rundata
   *          the request RunData
   * @param the
   *          portlet instance managed by this control
   * @return a list of ordered PortletAction objects describing the the actions
   *         available for this portlet
   */
  @SuppressWarnings({ "deprecation", "null" })
  protected List<PortletAction> buildActionList(RunData rundata,
      Portlet portlet, Context context) {
    List<PortletAction> actions = new Vector<PortletAction>();
    JetspeedLink jsLink = null;
    JetspeedRunData jdata = (JetspeedRunData) rundata;

    // disable actions option
    if (JetspeedSecurity.areActionsDisabledForAllUsers()) {
      return actions;
    }
    JetspeedUser user = jdata.getJetspeedUser();
    if (JetspeedSecurity.areActionsDisabledForAnon()
      && false == user.hasLoggedIn()) {
      return actions;
    }

    // list the available actiosn for this portlet
    if (portlet instanceof PortletState) {
      // the portlet is state aware
      PortletState state = (PortletState) portlet;
      boolean customized = (jdata.getMode() == JetspeedRunData.CUSTOMIZE);
      boolean maximized =
        customized || (jdata.getMode() == JetspeedRunData.MAXIMIZE);

      // 修正 ：最大化時とノーマル時のポートレットの表示を切り替えられるように，
      // 変数 isMaximized を追加した．jetspeed.vm で利用する．
      context.put("isMaximized", Boolean.valueOf(maximized));
      boolean infoAdded = false;

      if (state.allowCustomize(rundata)) {
        // 修正 ：ページのカスタマイズ時にタブにカスタマイズボタンを付ける．
        actions.add(new PortletAction("customize", "カスタマイズ"));
        /*
         * if (!customized) { actions.add(new PortletAction("customize",
         * "Customize")); }
         */
      } else {
        if (state.allowInfo(rundata)) {
          actions.add(new PortletAction("info", "Information"));
          infoAdded = true;
        }
      }

      if ((!customized) && state.allowPrintFriendly(rundata)) {
        actions.add(new PortletAction("print", "Print Friendly Format"));
      }

      if ((!customized) && state.allowInfo(rundata) && (!infoAdded)) {
        actions.add(new PortletAction("info", "Information"));
      }

      if ((!customized) && (!maximized) && state.allowClose(rundata)) {
        actions.add(new PortletAction("close", "Close"));
      }

      if (state.isMinimized(rundata) || maximized) {
        // 修正 ：ページのカスタマイズ時にタブに表示されていた最小化ボタンを非表示にした．
        // actions.add(new PortletAction("restore", "Restore"));
      } else {
        if (state.allowMinimize(rundata)) {
          actions.add(new PortletAction("minimize", "Minimize"));
        }

        if (state.allowMaximize(rundata)) {
          actions.add(new PortletAction("maximize", "Maximize"));
        }
      }
    } else {
      // the portlet only knows about edit and maximize
      if (portlet.getAllowEdit(rundata)) {
        actions.add(new PortletAction("info", "Information"));
      }

      if (portlet.getAllowMaximize(rundata)) {
        actions.add(new PortletAction("maximize", "Maximize"));
      }
    }

    // Now that we know which actions should be displayed,
    // build the links and put it in the context
    Iterator<PortletAction> i = actions.iterator();

    while (i.hasNext()) {
      PortletAction action = i.next();

      try {
        jsLink = JetspeedLinkFactory.getInstance(rundata);
      } catch (Exception e) {
        logger.error("ALVelocityPortletControl.buildActionList", e);
      }
      // action.setLink( jsLink.setPortletById(portlet.getID())
      // .addQueryData("action", getAction( action.getName()))
      // .toString());
      if ("customize".equals(action.getName())
        && portlet.getClass() != BasePortletSet.class) {
        action.setLink(jsLink
          .setAction(getAction("maximize"), portlet)
          .addQueryData("template", "PortletCustomizeFormScreen")
          .toString());
      } else {
        action.setLink(jsLink
          .setAction(getAction(action.getName()), portlet)
          .toString());
      }
      JetspeedLinkFactory.putInstance(jsLink);
      jsLink = null;
    }

    return actions;
  }

  // 修正 ：ノーマル表示時のポートレットの右上にメニューを配置できるように，
  // メソッド buildFunctionList を追加した．
  /**
   * 
   * @param rundata
   * @param portlet
   * @return
   */
  protected List<ALFunction> buildFunctionList(RunData rundata, Portlet portlet) {
    List<ALFunction> functions = new ArrayList<ALFunction>();
    try {

      int i = 1;
      Map<?, ?> map = portlet.getPortletConfig().getInitParameters();
      while (map.containsKey("function_mode" + i)) {
        ALFunction function = new ALFunction();
        function.setMode(portlet.getPortletConfig().getInitParameter(
          "function_mode" + i));
        function.setImage(portlet.getPortletConfig().getInitParameter(
          "function_image" + i));
        function.setCaption(portlet.getPortletConfig().getInitParameter(
          "function_caption" + i));
        if (map.containsKey("function_screen" + i)) {
          function.setScreen(true);
        }
        if (map.containsKey("function_before_function" + i)) {
          function.setBeforeFunction(portlet
            .getPortletConfig()
            .getInitParameter("function_before_function" + i));
        }
        if (map.containsKey("function_after_function" + i)) {
          function.setAfterFunction(portlet
            .getPortletConfig()
            .getInitParameter("function_after_function" + i));
        }
        functions.add(function);
        i++;
      }
    } catch (Exception e) {
      logger.error("ALVelocityPortletControl.buildFunctionList", e);
    }

    return functions;
  }

  /**
   * Transforms an Action name in Turbine valid action name, by adding a
   * controls package prefix and capitalizing the first letter of the name.
   */
  protected static String getAction(String name) {
    StringBuffer buffer = new StringBuffer("controls.");

    buffer.append(name.substring(0, 1).toUpperCase());
    buffer.append(name.substring(1, name.length()));

    return buffer.toString();
  }

  /**
   * This utility class is used to give information about the actions available
   * in a control theme template
   */
  public static class PortletAction {
    String name = null;

    String link = null;

    String alt = null;

    /**
     * Constructor
     * 
     * @param name
     *          Name of the action
     * @param alt
     *          Alternative text description (localized)
     */
    protected PortletAction(String name, String alt) {
      this.name = name;
      this.alt = alt;
    }

    public String getName() {
      return this.name;
    }

    public String getLink() {
      return this.link;
    }

    public void setLink(String link) {
      this.link = link;
    }

    public String getAlt() {
      return this.alt;
    }

  }

  /**
   * iphoneメニュー用にすべてのポートレットのリストを取り出す。
   * 
   * @param portlets
   * @param rundata
   * @param context
   * @return
   */
  private List<PortletTab> getMenus(Portlets portlets, RunData rundata,
      Context context) {
    List<PortletTab> tabs = new ArrayList<PortletTab>();
    ResultList<ALApplication> apps =
      ALApplicationService.getList(new ALApplicationGetRequest()
        .withStatus(ALApplicationGetRequest.Status.ACTIVE));
    // PanedPortletController controller =
    // (PanedPortletController) portlets.getController();

    // if portlet is a PortletSet, try to retrieve the Controller
    // we need a PanedPortletController to work properly.
    // if (portlets.getController() instanceof PanedPortletController) {
    // controller = (PanedPortletController) portlets.getController();
    // }
    int count = 0;
    for (Iterator en = portlets.getPortletsIterator(); en.hasNext();) {
      Portlets p = (Portlets) en.next();
      // ここからtabs
      String pane = p.getId();
      Collection<PortletTab> atabs =
        getTabs(PortalToolkit.getSet(p), rundata, context);
      /**
       * リンク埋め込み。
       **/
      for (PortletTab tab : atabs) {
        try {
          JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
          DynamicURI duri =
            jsLink.getLink(
              JetspeedLink.CURRENT,
              null,
              null,
              JetspeedLink.CURRENT,
              null);
          // 最大化リンクを登録する
          // tab.setMaximizeLink(controller.getPortletURI(p,
          // rundata).addQueryData(
          // "action",
          // "controls.Maximize").toString());
          duri =
            duri
              .addPathInfo(JetspeedResources.PATH_PANEID_KEY, pane)
              .addPathInfo(JetspeedResources.PATH_PORTLETID_KEY, tab.getId())
              .addQueryData(
                JetspeedResources.PATH_ACTION_KEY,
                "controls.Maximize");
          tab.setMaximizeLink(duri.toString());
          if ("GadgetsTemplate".equals(tab.getName().toString())) {
            for (ALApplication app : apps) {
              if (app.getTitle().toString().equals(tab.getTitle().toString())) {
                ALApplication gadgetApp =
                  ALApplicationService.get(new ALApplicationGetRequest()
                    .withAppId(app.getAppId().getValue()));
                tab.setIcon(gadgetApp.getIcon().getValue());
                break;
              }
            }
          }

        } catch (Exception e) {
          logger.warn("[ALVelocityPortletControl]", e);
        }
      }
      tabs.addAll(atabs);
    }
    return tabs;
  }

  /**
   * 修正：ポートレットの最大化画面時にタブを表示するために， <br />
   * クラス org.apache.jetspeed.portal.controls.VelocityPortletSetControl <br />
   * のメソッドを元に修正を加えた．
   * 
   * @param portlets
   * @param rundata
   * @param context
   * @return
   */
  private Collection<PortletTab> getTabs(PortletSet portlets, RunData rundata,
      Context context) {
    TreeSet<PortletTab> tabs =
      new TreeSet<PortletTab>(new PortletTabComparator());
    PanedPortletController controller = null;
    // if portlet is a PortletSet, try to retrieve the Controller
    // we need a PanedPortletController to work properly.
    if (portlets.getController() instanceof PanedPortletController) {
      controller = (PanedPortletController) portlets.getController();
    }

    // アクセス権限
    boolean hasAuthority =
      ALEipUtils.getHasAuthority(
        rundata,
        context,
        ALAccessControlConstants.VALUE_ACL_LIST);
    context.put(ALEipConstants.SECURE_ID, rundata.getUser().getTemp(
      ALEipConstants.SECURE_ID));

    JetspeedRunData jdata = (JetspeedRunData) rundata;

    int count = 0;
    for (Enumeration<?> en = portlets.getPortlets(); en.hasMoreElements(); count++) {
      Portlet p = (Portlet) en.nextElement();
      PortalResource portalResource = new PortalResource(p);
      if ("Activity".equals(p.getName())
        && portlets.getController().getConfig().getName().equals(
          "TabController")) {
        continue;
      }
      // Secure the tabs
      try {
        JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
        portalResource.setOwner(jsLink.getUserName());
        JetspeedLinkFactory.putInstance(jsLink);
      } catch (Exception e) {
        logger.warn("[ALVelocityPortletControl]", e);
        portalResource.setOwner(null);
      }

      boolean hasView =
        JetspeedSecurity.checkPermission(
          (JetspeedUser) jdata.getUser(),
          portalResource,
          JetspeedSecurity.PERMISSION_VIEW);
      if (!hasView) {
        continue;
      }
      // skip any closed portlet
      if ((p instanceof PortletState) && (((PortletState) p).isClosed(rundata))) {
        continue;
      }

      String mstate = p.getAttribute("_menustate", "open", rundata);
      if (mstate.equals("closed")) {
        continue;
      }
      PortletTab tab = new PortletTab();

      // Handle the portlet title

      tab.setId(p.getID());
      String title = null;
      PortletInstance pi = PersistenceManager.getInstance(p, rundata);
      if (pi != null) {
        title = pi.getTitle();
        if (title == null) {
          title = (p.getTitle() != null) ? p.getTitle() : p.getName();
        }
      }
      tab.setTitle(title);

      tab.setName(p.getName());

      tab.setPosition(p.getPortletConfig().getPosition());
      if (tabs.contains(tab)) {
        PortletTab lastTab = tabs.last();
        int nextPos = lastTab.getPosition() + 1;
        tab.setPosition(nextPos);
      }

      if (controller != null) {
        boolean isSelected = false;
        if (jdata.getMode() == JetspeedRunData.CUSTOMIZE) {
          PortletSet set = (PortletSet) (jdata).getCustomized();

          if (isTab(rundata, set.getID())) {
            // 現在選択しているタブかどうかを確認する．
            if (p.getID().equals(set.getID())) {
              isSelected = true;
              // 現在選択中のタブ ID をセッションに保存
              controller.savePaneID(rundata, p.getID());
            }
          } else {
            isSelected = controller.isSelected(p, rundata);
          }
        } else if (jdata.getMode() == JetspeedRunData.MAXIMIZE) {
          // isSelected = controller.isSelected(p, rundata);
          isSelected =
            containsPeid(rundata, (PortletSet) p, (String) jdata
              .getUser()
              .getTemp("js_peid"));
        }
        tab.setSelected(isSelected);

        // 修正 ：ポートレットの最大化画面でタブを押された場合に，
        // 最大化の情報をセッションから削除可能にするため，
        // URL にリストア処理用のクラスを付加した．
        if (getPortlet() == null) {
          tab.setLink(controller.getPortletURI(p, rundata).toString()
            + "?action=controls.Restore");
        } else {
          try {
            JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
            DynamicURI duri =
              jsLink.getLink(
                JetspeedLink.CURRENT,
                null,
                null,
                JetspeedLink.CURRENT,
                null);
            // 最大化リンクを登録する
            tab.setMaximizeLink(controller
              .getPortletURI(p, rundata)
              .addQueryData("action", "controls.Maximize")
              .toString());

            isSelected =
              containsPeid(rundata, (PortletSet) p, getPortlet().getID());
            if (isSelected) {
              duri =
                duri.addPathInfo(
                  JetspeedResources.PATH_PANEID_KEY,
                  getPortlet().getID()).addQueryData(
                  JetspeedResources.PATH_ACTION_KEY,
                  "controls.Restore");
              tab.setLink(duri.toString());
            } else {
              tab.setLink(controller.getPortletURI(p, rundata).addQueryData(
                "action",
                "controls.Restore").toString());
            }
          } catch (TurbineException e) {
            tab.setLink(controller.getPortletURI(p, rundata).addQueryData(
              "action",
              "controls.Restore").toString());
            // 最大化リンクを登録する
            tab.setMaximizeLink(controller
              .getPortletURI(p, rundata)
              .addQueryData("action", "controls.Maximize")
              .toString());
          }
          // tab.setLink(controller.getPortletURI(p, rundata).addPathInfo(
          // "js_pane", getPortlet().getID()).addQueryData("action",
          // "controls.Restore").toString());
        }
      }

      tab.setActions(buildActionList(rundata, p, context));
      tab.setAuthority(hasAuthority);

      tabs.add(tab);
    }

    return tabs;
  }

  /**
   * 修正 ：第二引数の PortletSet に第三引数のポートレットが含まれるかを検証する．
   * 
   * @param rundata
   * @param portlets
   *          タブ内に配置された Portlet 群
   * @param selectedPeid
   *          ポートレット ID
   * @return
   */
  private boolean containsPeid(RunData rundata, PortletSet portlets,
      String selectedPeid) {
    int count = 0;
    for (Enumeration<?> en = portlets.getPortlets(); en.hasMoreElements(); count++) {
      Portlet p = (Portlet) en.nextElement();
      if (p.getID().equals(selectedPeid)) {
        // PortletSet set =
        // PortalToolkit.getSet(((JetspeedRunData) rundata)
        // .getProfile()
        // .getDocument()
        // .getPortlets());
        SessionState state =
          ((JetspeedRunData) rundata).getPortletSessionState(portlets.getID());
        state.setAttribute(JetspeedResources.PATH_PANEID_KEY, portlets.getID());
        return true;
      }
    }
    return false;
  }

  /**
   * 修正 ：第二引数で指定したポートレットの ID がタブの ID かどうかを検証する．
   * 
   * @param rundata
   * @param peid
   * @return
   */
  private boolean isTab(RunData rundata, String peid) {
    if (peid == null || peid.equals("")) {
      return false;
    }
    Portlets portlets =
      ((JetspeedRunData) rundata).getProfile().getDocument().getPortlets();
    Portlets[] tabList = portlets.getPortletsArray();
    int length = tabList.length;
    for (int i = 0; i < length; i++) {
      if (tabList[i].getId().equals(peid)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 修正 ：ポートレットの最大化画面時にタブを表示するために， <br />
   * クラス org.apache.jetspeed.portal.controls.VelocityPortletSetControl <br />
   * のメソッドをコピーした．
   * 
   * @param rundata
   * @param portlets
   * @return
   */
  protected String retrievePaneIDFromSession(RunData rundata,
      PortletSet portlets) {

    // get the state for this portlet (portlet set) in this page in this session
    SessionState state =
      ((JetspeedRunData) rundata).getPortletSessionState(portlets.getID());

    // get the PANE_PARAMETER attribute
    String pane =
      (String) state.getAttribute(JetspeedResources.PATH_PANEID_KEY);

    // if not yet defined, select the first portlet set
    if (pane == null) {
      // use default
      if (portlets.size() > 0) {
        pane = portlets.getPortletAt(0).getID();
      }
    }

    return pane;
  }

  /**
   * 修正 ：ポートレットの最大化画面時にタブを表示するために， <br />
   * クラス org.apache.jetspeed.portal.controls.VelocityPortletSetControl <br />
   * のメソッドをコピーした．
   */
  public static class PortletTab {
    private final ALStringField title = new ALStringField();

    private final ALStringField name = new ALStringField();

    private boolean selected = false;

    private String link = null;

    private String maximize_link = null;

    private List<PortletAction> actions = null;

    private int position = -1;

    private String id = null;

    private final ALStringField icon = new ALStringField();

    // private final String paneid = null;

    private boolean authority = true;

    public ALStringField getTitle() {
      return this.title;
    }

    public void setTitle(String title) {
      this.title.setValue(title);
    }

    public ALStringField getName() {
      return this.name;
    }

    public void setName(String name) {
      this.name.setValue(name);
    }

    public boolean isSelected() {
      return this.selected;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    public String getLink() {
      return this.link;
    }

    public void setLink(String link) {
      this.link = link;
    }

    public String getMaximizeLink() {
      return this.maximize_link;
    }

    public void setMaximizeLink(String link) {
      this.maximize_link = link;
    }

    public List<PortletAction> getActions() {
      return (this.actions == null)
        ? new Vector<PortletAction>()
        : this.actions;
    }

    public void setActions(List<PortletAction> actions) {
      this.actions = actions;
    }

    public int getPosition() {
      return position;
    }

    public void setPosition(int pos) {
      position = pos;
    }

    public String getId() {
      return id;
    }

    public void setId(String tabId) {
      id = tabId;
    }

    public boolean getAuthority() {
      return authority;
    }

    public void setAuthority(boolean flg) {
      authority = flg;
    }

    public ALStringField getIcon() {
      return this.icon;
    }

    public void setIcon(String icon) {
      this.icon.setValue(icon);
    }

  }

  /**
   * 修正 ：ポートレットの最大化画面時にタブを表示するために追加した。
   */
  public static class PortletTabComparator implements Comparator<PortletTab>,
      Serializable {

    /**
     * 
     * @param o1
     * @param o2
     * @return
     */
    @Override
    public int compare(PortletTab o1, PortletTab o2) {
      try {
        PortletTab pt1 = o1;
        PortletTab pt2 = o2;
        int pos1 = pt1.getPosition();
        int pos2 = pt2.getPosition();

        if (pos1 < pos2) {
          return -1;
        } else if (pos1 > pos2) {
          return 1;
        } else {
          return 0;
        }
      } catch (ClassCastException e) {
        logger.error("ALVelocityPortletControl.compare", e);
        return 0;
      }
    }
  }
}
