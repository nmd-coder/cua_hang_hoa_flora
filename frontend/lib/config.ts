export function getGatewayUrl(): string {
  return process.env.GATEWAY_URL || (process.env.NODE_ENV === "production" ? "http://api-gateway:8080" : "http://localhost:8080");
}

