// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.UserAuthResponse;
import com.google.sps.util.ServletUtil;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles user authentiction */
@WebServlet("/auth")
public class UserAuthServlet extends HttpServlet {

  private static final UserService userService = UserServiceFactory.getUserService();
  private static final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    boolean loggedIn = isLoggedIn();
    String redirectURL  = ServletUtil.getRedirect(request);
    // loginUrl if not currently logged in, logout url if currently logged in.
    String logXUrl = loggedIn ? userService.createLogoutURL(redirectURL) :
        userService.createLoginURL(redirectURL);
    String username = loggedIn ? getUsername() : "";

    UserAuthResponse UAResponse =
        new UserAuthResponse(loggedIn, logXUrl, username);

    response.getWriter().println(ServletUtil.toJson(UAResponse));
  }

  public static void notLoggedInPage(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("text/html;");

    String redirectUrl = ServletUtil.getRedirect(request);
    String loginUrl = userService.createLoginURL(redirectUrl);
    response.getWriter().println(
        "<p> Sorry, you must be logged in to " + message +
        "<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
  }

  public static boolean isLoggedIn() {
    return userService.isUserLoggedIn();
  }

  public static String getEmail() {
    return userService.getCurrentUser().getEmail();
  }

  public static String getId() {
    return userService.getCurrentUser().getUserId();
  }

  public static Entity getUserEntity() {
    Query query = new Query("UserInfo").setFilter(new FilterPredicate(
        Entity.KEY_RESERVED_PROPERTY,
        Query.FilterOperator.EQUAL,
        KeyFactory.createKey("UserInfo", getId())));
    PreparedQuery result = ds.prepare(query);

    Entity userEntity = result.asSingleEntity();
    return userEntity;
  }

  public static String getUsername() {
    Entity userEntity = getUserEntity();
    return getUsername(userEntity);
  }

  public static String getUsername(Entity userEntity) {
    if (userEntity == null) {
      return "";
    }

    return (String) userEntity.getProperty("username");
  }
}
