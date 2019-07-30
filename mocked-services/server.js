const express = require("express");
const morgan = require("morgan");
const logger = require("./lib/logger");
const sts = require("./lib/sts");
const person = require("./lib/person");

const PORT = process.env.PORT || 8080;
const server = express()
  .use(morgan("combined", { stream: logger.stream }))
  .use(sts)
  .use(person)
  .get("/isReady", (req, res) => {
    res.sendStatus(200);
  })
  .get("/isAlive", (req, res) => {
    res.sendStatus(200);
  })
  .use(logger.logErrors)
  .listen(PORT, () => {
    console.log(`Running mock server on port ${PORT}!`);
  });

process.on("SIGINT", function() {
  server.close(() => {
    process.exit(0);
  });
});
