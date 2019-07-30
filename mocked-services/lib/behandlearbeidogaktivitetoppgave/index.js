const express = require("express");
const behandlearbeidogaktivitetoppgave = require("./bestillOppgave");
const ping = require("../shared/ping");

const app = express();

app.post("/behandleArbeidOgAktivitetOppgave/", (req, res) => {
  res
    .type("application/xml")
    .status(bestillOppgave.status)
    .send(bestillOppgave.body);
});

app.post("/ping", (req, res) => {
  res
    .type("application/xml")
    .status(ping.status)
    .send(ping.body);
});

module.exports = app;
