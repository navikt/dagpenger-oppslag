const winston = require("winston");
const { format } = require("logform");
const { combine, timestamp, logstash, cli } = format;

const options = {
  console: {
    level: "info",
    handleExceptions: true,
    format: combine(timestamp(), logstash())
  }
};

if (process.env.NODE_ENV !== "production") {
  options.console = {
    ...options.console,
    ...{
      level: "silly",
      format: combine(cli())
    }
  };
}

const logger = winston.createLogger({
  transports: [new winston.transports.Console(options.console)]
});

logger.stream = {
  write: message => {
    logger.info(message);
  }
};

logger.logErrors = (err, req, res, next) => {
  // Workaround for https://github.com/winstonjs/winston/issues/1338
  logger.error({ message: err instanceof Error ? err.stack : err });
  next(err);
};

module.exports = logger;
