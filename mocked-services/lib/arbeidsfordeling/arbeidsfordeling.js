const fs = require("fs");
const path = require("path");

module.exports = {
  status: 200,
  body: fs.readFileSync(path.resolve(__dirname, "arbeidsfordeling.xml"), "utf8")
};
