# Cloudflare Tunnel Setup Prompt

> Copy everything below the line and paste it as a prompt to Claude.

---

## Context

I have a multi-project workspace at `~/Documents/claude/` running several Docker services via `platform/docker-compose.yml`. I've purchased the domain **rakha.xyz** on Spaceship, added it to my Cloudflare account (free plan), and already changed the nameservers on Spaceship to point to Cloudflare. The domain is now active on Cloudflare.

My Cloudflare account uses Google login (email: markandey91@gmail.com).

## What's Running

| Service                      | Local Port | Target Subdomain     |
|------------------------------|------------|----------------------|
| Portfolio Tracker Frontend   | 5173       | rakha.xyz            |
| Portfolio Tracker API        | 8080       | api.rakha.xyz        |
| Admin Nexus                  | 5174       | admin.rakha.xyz      |
| MCP Gateway                  | 9080       | (internal only)      |
| Knowledge Store              | 3010       | (internal only)      |

Start the stack with:
```bash
cd ~/Documents/claude/platform
docker compose up -d
```

## What I Need You To Do

Complete the following steps **in order**. Pause and confirm with me if anything fails.

### 1. Install `cloudflared`

- Detect my OS (macOS or Linux)
- Install `cloudflared` using the appropriate method (brew on macOS, apt or binary on Linux)
- Verify installation with `cloudflared --version`

### 2. Authenticate `cloudflared`

- Run `cloudflared tunnel login`
- This will open a browser — I'll select `rakha.xyz` from the list
- Wait for me to confirm the auth is complete before proceeding

### 3. Create a Tunnel

- Run `cloudflared tunnel create rakha`
- Capture the **tunnel UUID** and **credentials file path** from the output
- Confirm both values with me before proceeding

### 4. Create the Config File

Create `~/.cloudflared/config.yml` with the following structure (replace UUID and credentials path with actual values from step 3):

```yaml
tunnel: <TUNNEL_UUID>
credentials-file: <CREDENTIALS_FILE_PATH>

ingress:
  # Portfolio Tracker Frontend
  - hostname: rakha.xyz
    service: http://localhost:5173

  # Portfolio Tracker API
  - hostname: api.rakha.xyz
    service: http://localhost:8080

  # Admin Nexus
  - hostname: admin.rakha.xyz
    service: http://localhost:5174

  # Catch-all (required)
  - service: http_status:404
```

### 5. Create DNS Records via Tunnel

Run these commands to create CNAME records in Cloudflare automatically:

```bash
cloudflared tunnel route dns rakha rakha.xyz
cloudflared tunnel route dns rakha api.rakha.xyz
cloudflared tunnel route dns rakha admin.rakha.xyz
```

### 6. Start the Tunnel and Verify

- Make sure Docker stack is running (`docker compose up -d` from `~/Documents/claude/platform`)
- Run `cloudflared tunnel run rakha`
- Confirm 4 connections are registered in the output
- Ask me to test `https://rakha.xyz` in my browser

### 7. Install as a System Service

So the tunnel survives reboots:

```bash
# macOS
sudo cloudflared service install
sudo launchctl start com.cloudflare.cloudflared

# Linux
sudo cloudflared service install
sudo systemctl enable cloudflared
sudo systemctl start cloudflared
```

### 8. Add `cloudflared` to Docker Compose

Add this service to `~/Documents/claude/platform/docker-compose.yml`:

```yaml
  cloudflared:
    image: cloudflare/cloudflared:latest
    command: tunnel run rakha
    volumes:
      - ~/.cloudflared:/etc/cloudflared:ro
    restart: unless-stopped
    network_mode: host
```

Make sure it's properly indented under the `services:` key alongside the other services.

### 9. Update Frontend API Base URL

The React frontend at `~/Documents/claude/applications/portfolio-tracker-frontend/` currently calls the API at `localhost:8080`. Update it so production traffic goes through the tunnel:

- Check for existing `.env`, `.env.production`, or `vite.config.ts` for API base URL configuration
- Set the production API URL to `https://api.rakha.xyz`
- Make sure local development still uses `http://localhost:8080` (don't break the dev workflow)
- The env variable is likely `VITE_API_BASE_URL` or similar — check the codebase first

### 10. Update CORS in Spring Boot Backend

The backend at `~/Documents/claude/applications/portfolio-tracker/` needs to allow requests from the new domain. Find the CORS configuration and add:

- `https://rakha.xyz`
- `https://admin.rakha.xyz`

to the allowed origins list, **keeping the existing localhost origins** so local dev still works.

### 11. Update Documentation

- Update `~/Documents/claude/CLAUDE.md` — add `rakha.xyz` domain info and the tunnel setup to the relevant sections
- Update the portfolio-tracker project's own CLAUDE.md with the production URL
- Update the frontend project's CLAUDE.md with the production API URL

### 12. Verification Checklist

Before reporting done, verify:

- [ ] `cloudflared --version` works
- [ ] Tunnel is created and has a UUID
- [ ] `~/.cloudflared/config.yml` exists with correct ingress rules
- [ ] DNS records exist in Cloudflare dashboard (3 CNAME records)
- [ ] `https://rakha.xyz` loads the frontend
- [ ] `https://api.rakha.xyz` responds (health check or swagger)
- [ ] `https://admin.rakha.xyz` loads the admin dashboard
- [ ] Frontend can make API calls without CORS errors
- [ ] `cloudflared` is registered as a system service
- [ ] `docker-compose.yml` has the cloudflared service
- [ ] Frontend `.env.production` has the correct API URL
- [ ] Backend CORS config includes the new domains
- [ ] Documentation is updated

## Important Notes

- My workspace root is `~/Documents/claude/`
- Read the root `CLAUDE.md` and each project's `CLAUDE.md` before making changes
- Do NOT push anything to git unless I ask
- Do NOT restart or modify running Docker containers without confirming with me first
- If any step fails, stop and troubleshoot — don't skip ahead
