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

package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

/** A comment on a webpage */
public final class Comment {

  private static final DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  private final long id;
  private final String message;
  private final long datePosted;
  // A value of 0 denotes a top-level comment.
  private final long parent;
  // The id of the top-level comment this comment belongs to.  0 for top comment
  private final long root;
  private final long score;
  private final String sender;

  public Comment(long id, String message, long datePosted, long parent, long root, long score, String sender) {
    this.id = id;
    this.message = message;
    this.datePosted = datePosted;
    this.parent = parent;
    this.root = root;
    this.score = score;
    this.sender = sender;
  }

  public Comment(Entity e, String sender) {
    this(
        e.getKey().getId(),
        (String) e.getProperty("message"),
        (long) e.getProperty("datePosted"),
        (long) e.getProperty("parent"),
        (long) e.getProperty("root"),
        (long) e.getProperty("score"),
        sender);
  }

  public long getId() {
    return this.id;
  }

  public static Comment createComment(Entity e) {
    try {
      Entity userInfo = ds.get((Key) e.getProperty("poster"));
      return new Comment(e, (String) userInfo.getProperty("username"));
    } catch (EntityNotFoundException err) {
      System.err.println(
          "Error finding user associated with comment key: " + e.getKey());
      return null;
    }
  }

  public static Entity createCommentEntity(String message, long parent, long root, long score, Key poster) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("message", message);
    commentEntity.setProperty("datePosted", System.currentTimeMillis());
    commentEntity.setProperty("parent", parent);
    commentEntity.setProperty("root", root);
    commentEntity.setProperty("score", score);
    commentEntity.setProperty("poster", poster);

    return commentEntity;
  }

}
