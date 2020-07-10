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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.sps.data.Comment;
import com.google.sps.servlets.UserAuthServlet;
import com.google.sps.util.ServletUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
  private static final int DEFAULT_RECORDS_SHOWN = 15;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<Comment> comments = new ArrayList<>();

    Query rootCommentQuery = new Query("Comment")
        .setFilter(FilterOperator.EQUAL.of("parent",0))
        .addSort("datePosted", SortDirection.DESCENDING);
    PreparedQuery rootComments = ds.prepare(rootCommentQuery);

    int recordsToReturn = getRecordsToReturn(request);
    List<Long> rootCommentIds = new ArrayList<>();
    for (Entity e : rootComments.asIterable(FetchOptions.Builder.withLimit(recordsToReturn))) {
      Comment c = Comment.createComment(e);
      if (c != null) {
        comments.add(c);
        rootCommentIds.add(c.getId());
      }
    }

    // Query with IN operator breaks if list is empty, so only run if nonempty
    if (!rootCommentIds.isEmpty()) {
      Query childCommentQuery = new Query("Comment")
          .setFilter(new FilterPredicate("root", FilterOperator.IN, rootCommentIds))
          .addSort("datePosted", SortDirection.DESCENDING);
      PreparedQuery childComments = ds.prepare(childCommentQuery);
      for (Entity e : childComments.asIterable()) {
        Comment c = Comment.createComment(e);
        if (c != null) {
          comments.add(c);
        }
      }
    }

    response.setContentType("application/json;");
    response.getWriter().println(ServletUtil.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Make sure the user is logged in.
    if (!UserAuthServlet.isLoggedIn()) {
      UserAuthServlet.notLoggedInPage(request, response, "send a comment.");
      return;
    }
    if (UserAuthServlet.getUsername().equals("")) {
      response.sendRedirect("/profile");
      return;
    }

    String message = request.getParameter("comment");
    long parent = 0;
    long root = 0;
    long score = 0; // TODO: populate this with real data somewhere

    try {
      parent = getNonnegativeLong(request, "parent");
      root = getNonnegativeLong(request, "root");
    } catch(NumberFormatException | IndexOutOfBoundsException e) {
      System.err.println(e);
      response.setContentType("text/html;");
      response.getWriter().println(
          "<h1>An error has occured</h1>" +
          "<p> Sorry, your last request resulted in an internal error.</p>");
      return;
    }
    if (message != null && !message.isEmpty()) {
      Entity commentEntity = Comment.createCommentEntity(
          message, parent, root, score, UserAuthServlet.getUserEntity().getKey());
      ds.put(commentEntity);
    }
    response.sendRedirect(ServletUtil.getRedirect(request));
  }

  private long getNonnegativeLong(HttpServletRequest request, String parameterName) throws NumberFormatException, IndexOutOfBoundsException{
    String parameterString = request.getParameter(parameterName);

    long parameter = Long.parseLong(parameterString);
    if (parameter < 0) {
      throw new IndexOutOfBoundsException(parameterName + " should not be negative!");
    }

    return parameter;
  }

  private int getRecordsToReturn(HttpServletRequest request) {
    String recordsString = request.getParameter("records");

    int records;
    try {
      records = Integer.parseInt(recordsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert '" + recordsString + "' to int.");
      return DEFAULT_RECORDS_SHOWN;
    }

    if (records < 1 || records > 100) {
      System.err.println("The request number of records is out of range.");
      return DEFAULT_RECORDS_SHOWN;
    }

    return records;
  }

}
