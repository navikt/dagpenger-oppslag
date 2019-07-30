FROM node:12

ENV NODE_ENV=production PORT=3000

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

HEALTHCHECK --interval=5s CMD [ "node", "healthcheck.js" ]

USER node
# Run node directly, so "npm" does not swallow the SIGINT/SIGTERM signals
CMD [ "node", "server.js" ]