FROM node:24-alpine AS builder
WORKDIR /workspace
RUN corepack enable
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/admin/package.json apps/admin/package.json
COPY packages/api-contract/package.json packages/api-contract/package.json
COPY packages/design-tokens/package.json packages/design-tokens/package.json
RUN pnpm install --frozen-lockfile
COPY apps/admin apps/admin
COPY packages packages
RUN pnpm --filter @leisure-brew/admin build

FROM nginx:1.29-alpine
COPY apps/admin/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /workspace/apps/admin/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=10s --timeout=3s --retries=5 CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1

