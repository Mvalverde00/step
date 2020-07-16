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

package com.google.sps;

/**
 * Utility class for creating greeting messages.
 */
public class Greeter {

  public static final char illegalChars[] = {'@', '$', '#'};

  /**
   * Returns a greeting for the given name.
   */
  public String greet(String name) {
    name = removeIllegalChars(name);
    name = trim(name);
    return "Hello " + name;
  }

  public String trim(String str) {
    return trim(str, 0, str.length() - 1);
  }

  private String trim(String str, int start, int end) {
    if (str.charAt(start) == ' ') {
      return trim(str, start + 1, end);
    }
    if (str.charAt(end) == ' ') {
      return trim(str, start, end - 1);
    }

    return str.substring(start, end + 1);
  }

  public String removeIllegalChars(String str) {
    StringBuilder newString = new StringBuilder();

    for (char c : str.toCharArray()) {
      if (!contains(illegalChars, c)) {
        newString.append(c);
      }
    }

    return newString.toString();
  }
  
  private boolean contains(char arr[], char c) {
    for (char arrChar : arr) {
      if (c == arrChar) {
        return true;
      }
    }
    return false;
  }
}
