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

import com.google.appengine.api.datastore.Entity;

/**
 * A comment on a webpage
 * @param id An identifier which uniquely defines a comment
 * @param message The actual text of the comment which will be displayed
 * @param date_posted The time the comment was submitted
 * @param parent The id of the comment's parent.  0 if top level comment
 * @param score A rating of the comment, decided by users' upvotes/downvotes

 */
public final class Comment {

  private final long id;
  private final String message;
  private final long date_posted;
  private final long parent;
  private final long score;

  public Comment(long id, String message, long date_posted, long parent, long score) {
    this.id = id;
    this.message = message;
    this.date_posted = date_posted;
    this.parent = parent;
    this.score = score;
  }

  public Comment(Entity e) {
    this(e.getKey().getId(), (String) e.getProperty("message"),
        (long) e.getProperty("date_posted"), (long) e.getProperty("parent"),
        (long) e.getProperty("score"));
  }

  public static Entity createComment(String message, long parent, long score) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("message", message);
    commentEntity.setProperty("date_posted", System.currentTimeMillis());
    commentEntity.setProperty("parent", parent);
    commentEntity.setProperty("score", score);

    return commentEntity;
  }

}
