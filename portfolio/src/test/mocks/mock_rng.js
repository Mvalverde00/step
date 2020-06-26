
/**
 * Because this class has a .random() method, it can be passed in to a function
 * to be used instead of Math.random(), allowing for a controlled random number
 * generator
*/
class MockRNG {
  constructor(initialValue = 0) {
    this.setReturn(initialValue);
  }

  setReturn(value) {
    this.toReturn = value;
  }

  random() {
    return this.toReturn;
  }
}

module.exports = MockRNG;
