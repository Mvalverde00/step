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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that manages comment voting system */
@WebServlet("/vote")
public class VoteServlet extends HttpServlet {

  private static final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long commentId = Long.parseLong(request.getParameter("commentId"));
    long newValue = Long.parseLong(request.getParameter("value"));
    String userId = UserAuthServlet.getId();

    Key voteKey = KeyFactory.createKey("Vote", commentId + userId);

    long deltaValue;
    Entity userVote;
    try {
      userVote = ds.get(voteKey);
      long oldValue = (long) userVote.getProperty("value");
      deltaValue = newValue - oldValue;

      userVote.setProperty("value", newValue);
    } catch (EntityNotFoundException e) {
      // If no vote was found, this is the users first time voting
      userVote = new Entity("Vote", commentId + userId);
      deltaValue = newValue;

      userVote.setProperty("value", newValue);
    }

    Key commentKey = KeyFactory.createKey("Comment", commentId);
    try {
      Entity comment = ds.get(commentKey);
      long newScore = (long) comment.getProperty("score") + deltaValue;
      comment.setProperty("score", newScore);
      ds.put(comment);
      ds.put(userVote);
    } catch (EntityNotFoundException e) {
      // If the target comment wasn't found, abort the operation.
      System.err.println("Failed to find comment with id " + commentId);
      return;
    }
  }

}
