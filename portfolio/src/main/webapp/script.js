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

function addRandomText(choices, elementId) {
  const choice = choices[Math.floor(Math.random() * choices.length)];

  const container = document.getElementById(elementId);
  container.innerText = choice;
}


/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  addRandomText([
    'Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!',
  ], 'greeting-container');
}

function addRandomTrivia() {
  addRandomText([
    'My favorite color is green.',
    'Although I live in New Jersey, I went to high school in New York.',
    'I know two dead languages-- Latin and Ancient Greek!',
    'My middle name is McAlarney',
  ], 'trivia-container');
}

function displayComments() {
  const records = document.getElementById("records").value;
  fetch(`/data?records=${records}`)
      .then(response => response.json())
      .then(comments => {
        let container = document.getElementById('comment-container');
        container.innerHTML = '';
        for (let comment of comments) {
          const commentElement = createCommentElement(comment);
          container.appendChild(commentElement);
        }
      });
}

function createCommentElement(commentJson) {
  let comment = document.createElement('div');
  comment.setAttribute('id', commentJson.id);

  let paragraph = document.createElement('p');
  paragraph.innerText = commentJson.message;
  comment.appendChild(paragraph);

  let replySpan = document.createElement('span');
  replySpan.innerText = 'Reply';
  replySpan.onclick = () => {
    replySpan.insertAdjacentHTML('beforebegin', createReplyForm(commentJson));
    comment.removeChild(replySpan);
  };
  comment.appendChild(replySpan);

  return comment;
}

function createReplyForm(commentJson) {
  return `
      <form action="/data" method="POST">
        <label for="comment">Enter comment:</label>
        <input name="comment" type="text"/>

        <input type="hidden" name="parent" value="${commentJson.message}"/>

        <input type="submit" value="Send Comment!"/>
      </form>
      `;
}

function deleteAllComments() {
  fetch('/delete-data', {method:'post'}).then(() => displayComments());
}

function buildCommentTree(commentJsons) {
  let root = new TreeNode({"id":0});
  buildCommentTreeHelper(root, commentJsons);
  return root;
}

function buildCommentTreeHelper(parentNode, commentJsons) {
  for (let commentJson of commentJsons) {
    if (parentNode.data.id == commentJson.parent) {
      let childNode = new TreeNode(commentJson);
      buildCommentTreeHelper(childNode, commentJsons);
      parentNode.appendChild(childNode);
    }
  }
}
