const { faker } = require('@faker-js/faker');

module.exports = {
  generateRandomName: function (userContext, events, done) {
    userContext.vars.randomName = faker.commerce.productName();
    return done();
  },
};
