# OrbisServer
OSGI web server plugin for OrbisGIS based on wisdom-framework


## Start the server

Clone the repository. Go to core-server and execute


```

mvn wisdom:run

```

Then, open your browser to: http://localhost:8080.


## Configuration Ports

In the `src/main/configuration/application.conf` file, use these properties to configure the ports:

```
http.port = HTTP port, 9000 by default
https.port = HTTPS port, disabled by default
```

You can use `0` to use a random port.
