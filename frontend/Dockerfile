FROM node:20-alpine AS builder
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
ARG NEXT_PUBLIC_AUTH_PROVIDER=local
ENV NEXT_PUBLIC_AUTH_PROVIDER=$NEXT_PUBLIC_AUTH_PROVIDER

COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

RUN addgroup --system --gid 1001 canopy && adduser --system --uid 1001 nextjs
COPY --from=builder --chown=nextjs:canopy /app/.next/standalone ./
COPY --from=builder --chown=nextjs:canopy /app/.next/static ./.next/static
COPY --from=builder --chown=nextjs:canopy /app/public ./public

USER nextjs
EXPOSE 3000
CMD ["node", "server.js"]
