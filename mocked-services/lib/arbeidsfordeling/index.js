const express = require("express");
const arbeidsfordeling = require("./arbeidsfordeling");
const ping = require("../shared/ping");

const app = express();

app.post("/arbeidsfordeling", (req, res) => {
  res
    .type("application/xml")
    .status(arbeidsfordeling.status)
    .send(arbeidsfordeling.body);
});

app.post("/ping", (req, res) => {
  res
    .type("application/xml")
    .status(ping.status)
    .send(ping.body);
});

module.exports = app;
