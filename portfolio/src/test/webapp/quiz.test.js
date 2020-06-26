const FakeRNG = require('./fake_rng.js');
const shuffle = require('../../main/webapp/quiz.js');

test('Shuffle on empty array', () => {
  let arr = [];
  expect(shuffle(arr)).toEqual([]);
});

test('Shuffle on small array', () => {
  let arr = [1,2,3];
  expect(shuffle(arr, new FakeRNG(0))).toEqual([1,2,3]);

  arr = [1,2,3];
  expect(shuffle(arr, new FakeRNG(0.99))).toEqual([3,1,2]);
});

// Helper function for testing conservation of elements
function countArrayElements(arr) {
  let count = {};

  for (let i = 0; i < arr.length; i++) {
    if (arr[i] in count) {
      count[arr[i]]++;
    } else {
      count[arr[i]] = 1;
    }
  }

  return count;
}

/**
 * If the shuffler is working properly, every element in the original array
 * should be in the new array
 */
test('Shuffle conserves elements', () => {
  const iterations = 10000;
  const max_range = 100;

  let arr = [];
  for (let i = 0; i < iterations; i++) {
    arr[i] = Math.floor(Math.random() * max_range);
  }

  let originalCount = countArrayElements(arr);
  arr = shuffle(arr);
  let newCount = countArrayElements(arr);
  expect(originalCount).toEqual(newCount);
});
