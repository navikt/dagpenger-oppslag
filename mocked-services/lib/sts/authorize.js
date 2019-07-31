const fs = require("fs");
const path = require("path");

module.exports = {
  status: 200,
  body: fs.readFileSync(path.resolve(__dirname, "authorize.xml"), "utf8")
};
