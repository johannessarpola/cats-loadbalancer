package main

import (
	"fmt"
	"log/slog"
	"net/http"
	"os"
	"strconv"
)

var host string
var port int

func main() {
	opts := &slog.HandlerOptions{
		Level: slog.LevelDebug,
	}

	logger := slog.New(slog.NewJSONHandler(os.Stdout, opts))
	slog.SetDefault(logger)

	// Define handlers for /health and / endpoints
	http.HandleFunc("/health", healthHandler)
	http.HandleFunc("/", rootHandler)

	host = "localhost"
	// Get the port from command-line arguments or use the default (8080)
	port = getPortFromArgs()

	slog.Info(
		"server is running on",
		"port", port,
		"host", host,
	)
	err := http.ListenAndServe(fmt.Sprintf(":%d", port), nil)
	if err != nil {
		slog.Error("Error starting the server:", err)
	}
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	slog.Debug(
		"healthcheck called",
		"from", r.Host,
		"headers", r.Header,
	)
	// write ok to response
	fmt.Fprint(w, "OK")
}

func rootHandler(w http.ResponseWriter, r *http.Request) {
	slog.Debug(
		"root called",
		"from", r.Host,
		"headers", r.Header,
	)

	response := fmt.Sprintf("Hello, this is the root endpoint on %s!", url(host, port))
	// write simple message to response
	fmt.Fprint(w, response)
}

func url(host string, port int) string {
	return fmt.Sprintf("%s:%d", host, port)
}

func getPortFromArgs() int {
	if len(os.Args) > 1 {
		port := os.Args[1]
		if portNum, err := strconv.Atoi(port); err == nil {
			return portNum
		}

		slog.Error(
			"invalid port arg provided",
			"arg", port,
		)
		os.Exit(1)
	}
	return 8080
}
