# Deploy SplitMate with Neon, Render, and Vercel

This setup keeps the Spring Boot API on Render, the React frontend on Vercel, and uses Neon for managed PostgreSQL. Neon’s current Free plan is $0 and includes 100 compute-hours and 1 GB of database storage per project. Compute suspends after five idle minutes and wakes on the next query; free compute and storage limits still apply. [Neon plan details](https://neon.com/docs/introduction/plans)

You need a GitHub repository before connecting Render and Vercel. These steps do not publish anything automatically from your computer.

## 1. Push the project to GitHub

1. In VS Code, open the splitmoney folder (the one containing render.yaml).
2. Open Terminal → New Terminal and run:

~~~powershell
git status
git add .
git status
~~~

Confirm .env is not listed as staged. The .gitignore excludes it. Create an empty GitHub repository named splitmoney, then run:

~~~powershell
git commit -m "Prepare SplitMate for deployment"
git branch -M main
git remote add origin https://github.com/YOUR_GITHUB_NAME/splitmoney.git
git push -u origin main
~~~

Replace YOUR_GITHUB_NAME with your GitHub username. If you already have a Git remote, keep it and push to that repository instead.

## 2. Create the free PostgreSQL database on Neon

1. Sign in at [Neon](https://console.neon.tech/) and create a project.
2. Choose a region close to your Render API region (the Blueprint uses Render Singapore).
3. Open the project’s Connect dialog. Select the database and role, then copy the PostgreSQL connection string. It should look like:

~~~text
postgresql://USER:PASSWORD@HOST/DATABASE?sslmode=require&channel_binding=require
~~~

Keep the connection string private. Neon requires encrypted database connections. [Neon connection security](https://neon.com/blog/oops-proof-your-vibe-code-with-neon-because-mistakes-happen)

## 3. Deploy the API on Render

1. Sign in to Render and choose New → Blueprint.
2. Connect the GitHub repository. Render reads render.yaml from the repository root and configures the Spring API service.
3. When prompted for DATABASE_URL, paste the Neon connection string. For CORS_ALLOWED_ORIGINS, enter http://localhost:5173 for now.
4. Create the Blueprint and wait for splitmate-api to deploy.
5. Copy the API’s public URL from Render, for example https://splitmate-api.onrender.com.
6. Visit https://YOUR-RENDER-API/healthz; it should return a JSON response with status ok.

Render generates JWT_SECRET. The backend converts Neon’s PostgreSQL connection URL into JDBC format, Flyway creates the schema on startup, and the application listens on Render’s assigned port. [Render Blueprints](https://render.com/docs/blueprint-spec)

## 4. Deploy the frontend on Vercel

1. In Vercel, choose Add New → Project and import the same GitHub repository.
2. Set Root Directory to frontend and confirm the selection.
3. Use these Vite settings:
   - Framework preset: Vite
   - Build command: npm run build
   - Output directory: dist
4. Add this environment variable for Production (and Preview if wanted):

~~~text
VITE_API_URL=https://YOUR-RENDER-API.onrender.com/api
~~~

Replace the example with the Render URL you copied. Vite exposes client build variables prefixed with VITE_. [Vercel’s Vite guide](https://vercel.com/docs/frameworks/frontend/vite)
5. Deploy and copy the Vercel site URL, for example https://splitmoney.vercel.app.

## 5. Allow the Vercel site to call the API

1. In Render, open splitmate-api → Environment.
2. Change CORS_ALLOWED_ORIGINS to the exact Vercel origin, including https:// and without a trailing slash. For example:

~~~text
https://YOUR-VERCEL-SITE.vercel.app
~~~

To allow local development too, use a comma-separated value such as https://YOUR-VERCEL-SITE.vercel.app,http://localhost:5173.
3. Save the change and let Render redeploy.

## 6. Verify

Open the Vercel URL, register an account, create a household, and add an expense. Swagger documentation is at https://YOUR-RENDER-API/swagger-ui.html.

Neon Free includes 100 compute-hours and 1 GB storage per project; when the compute quota is exhausted, the database suspends until the next monthly period or an upgrade. Render’s free API can also sleep after inactivity, so its first request after idle may be slow. [Neon plans](https://neon.com/docs/introduction/plans), [Render free service behavior](https://render.com/docs/free)

## Deploy later changes

Commit and push changes to the GitHub branch connected to Render and Vercel. If the Vercel URL changes, update CORS_ALLOWED_ORIGINS in Render. If the API URL changes, update VITE_API_URL in Vercel and redeploy.

