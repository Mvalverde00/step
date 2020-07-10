function updateUsername() {
  parameters = {
    method: 'POST',
    body: new URLSearchParams(`username=${document.getElementById('username').value}`)
  };
  fetch(`/profile`, parameters)
      .then(response => response.json())
      .then(response => {
        if (response.success) {
          giveUserSuccess();
        } else {
          giveUserFailure();
        }
      });

  return false;
}

function giveUserSuccess() {
  giveUserFeedback(`
    <p>Your username has been updated!</p>
    <p><a href="/">Click here</a> to go back to the home page</p>`,
    '#00ff00');
}

function giveUserFailure() {
  giveUserFeedback(
      `Error: your username does not match the specified criteria!
       Please reread the criteria and try again`,
      '#ff0000');
}

function giveUserFeedback(message, color) {
  let feedbackDiv = document.getElementById('user-feedback');

  feedbackDiv.style['background-color'] = color;
  feedbackDiv.innerHTML = message;
};

function populateUsername() {
  fetch('/auth')
      .then(response => response.json())
      .then(authJson => {
        let usernameInput = document.getElementById('username');
        if (authJson.username == "") {
          usernameInput.placeholder = "Enter username here."
        } else {
          usernameInput.value = authJson.username;
        }
      });
}
