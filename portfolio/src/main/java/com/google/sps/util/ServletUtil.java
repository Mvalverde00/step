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

package com.google.sps.util;

import com.google.gson.Gson;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class containing useful functions for Servlets */
public final class ServletUtil {

  private static final String host;
  private static final Gson gson = new Gson();

  static {
    String environmentHost = System.getenv("SERVER_HOST_NAME");
    if (environmentHost == null) {
      host = "michael-leoyao-step-2020.appspot.com";
    } else {
      host = environmentHost;
    }
  }

  public static String getRedirect(HttpServletRequest request) {
    String referer = request.getHeader("referer");
    String refererHost;

    // It's possible the user manually set a referer that is not a valid URI
    try {
      refererHost = new URI(referer).getHost();
    } catch (URISyntaxException | NullPointerException e) {
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

  public static String toJson(Object obj) {
    return gson.toJson(obj);
  }

}
