const express = require("express");
const soapAction = require("../middleware/soap-action");
const hentPersonResponse = require("./hent-person");
const ping = require("../shared/ping");

const app = express();

const hentPersonAction = soapAction(
  "http://nav.no/tjeneste/virksomhet/person/v3/Person_v3/hentPersonRequest"
);

app.post("/person", hentPersonAction, (req, res) => {
  console.log("perosn");
  res
    .type("application/xml")
    .status(hentPersonResponse.status)
    .send(hentPersonResponse.body);
});

const pingAction = soapAction(
  "http://nav.no/tjeneste/virksomhet/person/v3/Person_v3/pingRequest"
);

app.post("/person", pingAction, (req, res) => {
  console.log("ping");
  res
    .type("application/xml")
    .status(ping.status)
    .send(ping.body);
});

module.exports = app;
