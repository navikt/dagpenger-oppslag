const soapAction = soapAction => (req, res, next) => {
  const stripQuotes = str => str.replace(/\"/g, "");
  return stripQuotes(req.header("soapaction")) == soapAction
    ? next()
    : next("route");
};

module.exports = soapAction;
