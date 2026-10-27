# Deploy SplitMate to Render and Vercel

This project deploys the Spring Boot API and PostgreSQL database on Render, and the Vite frontend on Vercel. You need to push the `splitmoney` folder to a GitHub repository first; both platforms deploy from that repository.

The included Render Blueprint selects free plans to make the first deployment straightforward. Render’s free web service can sleep after inactivity, and its free Postgres database expires after 30 days. Treat that setup as a temporary demo; upgrade the database to a paid plan before storing household expenses you need to keep.

## 1. Push the project to GitHub

1. Create a new GitHub repository, for example `splitmoney`. Keep it private if you prefer.
2. In VS Code, open the `splitmoney` folder (the one containing `render.yaml`).
3. Open **Terminal → New Terminal** and run:

   ```powershell
   git init
   git add .
   git commit -m "Prepare SplitMate for deployment"
   git branch -M main
   git remote add origin https://github.com/YOUR_GITHUB_NAME/splitmoney.git
   git push -u origin main
   ```

   Replace `YOUR_GITHUB_NAME` with your GitHub username. The root `.gitignore` excludes `.env`; check that `.env` is not listed by `git status` before pushing. Never commit `.env` or paste its secrets into GitHub.

## 2. Deploy the API and database on Render

1. Sign in to Render and choose **New → Blueprint**.
2. Connect the GitHub repository you just pushed. Render detects the root `render.yaml` and shows the API service and PostgreSQL database it will create.
3. When asked for `CORS_ALLOWED_ORIGINS`, enter `http://localhost:5173` for now. You’ll replace it with the Vercel URL after the frontend deploys.
4. Create the Blueprint and wait for the `splitmate-api` service to finish deploying.
5. Open the API service’s **Settings** page and copy its public URL, for example `https://splitmate-api.onrender.com`.
6. Confirm the health check succeeds by visiting `https://YOUR-RENDER-API/healthz`. It should return `{"status":"ok"}`.

The Blueprint wires Render’s private PostgreSQL connection URL into the API. The application converts that URL to the JDBC format Spring needs; Flyway creates the schema on startup. Render generates the JWT signing secret. Keep the database in the same region as the API.

## 3. Deploy the frontend on Vercel

1. Sign in to Vercel and choose **Add New → Project**.
2. Import the same GitHub repository.
3. In **Root Directory**, choose `frontend` and confirm the selection.
4. Use these build settings (Vercel usually detects Vite automatically):
   - Framework preset: **Vite**
   - Build command: `npm run build`
   - Output directory: `dist`
5. Before deploying, add this environment variable under **Environment Variables**:

   ```text
   VITE_API_URL=https://YOUR-RENDER-API.onrender.com/api
   ```

   Use the API URL copied from Render. Select **Production** (and Preview too if you want preview deployments to call the same API).
6. Deploy. When Vercel finishes, copy the frontend URL, for example `https://splitmoney.vercel.app`.

Vite embeds `VITE_API_URL` during the build, so if you change it later, redeploy the frontend.

## 4. Allow the Vercel site in the API CORS settings

1. In Render, open the `splitmate-api` service and go to **Environment**.
2. Set `CORS_ALLOWED_ORIGINS` to the exact Vercel origin, including `https://` and without a trailing slash. For example:

   ```text
   https://splitmoney.vercel.app
   ```

   To allow local development as well, use a comma-separated value:

   ```text
   https://splitmoney.vercel.app,http://localhost:5173
   ```

3. Save changes and let Render redeploy the API.

## 5. Check the live app

Open the Vercel URL, register an account, create a household, and record an expense. Check the API docs at `https://YOUR-RENDER-API/swagger-ui.html` if you want to inspect the endpoints.

If the browser reports a CORS error, check that the Render `CORS_ALLOWED_ORIGINS` value exactly matches the Vercel site origin. If sign-in fails while the API is waking up, wait briefly and try again; low-cost Render services may take time to start after inactivity.

## Deploy updates

Commit and push code changes to the GitHub branch connected to each platform. Render and Vercel then build and deploy the changes. If only the frontend API URL changes, update `VITE_API_URL` in Vercel and redeploy. If only the frontend origin changes, update `CORS_ALLOWED_ORIGINS` in Render.
