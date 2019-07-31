const express = require("express");
const authorize = require("./authorize");

const app = express();

app.post("/sts/authorize", (req, res) => {
  res
    .type("application/xml")
    .status(authorize.status)
    .send(authorize.body);
});

module.exports = app;
