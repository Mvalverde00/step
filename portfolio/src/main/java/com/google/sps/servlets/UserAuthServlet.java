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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that deletes all comment data */
@WebServlet("/auth")
public class UserAuthServlet extends HttpServlet {

  private static final UserService userService = UserServiceFactory.getUserService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    notLoggedInPage(response, "continue", "/");
  }

  public static void notLoggedInPage(HttpServletResponse response, String message, String redirectUrl) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("text/html;");

    String loginUrl = userService.createLoginURL(redirectUrl);
    response.getWriter().println(
        "<p> Sorry, you must be logged in to " + message +
        "<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
  }

  public static boolean isLoggedIn() {
    return userService.isUserLoggedIn();
  }

}
