function updateUsername() {
  parameters = {
    method: 'POST',
    body: new URLSearchParams(`username=${document.getElementById('username').value}`)
  };
  console.l
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
  giveUserFeedback('Your username has been updated!', '#00ff00');
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
  feedbackDiv.innerText = message;
};
