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


// Fisher-Yates shuffle algorithm
function shuffle(arr) {
  for (let i = 0; i < arr.length; i++) {
    const index = i + Math.floor(Math.random() * (arr.length - i));

    const temp = arr[i];
    arr[i] = arr[index];
    arr[index] = temp;
  }

  return arr;
};

/**
 * This class contains all the data necessary to create a question
 * that can be answered by a simple radio-button form.
 */
class QuizQuestion {
  constructor(question, choices, answerIndex) {
    /** @type {string} The question to be asked */
    this.question = question;

    /**
     * @type {List<string>} A list of an arbitrary number of possible
     *    answers to the question, exactly one of which should be correct
     */
    this.choices = choices;

    /** @type {int} The index of the choice in choices that is correct*/
    this.answerIndex = answerIndex;
  }
};

/**
 * This class contains all the data necessary to create a quiz
 * composed of consecutive QuizQuestions, render it to the screen,
 * and process the user's input.
 */
class Quiz {
  constructor(quizQuestions, divID, feedbackDivID) {
    /** @type {List<QuizQuestion>} The Quiz Questions to be asked*/
    this.quizQuestions = shuffle(quizQuestions);

    this.currQuizQuestion = 0;
    this.correctAnswers = 0;

    /**
     * @type {HTMLElement} The div in which the question prompt and
     *    answer choices will be rendered
     */
    this.container = document.getElementById(divID);
    /**
     * @type {HTMLElement} The div in which feedback to the user
     *    will be rendered
     */
    this.feedbackContainer = document.getElementById(feedbackDivID);

    /** @type {string} The value of the radio inputs' name field*/
    this.radioName = 'question';

    this.correctColor = '#00ff00';
    this.incorrectColor = '#ff0000';
    this.warningColor = '#ffff00';
    }

  getNextQuizQuestion() {
    if (this.currQuizQuestion >= this.quizQuestions.length) {
      throw RangeError('No remaining quiz questions');
    }

    return this.quizQuestions[this.currQuizQuestion];
  }

  getRadioInputString(choice, index) {
    return `<input type="radio" id="${choice}" name="${this.radioName}"
        value="${index}"> <label for="${choice}">${choice}</label> <br>`;
  }

  presentNextQuestion() {
    const quizQuestion = this.getNextQuizQuestion();

    this.container.innerHTML = '';

    this.container.insertAdjacentHTML('beforeend',
        `<p>Question ${this.currQuizQuestion + 1}: ${quizQuestion.question}</p>`
    );

    for (let i = 0; i < quizQuestion.choices.length; i++) {
      const radio = this.getRadioInputString(quizQuestion.choices[i], i);
      this.container.insertAdjacentHTML('beforeend', radio);
    }

    const submitButton = document.createElement('button');
    submitButton.innerHTML = 'Submit Answer';

    submitButton.onclick = () => {
      this.processAnswer();
    };

    this.container.insertAdjacentElement('beforeend', submitButton);

  };

  processAnswer() {
    let checkedRadio =
        document.querySelector(`input[name="${this.radioName}"]:checked`);

    this.feedbackContainer.innerHTML = '';
    let p = document.createElement('p');
    p.style['display'] = 'inline';

    if (checkedRadio == null) {
      p.innerHTML = 'Please Select an answer before submitting!';
      this.feedbackContainer.insertAdjacentElement('beforeend', p);
      this.feedbackContainer.style['background-color'] =
          this.warningColor;
      return;
    }

    let answer = parseInt(checkedRadio.value);
    if (answer === this.quizQuestions[this.currQuizQuestion].answerIndex) {
      p.innerHTML = 'Correct!  Nice job.';
      this.feedbackContainer.style['background-color'] =
          this.correctColor;
      this.correctAnswers++;
    } else {
      p.innerHTML = 'Sorry, that wasn\'t correct.';
      this.feedbackContainer.style['background-color'] =
          this.incorrectColor;
    }
    this.feedbackContainer.insertAdjacentElement('beforeend', p);

    this.currQuizQuestion++;
    if (this.currQuizQuestion >= this.quizQuestions.length) {
      this.presentGameOverStats();
    } else {
      this.presentNextQuestion();
    }
  }

  presentGameOverStats(){
    this.container.innerHTML = "";

    this.container.insertAdjacentHTML('beforeend',
        `<p>
           Thank you for taking the time to play this trivia game.
           You answered <b>${this.correctAnswers}</b> questions correctly out of
           <b>${this.quizQuestions.length}</b>.
         </p>
         <p><a href="/index.html">Go back to the main page?</a></p>`
    );
  }
};

window.onload = () => {
  const q1 = new QuizQuestion('Where am I from?', [
    'New York', 'New Jersey', 'California', 'Connecticut',
  ], 1);
  const q2 = new QuizQuestion('What is my dog\'s name?', [
    'Coffee', 'Doughnut', 'Biscuit', 'Bailey',
  ], 2);
  const q3 = new QuizQuestion(
      'Which of the following areas am I most interested in?', [
        'Virtual Reality', 'Machine Learning', 'App Development',
        'Computational Biology',
      ], 0);
  const q4 = new QuizQuestion(
      'Which of the following languages have I never studied?', [
        'Latin', 'English', 'Ancient Greek', 'Spanish',
      ], 3);
  const q5 = new QuizQuestion('What is my favorite color?', [
    'Red', 'Green', 'Cyan', 'Magenta',
  ], 1);

  const quiz =
      new Quiz([q1, q2, q3, q4, q5], 'trivia-display', 'trivia-feedback');
  quiz.presentNextQuestion();

}
