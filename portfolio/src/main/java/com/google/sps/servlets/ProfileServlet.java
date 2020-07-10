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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.sps.data.ProfileResponse;
import com.google.sps.util.ServletUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that deletes all comment data */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

  private static final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!UserAuthServlet.isLoggedIn()) {
      UserAuthServlet.notLoggedInPage(request, response, "view your profile.");
      return;
    }

    response.sendRedirect("/profile.html");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!UserAuthServlet.isLoggedIn()) {
      UserAuthServlet.notLoggedInPage(request, response, "set a username.");
      return;
    }

    response.setContentType("application/json");

    String username = request.getParameter("username");
    if (!usernameMeetsCriteria(username)) {
      ProfileResponse pr =
          new ProfileResponse(false, "Username does not meet requirements");
      response.getWriter().println(ServletUtil.toJson(pr));
      return;
    }

    // If the entity doesn't exist, we need to define some extra properties
    Entity userEntity = UserAuthServlet.getUserEntity();
    if (userEntity == null) {
      userEntity = new Entity("UserInfo", UserAuthServlet.getId());
      userEntity.setProperty("email", UserAuthServlet.getEmail());
      userEntity.setProperty("joinDate", System.currentTimeMillis());
    }
    userEntity.setProperty("username", username);

    ds.put(userEntity);

    ProfileResponse pr = new ProfileResponse(true, "Username updated");
    response.getWriter().println(ServletUtil.toJson(pr));
  }

  private boolean usernameMeetsCriteria(String username) {
    if (username == null) {
      return false;
    }

    if (username.length() < 4 || username.length() > 30) {
      return false;
    }
    // alphanumeric characters and spaces allowed
    if (!username.matches("[A-Za-z0-9 ]+")) {
      return false;
    }

    return true;
  }
}
