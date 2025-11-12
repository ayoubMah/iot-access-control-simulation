# TO RUN THIS PROJECT YOU HAVE TO :

# 1 : Create a .env file in the project root named VITE_LOGS_BACKEND_SSE_URL with the logs backend's SSE endpoint as the value

# 2 : Create a docker image with the command (You should be at the react project's root) : docker build -t cockpit_front .

# 3 : Launch the logs backend

# 4 : Create a container and run it on the port 3000 : docker run -d --name cockpitFront -p 3000:3000 cockpit_front