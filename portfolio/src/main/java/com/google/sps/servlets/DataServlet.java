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
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final Gson gson = new Gson();
  private static final String host;
  private static final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
  private static final int DEFAULT_RECORDS_SHOWN = 15;

  static {
    String environmentHost = System.getenv("SERVER_HOST_NAME");
    if (environmentHost == null) {
      host = "michael-leoyao-step-2020.appspot.com";
    } else {
      host = environmentHost;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("datePosted", SortDirection.DESCENDING);
    PreparedQuery results = ds.prepare(query);

    int recordsToReturn = getRecordsToReturn(request);
    List<Comment> comments = new ArrayList<>();
    for (Entity e : results.asIterable(FetchOptions.Builder.withLimit(recordsToReturn))) {
      comments.add(new Comment(e));
    }

    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String newComment = request.getParameter("comment");
    if (newComment != null && !newComment.isEmpty()) {
      // TODO: Pass in actual values for parent and score.
      Entity commentEntity = Comment.createComment(newComment, 0, 0);
      ds.put(commentEntity);
    }

    response.sendRedirect(getRedirect(request));
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

  private String getRedirect(HttpServletRequest request) {
    String referer = request.getHeader("referer");
    String refererHost;

    // It's possible the user manually set a referer that is not a valid URI
    try {
      refererHost = new URI(referer).getHost();
    } catch (URISyntaxException e) {
      refererHost = "";
    }

    /**
     * Allow handling of comments sections on multiple pages.  For example, a
     * request made from www.example.com and www.example.com/page2.html will
     * both have www.example.com as their refererHost, but the referers will
     * point to different pages (namely `/` vs `/page2.html` ).
     * If refererHost does not equal host, that means some external tool
     * tried to set referer, so in that case we return the home page.
     */
    if (!refererHost.equals(host)) {
      return host;
    }
    return referer;
  }
}
